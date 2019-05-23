package com.nga.xtendhr.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.client.ClientProtocolException;
import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nga.xtendhr.connection.BatchRequest;
import com.nga.xtendhr.model.Entities;
import com.nga.xtendhr.model.Fields;
import com.nga.xtendhr.model.MapCountryCompanyGroup;
import com.nga.xtendhr.model.MapGroupTemplates;
import com.nga.xtendhr.model.MapRuleFields;
import com.nga.xtendhr.model.TemplateCriteriaGeneration;
import com.nga.xtendhr.model.Templates;
import com.nga.xtendhr.service.EntitiesService;
import com.nga.xtendhr.service.FieldsService;
import com.nga.xtendhr.service.MapCountryCompanyGroupService;
import com.nga.xtendhr.service.MapGroupTemplatesService;
import com.nga.xtendhr.service.MapRuleFieldsService;
import com.nga.xtendhr.service.RulesService;
import com.nga.xtendhr.service.TemplateCriteriaGenerationService;
import com.nga.xtendhr.service.TemplateService;

/*
 * AppName: DocGen
 * Complete DocGen code
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@RestController
@RequestMapping("/DocGen")
public class DocGen {

	Logger logger = LoggerFactory.getLogger(DocGen.class);
	private static final String sfDestination = "prehiremgrSFTest";

	@Autowired
	MapCountryCompanyGroupService mapCountryCompanyGroupService;

	@Autowired
	MapGroupTemplatesService mapGroupTemplateService;

	@Autowired
	TemplateCriteriaGenerationService templateCriteriaGenerationService;

	@Autowired
	FieldsService fieldsService;

	@Autowired
	TemplateService templateService;

	@Autowired
	EntitiesService entitiesService;

	@Autowired
	MapRuleFieldsService mapRuleFieldsService;

	@Autowired
	RulesService rulesService;

	@GetMapping(value = "/login")
	public ResponseEntity<?> login(HttpServletRequest request) {
		try {
			HttpSession session = request.getSession(true);
			session.setAttribute("loginStatus", "Success");
			session.setAttribute("loggedInUser", request.getUserPrincipal().getName());
			return ResponseEntity.ok().body("Login Success!");// True to create a new session for the logged-in user as
																// its the initial call
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/executeRule")
	public ResponseEntity<?> executeRule(@RequestParam(name = "ruleID") String ruleID, HttpServletRequest request) {

		try {
			HttpSession session = request.getSession(false);// false is not create new session and use the existing
															// session
			logger.debug("Session: " + session);
			return session.getAttribute("loginStatus") != null
					? ResponseEntity.ok().body(getRuleData(ruleID, session, false).toString()) // forDirectReport false
					: new ResponseEntity<>("Session timeout! Please Login again!", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/executeRule")
	public ResponseEntity<?> executePostRule(@RequestParam(name = "ruleID") String ruleID, HttpServletRequest request,
			@RequestBody String requestData) {
		try {
			HttpSession session = request.getSession(false);// false is not create new session and use the existing
															// session
			if (session.getAttribute("loginStatus") == null) {
				return new ResponseEntity<>("Session timeout! Please Login again!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			logger.debug("ruleID: " + ruleID + " ::: requestData:" + requestData);
			session.setAttribute("requestData", requestData);
			String ruleName = rulesService.findByRuleID(ruleID).get(0).getName();
			// Calling function dynamically
			// more Info here: https://www.baeldung.com/java-method-reflection
			Method method = this.getClass().getDeclaredMethod(ruleName, String.class, HttpSession.class);
			return ResponseEntity.ok().body((String) method.invoke(this, ruleID, session));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 *** GET Rules Start***
	 */
	String checkIfManager(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB required to check if current loggenIn user is a manager
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		JSONArray directReports = new JSONArray(getFieldValue(mapRuleField.getField(), session, forDirectReport));
		String isManager = directReports.length() > 0 ? "true" : "false";
		return isManager;
	}

	String getGroups(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB required to get Groups of current loggenIn user
		JSONObject ruleData = getRuleData(ruleID, session, forDirectReport);
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		String countryID = ruleData.getString(mapRuleField.get(0).getField().getTechnicalName());
		String companyID = ruleData.getString(mapRuleField.get(1).getField().getTechnicalName());
		Boolean isManager = Boolean.parseBoolean(ruleData.getString(mapRuleField.get(2).getField().getTechnicalName()));
		Iterator<MapCountryCompanyGroup> iterator = mapCountryCompanyGroupService
				.findByCountryCompany(countryID, companyID, isManager).iterator();
		JSONArray response = new JSONArray();
		while (iterator.hasNext()) {
			response.put(iterator.next().toString());
		}
		return response.toString();
	}

	String isDirectReport(String ruleID, HttpSession session, Boolean forDirectReport) throws BatchException,
			ClientProtocolException, UnsupportedOperationException, NamingException, URISyntaxException, IOException {
		// Rule in DB to check if the logged in user is exactly a manager of the user
		// provided
		JSONObject requestObj = new JSONObject((String) session.getAttribute("requestData"));
		String directReportUserID = requestObj.getString("userID");// userID passed from UI

		// Checking if data is already fetched for a particular UserID
		if (session.getAttribute("directReportData-" + directReportUserID) != null) {
			// If yes then its already checked data required for future calls is already
			// present in session
			return ("true");
		}
		String isDirectReport;
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		logger.debug((String) session.getAttribute("requestData"));

		String url = "";
		String loggedInUser = (String) session.getAttribute("loggedInUser");
		loggedInUser = loggedInUser.equals("S0014379281") || loggedInUser.equals("S0018269301")
				|| loggedInUser.equalsIgnoreCase("S0019013022") ? "E00000882" : loggedInUser;
		url = mapRuleField.getUrl();// URL saved at required Data
		url = url.replaceFirst("<>", directReportUserID);// UserId passed from UI
		url = url.replaceFirst("<>", loggedInUser);// for direct Manager
		url = url.replaceFirst("<>", loggedInUser);// for 2nd level manager
		JSONArray responseArray = new JSONArray(callSFSingle(mapRuleField.getKey(), url));// Entity name saved in KEY
																							// column

		isDirectReport = responseArray.length() > 0 ? "true" : "false";
		// generating a unique Id for each UserID sent from the UI, In order to fetch
		// data in
		// future
		if (Boolean.parseBoolean(isDirectReport))
			session.setAttribute("directReportData-" + directReportUserID, responseArray.get(0).toString());
		return isDirectReport;
	}

	String getDirectReportCountry(String ruleID, HttpSession session, Boolean forDirectReport) {
		// Rule in DB to get country of direct Report
		// Before this rule isDirectReport must be mapped in order to check if usedID
		// provided in post body is exactly a direct Report of loggedIn user and to set
		// its data in session
		JSONObject requestObj = new JSONObject((String) session.getAttribute("requestData"));
		String directReportUserID = requestObj.getString("userID");
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		JSONObject directReportData = new JSONObject(
				(String) session.getAttribute("directReportData-" + directReportUserID));
		return getValueFromPath(mapRuleField.getValueFromPath(), directReportData);
	}

	String getDirectReportCompany(String ruleID, HttpSession session, Boolean forDirectReport) {
		// Rule in DB to get company of direct Report
		// Before this rule isDirectReport must be mapped in order to check if usedID
		// provided in post body is exactly a direct Report of loggedIn user and to set
		// its data in session
		JSONObject requestObj = new JSONObject((String) session.getAttribute("requestData"));
		String directReportUserID = requestObj.getString("userID");
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		JSONObject directReportData = new JSONObject(
				(String) session.getAttribute("directReportData-" + directReportUserID));
		return getValueFromPath(mapRuleField.getValueFromPath(), directReportData);
	}

	String getDirectReports(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, JSONException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Rule in DB to get direct report (2 levels) of the loggedIn User

		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		// getting the Parent/root array containing directReports
		JSONArray parentDirectReportArray = new JSONArray(
				getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport));
		JSONArray responseDirectReports = new JSONArray();
		JSONArray tempHoldChildDirectReports = new JSONArray();
		String childDirectReportsPath = mapRuleField.get(1).getValueFromPath();// Path to fetch Child Direct Reports
		String keyToRemoveObj = childDirectReportsPath.split("/")[0];// String to remove object from object before
																		// copying
		for (int i = 0; i < parentDirectReportArray.length(); i++) {
			tempHoldChildDirectReports = new JSONArray(
					getValueFromPath(childDirectReportsPath, parentDirectReportArray.getJSONObject(i)));
			for (int j = 0; j < tempHoldChildDirectReports.length(); j++) {
				tempHoldChildDirectReports.getJSONObject(i).remove(keyToRemoveObj); // Removing object just make is look
																					// similar as of main obj
				responseDirectReports.put(tempHoldChildDirectReports.get(j));
			}
			// removing child directReports from Parent as those are already added to
			// response
			parentDirectReportArray.getJSONObject(i).remove(keyToRemoveObj);
			responseDirectReports.put(parentDirectReportArray.getJSONObject(i));
		}
		return responseDirectReports.toString();
	}

	/*
	 *** GET Rules END***
	 */

	/*
	 *** POST Rules Start***
	 */
	String getGroupsOfDirectReport(String ruleID, HttpSession session) throws BatchException, ClientProtocolException,
			UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Rule in DB to get groups of a direct report
		JSONObject ruleData = getRuleData(ruleID, session, false); // forDirectReport false
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		Boolean isManager = Boolean.parseBoolean(ruleData.getString(mapRuleField.get(0).getField().getTechnicalName()));

		/*
		 *** Security Check *** Checking if userID passed from UI is actually a direct
		 * report of the loggenIn user
		 */
		Boolean isDirectReport = Boolean
				.parseBoolean(ruleData.getString(mapRuleField.get(1).getField().getTechnicalName()));
		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		if (isManager == false || isDirectReport == false) {
			logger.error("Unauthorized access User: " + (String) session.getAttribute("loggedInUser")
					+ " Tried accessing groups of user: " + requestData.getString("userID"));// userID passed from UI
			return "You are not authorized to access this data! This event has been logged!";
		}
		String countryID = ruleData.getString(mapRuleField.get(2).getField().getTechnicalName());
		String companyID = ruleData.getString(mapRuleField.get(3).getField().getTechnicalName());
		Iterator<MapCountryCompanyGroup> iterator = mapCountryCompanyGroupService
				.findByCountryCompany(countryID, companyID, false).iterator();
		JSONArray response = new JSONArray();
		while (iterator.hasNext()) {
			response.put(iterator.next().toString());
		}
		return response.toString();
	}

	String getTemplates(String ruleID, HttpSession session) throws BatchException, ClientProtocolException,
			UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Rule in DB to get templates of a Group of loggedIn user

		/*
		 *** Security Check *** Checking if groupID passed from UI is actually available
		 * for the loggerIn user
		 */
		JSONObject ruleData = getRuleData(ruleID, session, false); // forDirectReport false as this rule is for the
																	// loggedIn user
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		String countryID = ruleData.getString(mapRuleField.get(0).getField().getTechnicalName());
		String companyID = ruleData.getString(mapRuleField.get(1).getField().getTechnicalName());
		Boolean isManager = Boolean.parseBoolean(ruleData.getString(mapRuleField.get(2).getField().getTechnicalName()));

		JSONObject requestObj = new JSONObject((String) session.getAttribute("requestData"));
		String groupID = requestObj.getString("groupID");// groupID passed from UI

		Boolean groupAvailableCheck = mapCountryCompanyGroupService
				.findByGroupCountryCompany(groupID, countryID, companyID, isManager).size() == 1 ? true : false;
		if (groupAvailableCheck) {
			List<MapGroupTemplates> mapGroupTemplate = mapGroupTemplateService.findByGroupID(groupID);
			// Now Iterating for each template assigned to the provided group
			Iterator<MapGroupTemplates> iterator = mapGroupTemplate.iterator();
			String generatedCriteria;
			String templateID;
			List<Templates> template;
			Templates tempTemplate;
			JSONArray response = new JSONArray();
			while (iterator.hasNext()) {
				templateID = iterator.next().getTemplateID();
				// Generating criteria for each template
				generatedCriteria = generateCriteria(templateID, session, false); // forDirectReport false
				template = templateService.findByIdAndCriteria(templateID, generatedCriteria);
				tempTemplate = template.size() > 0 ? template.get(0) : null;
				if (tempTemplate != null) {
					response.put(tempTemplate.toString());
				}
			}
			return response.toString();
		}
		logger.error("Unauthorized access User: " + (String) session.getAttribute("loggedInUser")
				+ " Tried accessing templates of group that is not available for this user. groupID: " + groupID);
		return "You are not authorized to access this data! This event has been logged!";
	}

	String getTemplatesOfDirectReports(String ruleID, HttpSession session)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB to get templates of a direct report

		/*
		 *** Security Check *** Checking if loggenIn user is a manager and UserID passed
		 * from UI is actually a directReport of the loggedIn User
		 */
		JSONObject ruleData = getRuleData(ruleID, session, false); // forDirectReport false as this rule is for the
																	// loggedIn user
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		String directReportCountryID = ruleData.getString(mapRuleField.get(0).getField().getTechnicalName());
		String directReportCompanyID = ruleData.getString(mapRuleField.get(1).getField().getTechnicalName());
		Boolean isManager = Boolean.parseBoolean(ruleData.getString(mapRuleField.get(2).getField().getTechnicalName()));
		Boolean isDirectReport = Boolean
				.parseBoolean(ruleData.getString(mapRuleField.get(3).getField().getTechnicalName()));

		String loggerInUser = (String) session.getAttribute("loggedInUser");
		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		if (isManager == false || isDirectReport == false) {
			logger.error("Unauthorized access User: " + loggerInUser + " Tried accessing groups of user: "
					+ requestData.getString("userID"));// userID passed from UI
			return "You are not authorized to access this data! This event has been logged!";
		}

		/*
		 *** Security Check *** Checking if groupID passed from UI is actually available
		 * for the userID provided from the UI
		 */
		String groupID = requestData.getString("groupID");// groupID passed from UI
		Boolean groupAvailableCheck = mapCountryCompanyGroupService
				.findByGroupCountryCompany(groupID, directReportCountryID, directReportCompanyID, false).size() == 1
						? true
						: false;
		if (groupAvailableCheck) {
			// Now getting templates those are available for the userID provided from UI
			List<MapGroupTemplates> mapGroupTemplate = mapGroupTemplateService.findByGroupID(groupID);
			// Now Iterating for each template assigned to the provided group
			Iterator<MapGroupTemplates> iterator = mapGroupTemplate.iterator();
			String generatedCriteria;
			String templateID;
			List<Templates> template;
			Templates tempTemplate;
			JSONArray response = new JSONArray();
			while (iterator.hasNext()) {
				templateID = iterator.next().getTemplateID();
				// Generating criteria for each template
				generatedCriteria = generateCriteria(templateID, session, true);// forDirectReport true
				template = templateService.findByIdAndCriteria(templateID, generatedCriteria);
				tempTemplate = template.size() > 0 ? template.get(0) : null;
				if (tempTemplate != null) {
					response.put(tempTemplate.toString());
				}
			}
			return response.toString();
		}
		logger.error("Unauthorized access User: " + loggerInUser
				+ " Tried accessing templates of group that is not available for user provided from UI userID:"
				+ requestData.getString("userID") + " groupID: " + groupID);
		return "You are not authorized to access this data! This event has been logged!";
	}
	/*
	 *** POST Rules END***
	 */

	/*
	 *** Helper functions Start***
	 */
	private String generateCriteria(String templateID, HttpSession session, Boolean forDirectReport)
			throws NamingException, BatchException, ClientProtocolException, UnsupportedOperationException,
			URISyntaxException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		List<TemplateCriteriaGeneration> templateCriteriaGeneration = templateCriteriaGenerationService
				.findByTemplateID(templateID); // This will get fields IDs required to generate criteria

		// Now Iterating for each field mapped for Criteria generation
		Iterator<TemplateCriteriaGeneration> iterator = templateCriteriaGeneration.iterator();
		String criteria = "";
		while (iterator.hasNext()) {
			criteria = criteria + getFieldValue(iterator.next().getField(), session, forDirectReport) + "|";
		}
		criteria = criteria.length() > 0 ? criteria.substring(0, criteria.length() - 1) : "";
		logger.debug("criteria: " + criteria + " for  templateID: " + templateID + " ::: forDirectReport: "
				+ forDirectReport);
		return criteria;
	}

	private String getFieldValue(Fields field, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NamingException,
			URISyntaxException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		logger.debug("Getting value for Field: " + field.getTechnicalName() + "  ::: RuleID: " + field.getRuleID()
				+ " ::: forDirectReport: " + forDirectReport);
		if (field.getRuleID() == null) {
			JSONObject entityData;
			Entities entity = field.getEntity();
			logger.debug("EntityName: " + entity.getName() + " For GetFieldValue Field: " + field.getTechnicalName());
			entity = checkForDependantEntity(entity); // Check for root entity and get root entity if current entity
														// is
														// dependent on some other entity
			// now entity variable will be having the root entity from which will get the
			// data of our field
			entityData = getEntityData(entity, session, forDirectReport);
			return getValueFromPath(field.getValueFromPath(), entityData);
		}
		// Calling function dynamically
		// more Info here: https://www.baeldung.com/java-method-reflection
		Method method = this.getClass().getDeclaredMethod(field.getRule().getName(), String.class, HttpSession.class,
				Boolean.class);
		return (String) method.invoke(this, field.getRuleID(), session, forDirectReport);
	}

	private Entities checkForDependantEntity(Entities entity) { // function to get the root entity
		if (!entity.getIsDependant()) { // check if the entity is the root entity
			return entity; // return the root entity
		}
		// If not call checkForDependantEntity i.e. recursively with the dependent
		// Entity
		return checkForDependantEntity(entity.getDependantOnEntity());
	}

	private JSONObject getEntityData(Entities entity, HttpSession session, Boolean forDirectReport)
			throws NamingException, BatchException, ClientProtocolException, UnsupportedOperationException,
			URISyntaxException, IOException {
		// function to get data of the root entity
		JSONObject entityData;
		String entityName = entity.getName();
		if (!forDirectReport) {// if false then data needs to get for the loggedIn user
			if (session.getAttribute(entityName) != null) { // Check if entity data is present in the Session
				entityData = new JSONObject((String) session.getAttribute(entityName));
				logger.debug("Data fetched from session for Entity: " + entityName);
				return entityData;
			} // else retrieve data from SF
			entityData = fetchDataFromSF(entity, session, forDirectReport);
			logger.debug(
					"Data fetched from SF for entity: " + entityName + " ::: For Direct report: " + forDirectReport);
			return entityData;
		}
		// Else retrieve data for direct report
		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		String directReportUserID = requestData.getString("userID");
		if (session.getAttribute("directReportEntities-" + directReportUserID + entityName) != null) {
			entityData = new JSONObject(
					(String) session.getAttribute("directReportEntities-" + directReportUserID + entityName));
			logger.debug("Data fetched from session for directReport Entity: " + entityName
					+ " ::: directReportUserID: " + directReportUserID);
			return entityData;
		} // else retrieve data from SF
		entityData = fetchDataFromSF(entity, session, forDirectReport);
		logger.debug("Data fetched from SF for direct report entity: " + entityName + " ::: For Direct report: "
				+ forDirectReport);
		return entityData;
	}

	private JSONObject fetchDataFromSF(Entities entity, HttpSession session, Boolean forDirectReport)
			throws NamingException, BatchException, ClientProtocolException, UnsupportedOperationException,
			URISyntaxException, IOException {
		// Calling SF URL to fetch Entity data
		String loggedInUser = (String) session.getAttribute("loggedInUser");
		String entityName = entity.getName();
		String directReportUserID = null;
		loggedInUser = loggedInUser.equals("S0014379281") || loggedInUser.equals("S0018269301")
				|| loggedInUser.equalsIgnoreCase("S0019013022") ? "E00000882" : loggedInUser;
		Map<String, String> entityMap = new HashMap<String, String>();
		BatchRequest batchRequest = new BatchRequest();
		batchRequest.configureDestination(sfDestination);
		String selectPath = createSelectPath(entity); // Create GetPath for all the fields those are dependent on root
														// entity
		String expandPath = "";
		List<Entities> dependentEntities = getDependentEntities(entity); // Get all the entities those are dependent on
																			// the root entity
		if (dependentEntities != null) {
			Iterator<Entities> iterator = dependentEntities.iterator(); // Iterating for each Entity for creating select
																		// and expand path
			Entities tempEntity;
			String tempPath = "";
			while (iterator.hasNext()) {
				tempEntity = iterator.next();
				tempPath = createSelectPath(tempEntity);
				selectPath = selectPath == "" ? tempPath : tempPath != "" ? selectPath + "," + tempPath : selectPath;
				tempPath = tempEntity.getExpandPath();
				expandPath = tempPath.toString() != "null" || tempPath.toString() != "" ? expandPath + tempPath + ","
						: expandPath;
			}
			expandPath = expandPath.length() > 0 ? expandPath.substring(0, expandPath.length() - 1) : "";
		}
		logger.debug("Generated expand path: " + expandPath + "...for entity: " + entityName);
		logger.debug("Generated select path: " + selectPath + "...for entity: " + entityName);

		if (!forDirectReport)
			entityMap.put(entity.getName(), "?$filter=" + entity.getFilter() + " eq '" + loggedInUser
					+ "'&$format=json&$expand=" + expandPath + "&$select=" + selectPath);
		else {
			// Else retrieve data for direct report
			JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
			directReportUserID = requestData.getString("userID");
			entityMap.put(entity.getName(), "?$filter=" + entity.getFilter() + " eq '" + directReportUserID
					+ "'&$format=json&$expand=" + expandPath + "&$select=" + selectPath);
		}

		logger.debug("Generated URL: " + entity.getName() + "/" + "?$filter=" + entity.getFilter() + " eq '"
				+ loggedInUser + "'&$format=json&$expand=" + expandPath + "&$select=" + selectPath);

		// adding request to Batch
		for (Map.Entry<String, String> entityM : entityMap.entrySet()) {
			batchRequest.createQueryPart("/" + entityM.getKey() + entityM.getValue(), entityM.getKey());
		}

		batchRequest.callBatchPOST("/$batch", "");// Executing Batch request
		List<BatchSingleResponse> batchResponses = batchRequest.getResponses();
		String response;
		JSONObject responseObj;
		Map<String, JSONObject> entityResponseMap = new HashMap<String, JSONObject>(); // reading responses from Batch
																						// call
		for (BatchSingleResponse batchResponse : batchResponses) {
			responseObj = new JSONObject(batchResponse.getBody()).getJSONObject("d").getJSONArray("results")
					.getJSONObject(0);
			response = responseObj.toString();
			entityResponseMap.put(entityName, responseObj);
			if (!forDirectReport)
				session.setAttribute(entityName, response);
			else
				session.setAttribute("directReportEntities-" + directReportUserID + entityName, response);
			// logger.debug(entityName + " Response: " + response);
		}
		return entityResponseMap.get(entityName);
	}

	private String createSelectPath(Entities entity) { // This function will create the select path for the entity with
														// all the fields those are dependent on the given entity
		List<Fields> fields = fieldsService.findByEntity(entity.getId()); // Getting all the fields those are dependent
																			// on the specified entity
		String selectPath = "";
		Iterator<Fields> iterator = fields.iterator(); // Iterating for each field
		String tempPath = "";
		while (iterator.hasNext()) {
			tempPath = iterator.next().getSelectOption();
			selectPath = tempPath.toString() != "null" || tempPath.toString() != "" ? selectPath + tempPath + ","
					: selectPath;
		}
		return selectPath.length() > 0 ? selectPath.substring(0, selectPath.length() - 1) : "";
	}

	private List<Entities> getDependentEntities(Entities entity) { // select all the entities those are dependent on the
																	// root entity
		List<Entities> dependentEntites = entitiesService.findAllDependant(entity.getId());
		return dependentEntites.size() > 0 ? dependentEntites : null;
	}

	private String getValueFromPath(String path, JSONObject retriveFromObj) throws JSONException {
		String[] pathArray = path.split("/");
		JSONObject currentObject = retriveFromObj;
		String value = null;
		for (String key : pathArray) {
			if (key.endsWith("\\0")) {// then value is at this location
				value = key.substring(key.length() - 4, key.length() - 3).equals("*") // Checking if complete array is
																						// required in output
						? currentObject.getJSONArray(key.substring(0, key.length() - 5)).toString()
						: currentObject.get(key.substring(0, key.length() - 2)).toString().equals("null") ? "null"
								: currentObject.getString(key.substring(0, key.length() - 2));
			} else if (key.endsWith("]")) { // in case of array
				currentObject = currentObject.getJSONArray(key.substring(0, key.length() - 2)).getJSONObject(0);
			} else {// in case of Obj
				currentObject = currentObject.getJSONObject(key);
			}
		}
		// logger.debug("Returned Value for Path: " + path + "... Value: " + value);
		return value;
	}

	private JSONObject getRuleData(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NamingException,
			URISyntaxException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// This function will create jsonObject for a particular rule
		List<MapRuleFields> mapRuleFields = mapRuleFieldsService.findByRuleID(ruleID);
		MapRuleFields mapRuleField;
		JSONObject responseObj = new JSONObject();
		Fields field;
		Iterator<MapRuleFields> iterator = mapRuleFields.iterator();
		while (iterator.hasNext()) {
			mapRuleField = iterator.next();
			field = mapRuleField.getField();
			responseObj.put(field.getTechnicalName(), getFieldValue(field, session, forDirectReport));
		}
		return responseObj;
	}

	private String callSFSingle(String entityName, String url) throws NamingException, BatchException,
			ClientProtocolException, UnsupportedOperationException, URISyntaxException, IOException {
		// function used to make single calls to SF that are required to get dynamic
		// data
		Map<String, String> entityMap = new HashMap<String, String>();
		BatchRequest batchRequest = new BatchRequest();
		batchRequest.configureDestination(sfDestination);
		entityMap.put(entityName, url);
		logger.debug("Generated URL for single call: " + entityName + url);
		// adding request to Batch
		for (Map.Entry<String, String> entityM : entityMap.entrySet()) {
			batchRequest.createQueryPart("/" + entityM.getKey() + entityM.getValue(), entityM.getKey());
		}
		batchRequest.callBatchPOST("/$batch", "");// Executing Batch request
		List<BatchSingleResponse> batchResponses = batchRequest.getResponses();

		JSONArray responseArray = new JSONObject(batchResponses.get(0).getBody()).getJSONObject("d")
				.getJSONArray("results");// Note the complete results array is returned not the object inside results
											// array
		String response = responseArray.toString();
		logger.debug("Response from single request: " + response);
		return response;
	}
	/*
	 *** Helper functions END***
	 */
}
