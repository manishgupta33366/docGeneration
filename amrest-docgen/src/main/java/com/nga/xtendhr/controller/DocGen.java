package com.nga.xtendhr.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
					? ResponseEntity.ok().body(getRuleData(ruleID, session).toString())
					: new ResponseEntity<>("Session timeout! Please Login again!", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/getTemplates")
	public ResponseEntity<?> getTemplates(@RequestParam(name = "groupID") String groupId, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession(false); // false is not create new session and use the existing
																// session
			if (session == null) {
				return new ResponseEntity<>("Session timeout! Please Login again!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			// get all the templates assigned to the group
			List<MapGroupTemplates> mapGroupTemplate = mapGroupTemplateService.findByGroupID(groupId);

			// Now Iterating for each template assigned to the provided group
			Iterator<MapGroupTemplates> iterator = mapGroupTemplate.iterator();
			String generatedCriteria;
			String templateID;
			List<Templates> template;
			Templates tempTemplate;
			List<Templates> response = new ArrayList<Templates>();
			while (iterator.hasNext()) {
				templateID = iterator.next().getTemplateID();
				// Generating criteria for each template
				generatedCriteria = generateCriteria(templateID, session);
				template = templateService.findByIdAndCriteria(templateID, generatedCriteria);
				tempTemplate = template.size() > 0 ? template.get(0) : null;
				if (tempTemplate != null) {
					response.add(tempTemplate);
				}
			}
			return ResponseEntity.ok().body(response);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String generateCriteria(String templateID, HttpSession session)
			throws NamingException, BatchException, ClientProtocolException, UnsupportedOperationException,
			URISyntaxException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		List<TemplateCriteriaGeneration> templateCriteriaGeneration = templateCriteriaGenerationService
				.findByTemplateID(templateID); // This will get fields IDs required to generate criteria

		// Now Iterating for each field mapped for Criteria generation
		Iterator<TemplateCriteriaGeneration> iterator = templateCriteriaGeneration.iterator();
		String criteria = "";
		while (iterator.hasNext()) {
			criteria = criteria + getFieldValue(iterator.next().getField(), session) + "|";
		}
		criteria = criteria.length() > 0 ? criteria.substring(0, criteria.length() - 1) : "";
		logger.debug("criteria: " + criteria + " for  templateID: " + templateID);
		return criteria;
	}

	private String getFieldValue(Fields field, HttpSession session) throws BatchException, ClientProtocolException,
			UnsupportedOperationException, NamingException, URISyntaxException, IOException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		logger.debug("Getting value for Field: " + field.getTechnicalName() + " ... RuleID: " + field.getRuleID());
		if (field.getRuleID() == null) {
			JSONObject entityData;
			Entities entity = field.getEntity();
			logger.debug("EntityName: " + entity.getName() + " For GetFieldValue Field: " + field.getTechnicalName());
			entity = checkForDependantEntity(entity); // Check for root entity and get root entity if current entity is
														// dependent on some other entity
			// now entity variable will be having the root entity from which will get the
			// data of our field
			entityData = getEntityData(entity, session);
			return getValueFromPath(field.getValueFromPath(), entityData);
		}
		// Calling function dynamically
		// more Info here: https://www.baeldung.com/java-method-reflection
		Method method = this.getClass().getDeclaredMethod(field.getRule().getName(), String.class, HttpSession.class);
		return (String) method.invoke(this, field.getRuleID(), session);
	}

	private Entities checkForDependantEntity(Entities entity) { // function to get the root entity
		if (!entity.getIsDependant()) { // check if the entity is the root entity
			return entity; // return the root entity
		}
		// If not call checkForDependantEntity i.e. recursively with the dependent
		// Entity
		return checkForDependantEntity(entity.getDependantOnEntity());
	}

	private JSONObject getEntityData(Entities entity, HttpSession session) throws NamingException, BatchException,
			ClientProtocolException, UnsupportedOperationException, URISyntaxException, IOException {
		// function to get data of the root entity
		JSONObject entityData;
		String entityName = entity.getName();
		if (session.getAttribute(entityName) != null) { // Check if entity data is present in the Session
			entityData = new JSONObject((String) session.getAttribute(entityName));
			logger.debug("Data fetched from session for Entity: " + entityName);
			return entityData;
		} // else retrieve data from SF
		entityData = fetchDataFromSF(entity, session);
		logger.debug("Data fetched from SF for entity: " + entityName);
		return entityData;
	}

	private JSONObject fetchDataFromSF(Entities entity, HttpSession session) throws NamingException, BatchException,
			ClientProtocolException, UnsupportedOperationException, URISyntaxException, IOException {
		// Calling SF URL to fetch Entity data
		String loggedInUser = (String) session.getAttribute("loggedInUser");
		String entityName = entity.getName();

		loggedInUser = loggedInUser.equals("S0014379281") || loggedInUser.equals("S0018269301")
				|| loggedInUser.equalsIgnoreCase("S0019013022") ? "E00001047" : loggedInUser;
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

		entityMap.put(entity.getName(), "?$filter=" + entity.getFilter() + " eq '" + loggedInUser
				+ "'&$format=json&$expand=" + expandPath + "&$select=" + selectPath);
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
			session.setAttribute(entityName, response);
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

	JSONObject getRuleData(String ruleID, HttpSession session) throws BatchException, ClientProtocolException,
			UnsupportedOperationException, NamingException, URISyntaxException, IOException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// This function will create jsonObject for a particular rule
		List<MapRuleFields> mapRuleFields = mapRuleFieldsService.findByRuleID(ruleID);
		MapRuleFields mapRuleField;
		JSONObject responseObj = new JSONObject();
		Fields field;
		Iterator<MapRuleFields> iterator = mapRuleFields.iterator();
		while (iterator.hasNext()) {
			mapRuleField = iterator.next();
			field = mapRuleField.getField();
			responseObj.put(field.getTechnicalName(), getFieldValue(field, session));
		}
		return responseObj;
	}

	String checkIfManager(String ruleID, HttpSession session) throws BatchException, ClientProtocolException,
			UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Rule in DB
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		return new JSONArray(getFieldValue(mapRuleField.getField(), session)).length() > 0 ? "true" : "false";
	}

	String getGroups(String ruleID, HttpSession session) throws BatchException, ClientProtocolException,
			UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Rule in DB
		JSONObject ruleData = getRuleData(ruleID, session);
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
}
