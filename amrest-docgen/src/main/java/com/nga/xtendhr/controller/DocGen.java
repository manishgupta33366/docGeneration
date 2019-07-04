package com.nga.xtendhr.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
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
import com.nga.xtendhr.connection.DestinationClient;
import com.nga.xtendhr.model.Entities;
import com.nga.xtendhr.model.Fields;
import com.nga.xtendhr.model.MapCountryCompanyGroup;
import com.nga.xtendhr.model.MapGroupTemplates;
import com.nga.xtendhr.model.MapRuleFields;
import com.nga.xtendhr.model.MapTemplateFields;
import com.nga.xtendhr.model.TemplateCriteriaGeneration;
import com.nga.xtendhr.model.Templates;
import com.nga.xtendhr.service.EntitiesService;
import com.nga.xtendhr.service.FieldsService;
import com.nga.xtendhr.service.MapCountryCompanyGroupService;
import com.nga.xtendhr.service.MapGroupTemplatesService;
import com.nga.xtendhr.service.MapRuleFieldsService;
import com.nga.xtendhr.service.MapTemplateFieldsService;
import com.nga.xtendhr.service.RulesService;
import com.nga.xtendhr.service.TemplateCriteriaGenerationService;
import com.nga.xtendhr.service.TemplateService;
import com.nga.xtendhr.utility.CommonFunctions;
import com.nga.xtendhr.utility.CommonVariables;

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

	private enum hunLocale {
		január, február, március, április, május, junius, julius, augusztus, szeptember, október, november, december
	};

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

	@Autowired
	MapTemplateFieldsService mapTemplateFieldsService;

	@GetMapping(value = "/login")
	public ResponseEntity<?> login(HttpServletRequest request) {
		try {
			HttpSession session = request.getSession(false);
			String loggedInUser = request.getUserPrincipal().getName();
			loggedInUser = loggedInUser.equals("S0014379281") || loggedInUser.equals("S0018269301")
					|| loggedInUser.equals("S0019013022") || loggedInUser.equals("S0020227452") ? "E00000815"
							: loggedInUser;
//			if (session != null) {
//				session.invalidate();
//			}
			session = request.getSession(true);
			session.setAttribute("loginStatus", "Success");
			session.setAttribute("loggedInUser", loggedInUser);
			JSONObject response = new JSONObject();
			response.put("login", "success");

			if (CommonFunctions.checkIfAdmin(loggedInUser, CommonVariables.sfDestination)) {
				session.setAttribute("adminLoginStatus", "Success");
				response.put("isAdmin", true);
			}
			return ResponseEntity.ok().body(response.toString());// True to create a new session for the logged-in user
																	// as its the initial call
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/test")
	public ResponseEntity<?> test(@RequestParam(name = "url") String url) {
		try {
			JSONObject body = new JSONObject();
			body.put("Gcc", "AMR");
			body.put("CompanyCode", "AMR_HU001");
			body.put("CountryCode", "AMR");
			return ResponseEntity.ok().body(CommonFunctions.callpostAPI(url, body));
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
			Method method = this.getClass().getDeclaredMethod(ruleName, String.class, HttpSession.class, Boolean.class);
			return ResponseEntity.ok().body((String) method.invoke(this, ruleID, session, false));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 *** For Admin Start***
	 */
	@GetMapping(value = "/docGenAdmin/searchUser")
	public ResponseEntity<?> searchUser(@RequestParam(name = "searchString", required = false) String searchString,
			HttpServletRequest request)
			throws ClientProtocolException, IOException, URISyntaxException, NamingException {
		try {
			HttpSession session = request.getSession(false);// false is not create new session and use the existing
															// session
			if (session.getAttribute("loginStatus") == null) {
				return new ResponseEntity<>("Session timeout! Please Login again!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			/*
			 *** Security Check *** Checking if user trying to login is exactly an Admin or
			 * not
			 *
			 */
			else if (session.getAttribute("adminLoginStatus") == null) {
				logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
						+ ", which is not an admin in SF, tried to login as admin.");
				return new ResponseEntity<>(
						"Error! You are not authorized to access this resource! This event has been logged!",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			HttpResponse searchResponse;
			String searchResponseJsonString;
			JSONObject searchResponseResponseObject;
			JSONArray searchResponseObjectResultArray;
			if (searchString == null) {
				searchResponse = CommonFunctions.getDestinationCLient(CommonVariables.sfDestination).callDestinationGET(
						"/User",
						"?$format=json&$select=userId,firstName,lastName&$filter=firstName ne null and lastName ne null");
			} else {
				searchString = searchString.toLowerCase();
				String url = "?$format=json&$select=userId,firstName,lastName&$filter=substringof('<inputParameter>',tolower(firstName)) or substringof('<inputParameter>',tolower(lastName)) or substringof('<inputParameter>',tolower(userId))";
				url = url.replace("<inputParameter>", searchString);
				searchResponse = CommonFunctions.getDestinationCLient(CommonVariables.sfDestination)
						.callDestinationGET("/User", url);
			}
			searchResponseJsonString = EntityUtils.toString(searchResponse.getEntity(), "UTF-8");
			searchResponseResponseObject = new JSONObject(searchResponseJsonString);
			searchResponseObjectResultArray = searchResponseResponseObject.getJSONObject("d").getJSONArray("results");
			return ResponseEntity.ok().body(searchResponseObjectResultArray.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/docGenAdmin/executePostCallRule")
	public ResponseEntity<?> executeRule(@RequestParam(name = "ruleID") String ruleID, @RequestBody String requestData,
			HttpServletRequest request) {

		try {
			HttpSession session = request.getSession(false);// false is not create new session and use the existing
															// session
			if (session.getAttribute("loginStatus") == null) {
				return new ResponseEntity<>("Session timeout! Please Login again!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			/*
			 *** Security Check *** Checking if user trying to login is exactly an Admin or
			 * not
			 *
			 */
			else if (session.getAttribute("adminLoginStatus") == null) {
				logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
						+ ", which is not an admin in SF, tried to login as admin.");
				return new ResponseEntity<>(
						"Error! You are not authorized to access this resource! This event has been logged!",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
			return ResponseEntity.ok()
					.body(CommonFunctions.callpostAPI(mapRuleField.getUrl(), new JSONObject(requestData)));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	String adminGetGroupsOfDirectReport(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB to get groups of a direct report for Admin

		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		if (session.getAttribute("loginStatus") == null) {
			return "Session timeout! Please Login again!";
		}
		/*
		 *** Security Check *** Checking if user trying to login is exactly an Admin or
		 * not
		 *
		 */
		else if (session.getAttribute("adminLoginStatus") == null) {
			logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
					+ ", which is not an admin in SF, tried to access Groups of a user:"
					+ requestData.getString("userID"));
			return "Error! You are not authorized to access this resource! This event has been logged!";
		}
		getFieldValue(mapRuleField.get(0).getField(), session, true);// get data of direct report
		String countryID = getFieldValue(mapRuleField.get(1).getField(), session, true);// forDirectReport true
		String companyID = getFieldValue(mapRuleField.get(2).getField(), session, true);// forDirectReport true
		Iterator<MapCountryCompanyGroup> iterator = mapCountryCompanyGroupService
				.findByCountryCompanyAdmin(countryID, companyID).iterator();
		JSONArray response = new JSONArray();
		while (iterator.hasNext()) {
			response.put(iterator.next().toString());
		}
		return response.toString();
	}

	String adminGetTemplatesOfDirectReports(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB to get templates of a direct report for admin

		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		/*
		 *** Security Check *** Checking if user trying to login is exactly an Admin or
		 * not
		 *
		 */
		if (session.getAttribute("adminLoginStatus") == null) {
			logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
					+ ", which is not an admin in SF, tried to access Groups of a user:"
					+ requestData.getString("userID"));
			return "Error! You are not authorized to access this resource! This event has been logged!";
		}

		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		getFieldValue(mapRuleField.get(0).getField(), session, true);// get data of direct report
		String directReportCountryID = getFieldValue(mapRuleField.get(1).getField(), session, true);// forDirectReport
																									// true
		String directReportCompanyID = getFieldValue(mapRuleField.get(2).getField(), session, true);// forDirectReport
																									// true
		getFieldValue(mapRuleField.get(3).getField(), session, true);// get get Templates from Azure and set that in
																		// session and forDirectReport true

		/*
		 *** Security Check *** Checking if groupID passed from UI is actually available
		 * for the userID provided from the UI
		 */
		String groupID = requestData.getString("groupID");// groupID passed from UI
		String loggerInUser = (String) session.getAttribute("loggedInUser");
		Boolean groupAvailableCheck = mapCountryCompanyGroupService
				.findByGroupCountryCompany(groupID, directReportCountryID, directReportCompanyID).size() == 1 ? true
						: false;
		if (!groupAvailableCheck) {
			logger.error("Unauthorized access! User: " + loggerInUser
					+ " Tried accessing templates of group that is not available for user provided from UI userID:"
					+ requestData.getString("userID") + " groupID: " + groupID);
			return "You are not authorized to access this data! This event has been logged!";
		}

		// get available Templates in Azure from Session
		@SuppressWarnings("unchecked")
		Map<String, JSONObject> templatesAvailableInAzure = (Map<String, JSONObject>) session
				.getAttribute("availableTemplatesForDirectReport");

		// Now getting templates those are available for the userID provided from UI
		List<MapGroupTemplates> mapGroupTemplate = mapGroupTemplateService.findByGroupID(groupID);
		// Now Iterating for each template assigned to the provided group
		Iterator<MapGroupTemplates> iterator = mapGroupTemplate.iterator();
		String generatedCriteria;
		String templateID;
		List<Templates> tempTemplate;
		JSONArray response = new JSONArray();
		MapGroupTemplates tempMapGroupTemplate;
		JSONObject tempTemplateJsonObject;
		while (iterator.hasNext()) {
			tempMapGroupTemplate = iterator.next();

			// Generating criteria for each template to check if its valid for the loggedIn
			// user
			templateID = tempMapGroupTemplate.getTemplateID();
			generatedCriteria = generateCriteria(templateID, session, false); // forDirectReport false
			tempTemplate = templateService.findByIdAndCriteria(templateID, generatedCriteria);
			if (tempTemplate.size() > 0) {
				// check if the template is available in Azure
				if (!templatesAvailableInAzure.containsKey(tempMapGroupTemplate.getTemplate().getName())) {
					tempTemplateJsonObject = new JSONObject(tempTemplate.get(0).toString());
					tempTemplateJsonObject.put("availableInAzure", false);
					response.put(tempTemplateJsonObject.toString());
					continue;
				}
				response.put(tempTemplate.get(0).toString());
			}
		}
		return response.toString();
	}

	String adminGetGroups(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {

		/*
		 *** Security Check *** Checking if user trying to login is exactly an Admin or
		 * not
		 *
		 */
		if (session.getAttribute("adminLoginStatus") == null) {
			logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
					+ ", which is not an admin in SF, tried to access adminGetGroups endpoint");
			return "Error! You are not authorized to access this resource! This event has been logged!";
		}

		// Rule in DB required to get Groups of current loggenIn user
		JSONObject ruleData = getRuleData(ruleID, session, forDirectReport);
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		String countryID = ruleData.getString(mapRuleField.get(0).getField().getTechnicalName());
		String companyID = ruleData.getString(mapRuleField.get(1).getField().getTechnicalName());
		Iterator<MapCountryCompanyGroup> iterator = mapCountryCompanyGroupService
				.findByCountryCompanyAdmin(countryID, companyID).iterator();
		JSONArray response = new JSONArray();
		while (iterator.hasNext()) {
			response.put(iterator.next().toString());
		}
		return response.toString();
	}

	String getDirectReportData(String ruleID, HttpSession session, Boolean forDirectReport) throws BatchException,
			ClientProtocolException, UnsupportedOperationException, NamingException, URISyntaxException, IOException {
		// Rule in DB get Data of a DirectReport for Admin

		/*
		 *** Security Check *** Checking if user trying to login is exactly an Admin or
		 * not
		 *
		 */
		if (session.getAttribute("adminLoginStatus") == null) {
			logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
					+ ", which is not an admin in SF, tried to access Admin Group endpoint");
			return "Error! You are not authorized to access this resource! This event has been logged!";
		}

		JSONObject requestObj = new JSONObject((String) session.getAttribute("requestData"));
		String directReportUserID = requestObj.getString("userID");// userID passed from UI

		// Checking if data is already fetched for a particular UserID
		if (session.getAttribute("directReportData-" + directReportUserID) != null) {
			// If yes then data is already fetched for the given user and is present the in
			// the session
			return ("true");
		}
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);

		String url = "";
		url = mapRuleField.getUrl();// URL saved at required Data
		url = url.replaceFirst("<>", directReportUserID);// UserId passed from UI

		JSONArray responseArray = new JSONArray(callSFSingle(mapRuleField.getKey(), url));// Entity name saved in KEY
																							// column
		session.setAttribute("directReportData-" + directReportUserID, responseArray.get(0).toString());
		return "true";
	}

	String adminDocDownloadDirectReport(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {

		// Rule in DB to download doc of Direct report for Admin

		/*
		 *** Security Check *** Checking if user trying to login is exactly an Admin or
		 * not
		 *
		 */
		if (session.getAttribute("adminLoginStatus") == null) {
			logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
					+ ", which is not an admin in SF, tried to access adminDocDownloadDirectReport endpoint");
			return "Error! You are not authorized to access this resource! This event has been logged!";
		}
		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		String loggerInUser = (String) session.getAttribute("loggedInUser");
		String templateID = requestData.getString("templateID");

		/*
		 *** Security Check *** Checking if templateID passed from UI is actually
		 * available for the loggedIn user
		 */

		if (!adminTemplateAvailableCheck(ruleID, session, true)) { // for DirectReport
			logger.error("Unauthorized access! User: " + loggerInUser
					+ " Tried downlaoding document of a template templateID: " + templateID
					+ " Which is not available for the UserId provided.");
			return "You are not authorized to access this data! This event has been logged!";
		}
		// Now Generating Object to POST
		JSONObject docRequestObject = getDocPostObject(templateID, session, true);// for direct Report true
		logger.debug("Doc Generation Request Obj: " + docRequestObject.toString());
		return getDocFromAPI(docRequestObject);
	}

	String adminDocDownload(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, JSONException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {

		// Rule in DB to download doc for Admin
		String loggerInUser = (String) session.getAttribute("loggedInUser");
		/*
		 *** Security Check *** Checking if user trying to login is exactly an Admin or
		 * not
		 *
		 */
		if (session.getAttribute("adminLoginStatus") == null) {
			logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
					+ ", which is not an admin in SF, tried to access adminDocDownload endpoint");
			return "Error! You are not authorized to access this resource! This event has been logged!";
		}
		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		String templateID = requestData.getString("templateID");

		/*
		 *** Security Check *** Checking if templateID passed from UI is actually
		 * available for the loggedIn user
		 */

		if (!adminTemplateAvailableCheck(ruleID, session, false)) { // for DirectReport
			logger.error("Unauthorized access! User: " + loggerInUser
					+ " Tried downlaoding document of a template that is not assigned for this user, templateID: "
					+ templateID);
			return "You are not authorized to access this data! This event has been logged!";
		}

		// Now Generating Object to POST
		JSONObject docRequestObject = getDocPostObject(templateID, session, false); // for direct report false
		logger.debug("Doc Generation Request Obj: " + docRequestObject.toString());
		return getDocFromAPI(docRequestObject);
	}

	String adminGetTemplates(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB to get templates of a Group of admin

		/*
		 *** Security Check *** Checking if user trying to login is exactly an Admin or
		 * not
		 *
		 */
		if (session.getAttribute("adminLoginStatus") == null) {
			logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
					+ ", which is not an admin in SF, tried to access Get Templates endpoint");
			return "Error! You are not authorized to access this resource! This event has been logged!";
		}

		/*
		 *** Security Check *** Checking if groupID passed from UI is actually available
		 * for the loggerIn user
		 */
		JSONObject ruleData = getRuleData(ruleID, session, false); // forDirectReport false as this rule is for the
																	// loggedIn user
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		String countryID = ruleData.getString(mapRuleField.get(0).getField().getTechnicalName());
		String companyID = ruleData.getString(mapRuleField.get(1).getField().getTechnicalName());

		JSONObject requestObj = new JSONObject((String) session.getAttribute("requestData"));
		String groupID = requestObj.getString("groupID");// groupID passed from UI

		Boolean groupAvailableCheck = mapCountryCompanyGroupService
				.findByGroupCountryCompany(groupID, countryID, companyID).size() == 1 ? true : false;
		if (!groupAvailableCheck) {
			logger.error("Unauthorized access! User: " + (String) session.getAttribute("loggedInUser")
					+ " Tried accessing templates of group that is not available for this user. groupID: " + groupID);
			return "You are not authorized to access this data! This event has been logged!";
		}
		// get available Templates in Azure from Session
		@SuppressWarnings("unchecked")
		Map<String, JSONObject> templatesAvailableInAzure = (Map<String, JSONObject>) session
				.getAttribute("availableTemplatesInAzure");
		List<MapGroupTemplates> mapGroupTemplate = mapGroupTemplateService.findByGroupID(groupID);
		// Now Iterating for each template assigned to the provided group
		Iterator<MapGroupTemplates> iterator = mapGroupTemplate.iterator();
		String generatedCriteria;
		String templateID;
		List<Templates> tempTemplate;
		JSONArray response = new JSONArray();
		MapGroupTemplates tempMapGroupTemplate;
		JSONObject tempTemplateJsonObject;
		while (iterator.hasNext()) {
			tempMapGroupTemplate = iterator.next();

			// Generating criteria for each template to check if its valid for the loggedIn
			// user
			templateID = tempMapGroupTemplate.getTemplateID();
			generatedCriteria = generateCriteria(templateID, session, false); // forDirectReport false
			tempTemplate = templateService.findByIdAndCriteria(templateID, generatedCriteria);
			if (tempTemplate.size() > 0) {
				// check if the template is available in Azure
				if (!templatesAvailableInAzure.containsKey(tempMapGroupTemplate.getTemplate().getName())) {
					tempTemplateJsonObject = new JSONObject(tempTemplate.get(0).toString());
					tempTemplateJsonObject.put("availableInAzure", false);
					response.put(tempTemplateJsonObject.toString());
					continue;
				}
				response.put(tempTemplate.get(0).toString());
			}
		}
		return response.toString();
	}

	private Boolean adminTemplateAvailableCheck(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, JSONException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		JSONArray availableTemplates;
		JSONArray availableGroups = new JSONArray(
				getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport));
		logger.debug("Available Groups:" + availableGroups.toString() + " ::: forDirectReport" + forDirectReport);
		String groupID;
		for (int i = 0; i < availableGroups.length(); i++) {
			// saving group ID in Session requestData attribute as its expected in Get
			// Templates function
			groupID = new JSONObject(availableGroups.getString(i)).getString("id");
			requestData.put("groupID", groupID);
			session.setAttribute("requestData", requestData.toString());
			availableTemplates = new JSONArray(getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport));
			logger.debug(
					"Available templates:" + availableTemplates.toString() + " ::: forDirectReport" + forDirectReport);
			for (int j = 0; j < availableTemplates.length(); j++) {
				if (requestData.getString("templateID")
						.equals(new JSONObject(availableTemplates.getString(j)).getString("id"))) {
					return true;
				}
			}
		}
		return false;
	}
	/*
	 *** For Admin End***
	 */

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

	String checkIfAdmin(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB required to check if current loggenIn user is an admin
		return session.getAttribute("adminLoginStatus") != null ? "true" : "false";
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

		String url = "";
		String loggedInUser = (String) session.getAttribute("loggedInUser");
		url = mapRuleField.getUrl();// URL saved at required Data
		url = url.replaceFirst("<>", directReportUserID);// UserId passed from UI
		url = url.replaceAll("<>", loggedInUser);// for direct Manager and for 2nd level manager

		JSONArray responseArray = new JSONArray(callSFSingle(mapRuleField.getKey(), url));// Entity name saved in KEY
																							// column
		isDirectReport = responseArray.length() > 0 ? "true" : "false";
		// generating a unique Id for each UserID sent from the UI, In order to fetch
		// data in future
		if (Boolean.parseBoolean(isDirectReport))
			// Generating unique ID for each directReport in session -- in Case of future
			// use
			session.setAttribute("directReportData-" + directReportUserID, responseArray.get(0).toString());
		return isDirectReport;
	}

	String getDirectReportCountry(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, JSONException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Rule in DB to get country of direct Report
		// Before this rule isDirectReport must be mapped in order to check if usedID
		// provided in post body is exactly a direct Report of loggedIn user and to set
		// its data in session
		JSONObject requestObj = new JSONObject((String) session.getAttribute("requestData"));
		String directReportUserID = requestObj.getString("userID");
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		JSONObject directReportData = new JSONObject(
				(String) session.getAttribute("directReportData-" + directReportUserID));
		return getValueFromPath(mapRuleField.getValueFromPath(), directReportData, session, forDirectReport);
	}

	String getDirectReportCompany(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, JSONException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Rule in DB to get company of direct Report
		// Before this rule isDirectReport must be mapped in order to check if usedID
		// provided in post body is exactly a direct Report of loggedIn user and to set
		// its data in session
		JSONObject requestObj = new JSONObject((String) session.getAttribute("requestData"));
		String directReportUserID = requestObj.getString("userID");
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		JSONObject directReportData = new JSONObject(
				(String) session.getAttribute("directReportData-" + directReportUserID));
		return getValueFromPath(mapRuleField.getValueFromPath(), directReportData, session, forDirectReport);
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
			tempHoldChildDirectReports = new JSONArray(getValueFromPath(childDirectReportsPath,
					parentDirectReportArray.getJSONObject(i), session, forDirectReport));
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

	String getTemplateName(String ruleID, HttpSession session, Boolean forDirectReport) {
		JSONObject rquestData = new JSONObject((String) session.getAttribute("requestData"));
		return templateService.findById(rquestData.getString("templateID")).get(0).getName();
	}

	String getFileType(String ruleID, HttpSession session, Boolean forDirectReport) {
		JSONObject rquestData = new JSONObject((String) session.getAttribute("requestData"));
		return rquestData.getString("fileType");
	}

	String getTemplatesFromAPI(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		if ((session.getAttribute("availableTemplatesInAzure") == null && !forDirectReport)
				|| forDirectReport == true) {
			List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
			JSONObject requestObj = new JSONObject();
			requestObj.put(mapRuleField.get(0).getKey(),
					getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport));
			requestObj.put(mapRuleField.get(1).getKey(),
					getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport));
			requestObj.put(mapRuleField.get(2).getKey(),
					getFieldValue(mapRuleField.get(2).getField(), session, forDirectReport));
			JSONObject apiResponse = null;
			apiResponse = new JSONObject(CommonFunctions
					.callpostAPI(getFieldValue(mapRuleField.get(3).getField(), session, forDirectReport), requestObj));
			Map<String, JSONObject> templatesAvailableInAzureMap = new HashMap<String, JSONObject>();
			JSONArray availableTemplates = apiResponse.getJSONArray("templates");
			JSONObject tempTemplateObject;
			for (int i = 0; i < availableTemplates.length(); i++) {
				tempTemplateObject = availableTemplates.getJSONObject(i);
				templatesAvailableInAzureMap.put(tempTemplateObject.getString("templateName"), tempTemplateObject);
			}
			if (!forDirectReport)
				session.setAttribute("availableTemplatesInAzure", templatesAvailableInAzureMap);
			else
				session.setAttribute("availableTemplatesForDirectReport", templatesAvailableInAzureMap);
		}
		return "";
	}

	String generateValueByConcatination(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Required to concatenate field Values and return single value
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		Iterator<MapRuleFields> iterator = mapRuleField.iterator();
		MapRuleFields tempMapRuleFields;
		String returnString = "";
		String fieldValue;
		while (iterator.hasNext()) {
			tempMapRuleFields = iterator.next();
			if (!(tempMapRuleFields.getKey() == null)) {
				fieldValue = getFieldValue(tempMapRuleFields.getField(), session, forDirectReport);
				returnString = fieldValue.equals("") ? returnString
						: returnString + fieldValue + tempMapRuleFields.getKey();
			} else {
				fieldValue = getFieldValue(tempMapRuleFields.getField(), session, forDirectReport);
				returnString = fieldValue.equals("") ? returnString : returnString + fieldValue;
			}
		}
		logger.debug("Concatinated Value: " + returnString);
		return returnString;
	}

	String formatDate(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Required to format dates

		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		String language = getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport);
		String dateToFormat = getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport);
		dateToFormat = dateToFormat.substring(dateToFormat.indexOf("(") + 1, dateToFormat.indexOf(")"));

		Date date;

		switch (language) {
		case "HUN":
			date = new Date(Long.parseLong(dateToFormat));
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			return (cal.get(Calendar.YEAR) + ". " + hunLocale.values()[cal.get(Calendar.MONTH)] + " "
					+ cal.get(Calendar.DAY_OF_MONTH));

		default:
			// works with default languages like: fr, en, sv, es, etc
			Locale locale = new Locale(language);
			date = new Date(Long.parseLong(dateToFormat));
			SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", locale);
			return (sdf.format(date));

		}
	}

	String checkForGreaterThen(String ruleID, HttpSession session, Boolean forDirectReport)
			throws NumberFormatException, BatchException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Required to check for Operation and return the result based on The mapped
		// fields
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		int greaterThen = Integer.parseInt(getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport));
		int checkInteger = Integer.parseInt(getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport));

		if (checkInteger >= greaterThen) {
			return getFieldValue(mapRuleField.get(2).getField(), session, forDirectReport);
		} else {
			return getFieldValue(mapRuleField.get(3).getField(), session, forDirectReport);
		}
	}

	String divideBy(String ruleID, HttpSession session, Boolean forDirectReport)
			throws NumberFormatException, BatchException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Required to get the result from operation
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		int divideBy_devisor = Integer
				.parseInt(getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport));
		int toBeDivided_dividant = Integer
				.parseInt(getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport));

		return Double.toString(toBeDivided_dividant / divideBy_devisor);
	}

	String formatYearPlusValue(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Required to format date and add one to the year
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		String language = getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport);
		String dateToFormat = getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport);
		dateToFormat = dateToFormat.substring(dateToFormat.indexOf("(") + 1, dateToFormat.indexOf(")"));

		Date date = new Date(Long.parseLong(dateToFormat));
		SimpleDateFormat sdf_YYYY = new SimpleDateFormat("yyyy");

		switch (language) {
		case "HUN":
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			return (Integer.parseInt(sdf_YYYY.format(date))
					+ Integer.parseInt(getFieldValue(mapRuleField.get(2).getField(), session, forDirectReport)) + ". "
					+ hunLocale.values()[11] + " " + 31);

		default:
			// works with default languages like: fr, en, sv, es, etc
			Date decMonth = new Date(1577786942000L);
			Locale locale = new Locale(language);
			SimpleDateFormat sdf_MMDD = new SimpleDateFormat("MMMM dd,", locale);
			return (sdf_MMDD.format(decMonth) + " " + (Integer.parseInt(sdf_YYYY.format(date))
					+ Integer.parseInt(getFieldValue(mapRuleField.get(2).getField(), session, forDirectReport))));
		}
	}
	/*
	 *** GET Rules END***
	 */

	/*
	 *** POST Rules Start***
	 */
	String getGroupsOfDirectReport(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB to get groups of a direct report

		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		/*
		 *** Security Check *** Checking if loggedIn user is a manager
		 *
		 */
		Boolean isManager = Boolean.parseBoolean(getFieldValue(mapRuleField.get(0).getField(), session, false));
		if (!isManager) {
			logger.error("Unauthorized access! User: " + (String) session.getAttribute("loggedInUser")
					+ " who is not a manager, Tried accessing groups of user: " + requestData.getString("userID"));
			return "You are not authorized to access this data! This event has been logged!";
		}

		/*
		 *** Security Check *** Checking if userID passed from UI is actually a direct
		 * report of the loggenIn user
		 */
		Boolean isDirectReport = Boolean.parseBoolean(getFieldValue(mapRuleField.get(1).getField(), session, false));

		if (!isDirectReport) {
			logger.error("Unauthorized access! User: " + (String) session.getAttribute("loggedInUser")
					+ " Tried accessing groups of user: " + requestData.getString("userID")
					+ ", which is not its direct report or level 2");// userID passed from UI
			return "You are not authorized to access this data! This event has been logged!";
		}
		String countryID = getFieldValue(mapRuleField.get(2).getField(), session, true);// forDirectReport true
		String companyID = getFieldValue(mapRuleField.get(3).getField(), session, true);// forDirectReport true
		Iterator<MapCountryCompanyGroup> iterator = mapCountryCompanyGroupService
				.findByCountryCompany(countryID, companyID, false).iterator();
		JSONArray response = new JSONArray();
		while (iterator.hasNext()) {
			response.put(iterator.next().toString());
		}
		return response.toString();
	}

	String getTemplates(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
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
		if (!groupAvailableCheck) {
			logger.error("Unauthorized access! User: " + (String) session.getAttribute("loggedInUser")
					+ " Tried accessing templates of group that is not available for this user. groupID: " + groupID);
			return "You are not authorized to access this data! This event has been logged!";
		}
		// get available Templates in Azure from Session
		@SuppressWarnings("unchecked")
		Map<String, JSONObject> templatesAvailableInAzure = (Map<String, JSONObject>) session
				.getAttribute("availableTemplatesInAzure");
		List<MapGroupTemplates> mapGroupTemplate = mapGroupTemplateService.findByGroupID(groupID);
		// Now Iterating for each template assigned to the provided group
		Iterator<MapGroupTemplates> iterator = mapGroupTemplate.iterator();
		String generatedCriteria;
		String templateID;
		List<Templates> tempTemplate;
		JSONArray response = new JSONArray();
		MapGroupTemplates tempMapGroupTemplate;
		JSONObject tempTemplateJsonObject;
		while (iterator.hasNext()) {
			tempMapGroupTemplate = iterator.next();

			// Generating criteria for each template to check if its valid for the loggedIn
			// user
			templateID = tempMapGroupTemplate.getTemplateID();
			generatedCriteria = generateCriteria(templateID, session, false); // forDirectReport false
			tempTemplate = templateService.findByIdAndCriteria(templateID, generatedCriteria);
			if (tempTemplate.size() > 0) {
				// check if the template is available in Azure
				if (!templatesAvailableInAzure.containsKey(tempMapGroupTemplate.getTemplate().getName())) {
					tempTemplateJsonObject = new JSONObject(tempTemplate.get(0).toString());
					tempTemplateJsonObject.put("availableInAzure", false);
					response.put(tempTemplateJsonObject.toString());
					continue;
				}
				response.put(tempTemplate.get(0).toString());
			}
		}
		return response.toString();
	}

	String getTemplatesOfDirectReports(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB to get templates of a direct report

		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);

		/*
		 *** Security Check *** Checking if loggedIn user is a manager
		 *
		 */
		Boolean isManager = Boolean.parseBoolean(getFieldValue(mapRuleField.get(0).getField(), session, false));
		String loggerInUser = (String) session.getAttribute("loggedInUser");
		if (!isManager) {
			logger.error("Unauthorized access! User: " + loggerInUser
					+ " who is not a manager, Tried accessing templates of user: " + requestData.getString("userID"));
			return "You are not authorized to access this data! This event has been logged!";
		}

		/*
		 *** Security Check *** Checking if userID passed from UI is actually a direct
		 * report of the loggenIn user
		 */
		Boolean isDirectReport = Boolean.parseBoolean(getFieldValue(mapRuleField.get(1).getField(), session, false));

		if (!isDirectReport) {
			logger.error("Unauthorized access! User: " + loggerInUser + " Tried accessing templates of a user: "
					+ requestData.getString("userID") + ", which is not its direct report or level 2");// userID passed
																										// from UI
			return "You are not authorized to access this data! This event has been logged!";
		}

		String directReportCountryID = getFieldValue(mapRuleField.get(2).getField(), session, true);// forDirectReport
																									// true
		String directReportCompanyID = getFieldValue(mapRuleField.get(3).getField(), session, true);// forDirectReport
																									// true
		getFieldValue(mapRuleField.get(4).getField(), session, true);// forDirectReport true

		/*
		 *** Security Check *** Checking if groupID passed from UI is actually available
		 * for the userID provided from the UI
		 */
		String groupID = requestData.getString("groupID");// groupID passed from UI
		Boolean groupAvailableCheck = mapCountryCompanyGroupService
				.findByGroupCountryCompany(groupID, directReportCountryID, directReportCompanyID, false).size() == 1
						? true
						: false;
		if (!groupAvailableCheck) {
			logger.error("Unauthorized access! User: " + loggerInUser
					+ " Tried accessing templates of group that is not available for user provided from UI userID:"
					+ requestData.getString("userID") + " groupID: " + groupID);
			return "You are not authorized to access this data! This event has been logged!";
		}

		// get available Templates in Azure from Session
		@SuppressWarnings("unchecked")
		Map<String, JSONObject> templatesAvailableInAzure = (Map<String, JSONObject>) session
				.getAttribute("availableTemplatesForDirectReport");

		// Now getting templates those are available for the userID provided from UI
		List<MapGroupTemplates> mapGroupTemplate = mapGroupTemplateService.findByGroupID(groupID);
		// Now Iterating for each template assigned to the provided group
		Iterator<MapGroupTemplates> iterator = mapGroupTemplate.iterator();
		String generatedCriteria;
		String templateID;
		List<Templates> tempTemplate;
		JSONArray response = new JSONArray();
		MapGroupTemplates tempMapGroupTemplate;
		JSONObject tempTemplateJsonObject;
		while (iterator.hasNext()) {
			tempMapGroupTemplate = iterator.next();

			// Generating criteria for each template to check if its valid for the loggedIn
			// user
			templateID = tempMapGroupTemplate.getTemplateID();
			generatedCriteria = generateCriteria(templateID, session, false); // forDirectReport false
			tempTemplate = templateService.findByIdAndCriteria(templateID, generatedCriteria);
			if (tempTemplate.size() > 0) {
				// check if the template is available in Azure
				if (!templatesAvailableInAzure.containsKey(tempMapGroupTemplate.getTemplate().getName())) {
					tempTemplateJsonObject = new JSONObject(tempTemplate.get(0).toString());
					tempTemplateJsonObject.put("availableInAzure", false);
					response.put(tempTemplateJsonObject.toString());
					continue;
				}
				response.put(tempTemplate.get(0).toString());
			}
		}
		return response.toString();
	}

	String docDownload(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, JSONException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Rule in DB to download doc

		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		String loggerInUser = (String) session.getAttribute("loggedInUser");
		String templateID = requestData.getString("templateID");

		/*
		 *** Security Check *** Checking if templateID passed from UI is actually
		 * available for the loggedIn user
		 */
		if (!templateAvailableCheck(ruleID, session, false)) { // for DirectReport false
			logger.error("Unauthorized access! User: " + loggerInUser
					+ " Tried downlaoding document of a template that is not assigned for this user, templateID: "
					+ templateID);
			return "You are not authorized to access this data! This event has been logged!";
		}
		// Now Generating Object to POST
		JSONObject docRequestObject = getDocPostObject(templateID, session, false); // for direct report false
		logger.debug("Doc Generation Request Obj: " + docRequestObject.toString());
		return getDocFromAPI(docRequestObject);
	}

	String docDownloadDirectReport(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB to download doc of Direct report

		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		Boolean isManager = Boolean.parseBoolean(getFieldValue(mapRuleField.get(3).getField(), session, false));
		String loggerInUser = (String) session.getAttribute("loggedInUser");
		String userID = requestData.getString("userID");
		String templateID = requestData.getString("templateID");

		/*
		 *** Security Check *** Checking if loggedIn user is a manager
		 *
		 */
		if (!isManager) {
			logger.error("Unauthorized access! User: " + loggerInUser
					+ " who is not a manager, Tried downloading doc for user: " + requestData.getString("userID"));
			return "You are not authorized to access this data! This event has been logged!";
		}

		/*
		 *** Security Check *** Checking if userID passed from UI is actually a direct
		 * report of the loggenIn user
		 */
		Boolean isDirectReport = Boolean.parseBoolean(getFieldValue(mapRuleField.get(2).getField(), session, false));

		if (!isDirectReport) {
			logger.error("Unauthorized access! User: " + loggerInUser + " Tried downloading doc of a user: " + userID
					+ ", which is not its direct report or level 2");// userID passed from UI
			return "You are not authorized to access this data! This event has been logged!";
		}

		/*
		 *** Security Check *** Checking if templateID passed from UI is actually
		 * available for the userID provided
		 */
		if (!templateAvailableCheck(ruleID, session, true)) {// for direct Report true
			logger.error("Unauthorized access! User: " + loggerInUser + " Tried downlaoding doc of the user: " + userID
					+ " and template: " + templateID + " which is not assigned for this user");
			return "You are not authorized to access this data! This event has been logged!";
		}
		// Now Generating Object to POST
		JSONObject docRequestObject = getDocPostObject(templateID, session, true);// for direct Report true
		logger.debug("Doc Generation Request Obj: " + docRequestObject.toString());
		return getDocFromAPI(docRequestObject);
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

			// checking if a default value is there in field
			if (field.getDefaultValue() != null)
				return field.getDefaultValue();

			JSONObject entityData;
			Entities entity = field.getEntity();
			logger.debug("EntityName: " + entity.getName() + " For Field: " + field.getTechnicalName());
			entity = checkForDependantEntity(entity); // Check for root entity and get root entity if current entity
														// is dependent on some other entity

			// now entity variable will be having the root entity from which will get the
			// data of our field
			entityData = getEntityData(entity, session, forDirectReport);
			return getValueFromPath(field.getValueFromPath(), entityData, session, forDirectReport);
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
		Map<String, String> entityMap = new HashMap<String, String>();
		BatchRequest batchRequest = new BatchRequest();
		batchRequest.configureDestination(CommonVariables.sfDestination);
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
				// Generating Unique ID pep user and Entity
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

	private String getValueFromPath(String path, JSONObject retriveFromObj, HttpSession session,
			Boolean forDirectReport) throws JSONException, BatchException, ClientProtocolException,
			UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NamingException, URISyntaxException, IOException {
		logger.debug("Fetching value from Path: " + path);
		logger.debug("Object from which value need to be fetched: " + retriveFromObj.toString());
		String[] pathArray = path.split("/");
		JSONObject currentObject = retriveFromObj;
		String value = null;
		for (String key : pathArray) {
			if (key.endsWith("\\0") && currentObject != null) {// then value is at this location
				value = key.substring(key.length() - 4, key.length() - 3).equals("*") // Checking if complete array is
																						// required in output
						? currentObject.getJSONArray(key.substring(0, key.length() - 5)).toString()
						: key.substring(key.length() - 3, key.length() - 2).equals("]") ? // Checking if single complete
																							// object need to be picked
																							// from the array
								currentObject.getJSONArray(key.substring(0, key.length() - 5))
										.getJSONObject(Integer
												.parseInt(key.substring(key.indexOf('[') + 1, key.indexOf('[') + 2)))
										.toString()
								: currentObject.has(key.substring(0, key.length() - 2))// checking if object containing
																						// the key or not, if not then
																						// send "" back
										? currentObject.get(key.substring(0, key.length() - 2)).toString()
												.equals("null") ? "null"
														: currentObject.getString(key.substring(0, key.length() - 2))
										: "";
			} else if (key.endsWith("]") && currentObject != null) { // in case of array get the indexed Object

				JSONArray tempArray = null;
				if (key.contains("?")) {
					tempArray = currentObject.getJSONArray(key.substring(0, key.indexOf('~')));

					String keyToSearchInEachObj = key.substring(key.indexOf("~SearchForKey~") + 14,
							key.indexOf("~FieldID~"));
					String fieldID = key.substring(key.indexOf("~FieldID~") + 9, key.indexOf('['));
					System.out.println("FieldID: " + fieldID + " Key: " + keyToSearchInEachObj);
					String valueToSearch = getFieldValue(fieldsService.findByID(fieldID).get(0), session,
							forDirectReport);
					JSONObject tempJsonObj;
					for (int i = 0; i < tempArray.length(); i++) {
						tempJsonObj = tempArray.getJSONObject(i);
						if (tempJsonObj.getString(keyToSearchInEachObj).equals(valueToSearch)) {
							currentObject = tempJsonObj;
						}
					}
				} else {
					int index = key.indexOf('[');
					index = Integer.parseInt(key.substring(index + 1, index + 2)); // to get the index between []
					tempArray = currentObject.getJSONArray(key.substring(0, key.length() - 3));
					System.out.println(tempArray.toString());
					currentObject = tempArray.length() > 0 ? tempArray.getJSONObject(index) : null;
				}
			} else if (currentObject != null) {// in case of Obj
				currentObject = currentObject.has(key) ? currentObject.getJSONObject(key) : null;
			}
		}
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
		batchRequest.configureDestination(CommonVariables.sfDestination);
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

	private Boolean templateAvailableCheck(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, JSONException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		JSONArray availableTemplates;
		JSONArray availableGroups = new JSONArray(
				getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport));
		logger.debug("Available Groups:" + availableGroups.toString() + " ::: forDirectReport" + forDirectReport);
		String groupID;
		for (int i = 0; i < availableGroups.length(); i++) {
			// saving group ID in Session requestData attribute as its expected in Get
			// Templates function
			groupID = new JSONObject(availableGroups.getString(i)).getString("id");
			requestData.put("groupID", groupID);
			session.setAttribute("requestData", requestData.toString());
			availableTemplates = new JSONArray(getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport));
			logger.debug(
					"Available templates:" + availableTemplates.toString() + " ::: forDirectReport" + forDirectReport);
			for (int j = 0; j < availableTemplates.length(); j++) {
				if (requestData.getString("templateID")
						.equals(new JSONObject(availableTemplates.getString(j)).getString("id"))) {
					return true;
				}
			}
		}
		return false;
	}

	private JSONObject getDocPostObject(String templateID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Function to generate POST object for DocGeneration

		JSONObject docPostObject = new JSONObject();
		MapTemplateFields mapTemplateField;
		Iterator<MapTemplateFields> iterator = mapTemplateFieldsService.findByTemplateID(templateID).iterator();
		while (iterator.hasNext()) {
			mapTemplateField = iterator.next();
			JSONObject objToPlace = new JSONObject();
			objToPlace.put("Key", mapTemplateField.getTemplateFieldName());
			objToPlace.put("Value", getFieldValue(mapTemplateField.getField(), session, forDirectReport));
			// To place value at specific location in POST object
			docPostObject = placeValue(objToPlace, mapTemplateField.getPlaceFieldAtPath(), docPostObject);
		}
		return docPostObject;
	}

	private JSONObject placeValue(JSONObject objToPlace, String placeAtPath, JSONObject placeAt) {
		// Function to place value at specific location in POST object

		String[] pathArray = placeAtPath.split("/");
		for (String key : pathArray) {
			// Only two cases are handled
			// 1. If the value needs to be placed inside Parameters array
			// 2. If the value need to be placed directly in the root object.
			if (key.endsWith("]\\0")) {
				if (placeAt.has(key.substring(0, key.length() - 5)))
					placeAt.getJSONArray(key.substring(0, key.length() - 5)).put(objToPlace);
				else
					placeAt.put(key.substring(0, key.length() - 5), new JSONArray().put(objToPlace));

			} else if (key.endsWith("\\0")) {
				placeAt.put(objToPlace.getString("Key"), objToPlace.getString("Value"));
			}
		}
		return placeAt;
	}

	private String getDocFromAPI(JSONObject requestObj)
			throws URISyntaxException, NamingException, ParseException, IOException {
		DestinationClient docDestination = new DestinationClient();
		docDestination.setDestName(CommonVariables.docGenDestination);
		docDestination.setHeaderProvider();
		docDestination.setConfiguration();
		docDestination.setDestConfiguration();
		docDestination.setHeaders(docDestination.getDestProperty("Authentication"));

		HttpResponse docResponse = docDestination.callDestinationPOST("", "", requestObj.toString());
		if (docResponse.getStatusLine().getStatusCode() != 200) {
			logger.debug("Error while fetching document from API, response from API: Response Status code: "
					+ docResponse.getStatusLine().getStatusCode() + " ::: Response: "
					+ EntityUtils.toString(docResponse.getEntity(), "UTF-8"));
			return "Error while generating Doc";
		}
		return EntityUtils.toString(docResponse.getEntity(), "UTF-8");
	}

	/*
	 *** Helper functions END***
	 */
}
