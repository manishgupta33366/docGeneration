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
import java.util.concurrent.TimeUnit;

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
import com.nga.xtendhr.model.CodelistText;
import com.nga.xtendhr.model.CountrySpecificFields;
import com.nga.xtendhr.model.Entities;
import com.nga.xtendhr.model.Fields;
import com.nga.xtendhr.model.MapCountryCompanyGroup;
import com.nga.xtendhr.model.MapGroupTemplates;
import com.nga.xtendhr.model.MapRuleFields;
import com.nga.xtendhr.model.MapTemplateCriteriaValues;
import com.nga.xtendhr.model.MapTemplateFields;
import com.nga.xtendhr.model.Rules;
import com.nga.xtendhr.model.TemplateFieldTag;
import com.nga.xtendhr.model.Templates;
import com.nga.xtendhr.model.Text;
import com.nga.xtendhr.service.CodelistService;
import com.nga.xtendhr.service.CodelistTextService;
import com.nga.xtendhr.service.CountrySpecificFieldsService;
import com.nga.xtendhr.service.EntitiesService;
import com.nga.xtendhr.service.FieldsService;
import com.nga.xtendhr.service.MapCountryCompanyGroupService;
import com.nga.xtendhr.service.MapGroupTemplatesService;
import com.nga.xtendhr.service.MapRuleFieldsService;
import com.nga.xtendhr.service.MapTemplateCriteriaValuesService;
import com.nga.xtendhr.service.MapTemplateFieldsService;
import com.nga.xtendhr.service.RulesService;
import com.nga.xtendhr.service.SFDataMappingService;
import com.nga.xtendhr.service.TemplateService;
import com.nga.xtendhr.service.TextService;
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

	@Autowired
	CodelistTextService codelistTextService;

	@Autowired
	CodelistService codelistService;

	@Autowired
	CountrySpecificFieldsService countrySpecificFieldsService;

	@Autowired
	SFDataMappingService sFDataMappingService;

	@Autowired
	MapTemplateCriteriaValuesService mapTemplateCriteriaValuesService;

	@Autowired
	TextService textService;

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

			if (CommonFunctions.checkIfAdmin(loggedInUser)) {
				session.setAttribute("adminLoginStatus", "Success");
				response.put("isAdmin", true);
			}
			session.setAttribute("locale", getLocale(session));
			return ResponseEntity.ok().body(response.toString());// True to create a new session for the logged-in user
																	// as its the initial call
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/cleanSession")
	public ResponseEntity<?> cleanSession(HttpServletRequest request) {
		try {
			HttpSession session = request.getSession(true);
			if (session != null) {
				session.invalidate();
			}
			return ResponseEntity.ok().body(true);
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
			session.setAttribute("requestData", requestData); // Saving groups in session as its required in
																// checkAvailable Templates Function
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
			return ResponseEntity.ok().body(searchResponseResponseObject.getJSONObject("d").toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<?> adminOnSearchUserSelect(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, JSONException, ClientProtocolException, UnsupportedOperationException,
			NamingException, URISyntaxException, IOException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		try {
			/*
			 * Rule required to get data for a specific User (Accessible only by Admin)
			 */
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

			JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
			String userID = requestData.getString("userID");// userID passed from UI
			List<MapRuleFields> mapRuleFields = mapRuleFieldsService.findByRuleID(ruleID);
			Iterator<MapRuleFields> mapRuleFieldItr = mapRuleFields.iterator();
			String url;

			MapRuleFields tempMapRuleField;
			JSONObject responseObj = new JSONObject();
			String fieldValue;
			JSONArray fieldsArray;
			Fields field;
			while (mapRuleFieldItr.hasNext()) {
				tempMapRuleField = mapRuleFieldItr.next();
				url = tempMapRuleField.getUrl();// URL
				url = url.replaceFirst("<>", userID);// UserId passed from UI
				// all fields based on single entity are saved in a form of array
				fieldsArray = new JSONArray(tempMapRuleField.getFieldID());

				// Entity name saved in KEY column
				JSONObject response = new JSONObject(callSFSingle(tempMapRuleField.getKey(), url));
				for (int i = 0; i < fieldsArray.length(); i++) {
					field = fieldsService.findByID(fieldsArray.getString(i)).get(0);// get field from the fields table
					fieldValue = getValueFromPath(field.getValueFromPath(), response, session, false, null);
					responseObj.put(field.getTechnicalName(), fieldValue);
				}

			}
			return ResponseEntity.ok().body(responseObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/docGenAdmin/executePostCallRule")
	public ResponseEntity<?> executeRule(@RequestParam(name = "ruleID") String ruleID, @RequestBody String requestData,
			HttpServletRequest request) {
		// rule to Post data from UI to a API
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

			if (mapRuleField.getCallUsingJWT()) {
				CommonFunctions commonFunctions = new CommonFunctions();
				return ResponseEntity.ok().body(commonFunctions.callpostAPIWithJWT(mapRuleField.getUrl(),
						new JSONObject(requestData), mapRuleField.getDestinationName()));
			} else
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
		getFieldValue(mapRuleField.get(0).getField(), session, true, null);// get data of direct report
		String countryID = getFieldValue(mapRuleField.get(1).getField(), session, true, null);// forDirectReport true
		String companyID = getFieldValue(mapRuleField.get(2).getField(), session, true, null);// forDirectReport true
		if (countryID.equals("") || companyID.equals(""))
			return "";
		Iterator<MapCountryCompanyGroup> iterator = mapCountryCompanyGroupService
				.findByCountryCompanyAdmin(countryID, companyID).iterator();
		JSONArray response = new JSONArray();
		String locale = (String) session.getAttribute("locale");
		MapCountryCompanyGroup tempMapCountryCompanyGroup;
		List<Text> tempTextList;
		JSONObject tempMapCountryCompanyGroupObj;
		while (iterator.hasNext()) {
			tempMapCountryCompanyGroup = iterator.next();
			tempMapCountryCompanyGroupObj = new JSONObject(tempMapCountryCompanyGroup.toString());
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getCountryID(), locale);
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("country_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("country_description_per_Locale",
						tempTextList.get(0).getDescription());
			}
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getCompanyID(), locale);
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("company_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("company_description_per_Locale",
						tempTextList.get(0).getDescription());
			}
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getGroupID(), locale);
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("group_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("group_description_per_Locale", tempTextList.get(0).getDescription());
			}
			response.put(tempMapCountryCompanyGroupObj);
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
		getFieldValue(mapRuleField.get(0).getField(), session, true, null);// get data of direct report
		String directReportCountryID = getFieldValue(mapRuleField.get(1).getField(), session, true, null);// forDirectReport
																											// true
		String directReportCompanyID = getFieldValue(mapRuleField.get(2).getField(), session, true, null);// forDirectReport
																											// true
		getFieldValue(mapRuleField.get(3).getField(), session, true, null);// get Templates from Azure and set that
																			// in
																			// session and forDirectReport true

		/*
		 *** Security Check *** Checking if groupID passed from UI is actually available
		 * for the userID provided from the UI
		 */
		JSONArray groupIdArray = requestData.getJSONArray("groupID");
		JSONObject response = new JSONObject();
		String groupID;
		for (int i = 0; i < groupIdArray.length(); i++) {
			groupID = groupIdArray.getString(i);// groupID passed from UI

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
			Boolean criteriaSatisfied;
			String templateID;
			Templates tempTemplate;
			JSONArray tempResponse = new JSONArray();
			MapGroupTemplates tempMapGroupTemplate;

			JSONObject tempTemplateObj;
			List<Text> tempTextList;
			String locale = (String) session.getAttribute("locale");
			while (iterator.hasNext()) {
				tempMapGroupTemplate = iterator.next();
				tempTemplate = tempMapGroupTemplate.getTemplate();
				tempTemplateObj = new JSONObject(tempTemplate.toString()); // object to save localeData and pass to
																			// response array
				tempTextList = textService.findByRefrencedIdLocale(tempTemplate.getId(), locale);// fetching locale data
																									// of
				// template
				if (tempTextList.size() > 0) {
					tempTemplateObj.put("template_text_per_Locale", tempTextList.get(0).getText());
					tempTemplateObj.put("template_description_per_Locale", tempTextList.get(0).getDescription());
				}
				// Generating criteria for each template to check if its valid for the loggedIn
				// user
				templateID = tempMapGroupTemplate.getTemplateID();
				criteriaSatisfied = checkCriteria(templateID, session, true); // forDirectReport true
				if (criteriaSatisfied) {
					// check if the template is available in Azure
					if (!templatesAvailableInAzure.containsKey(tempMapGroupTemplate.getTemplate().getName())) {
						tempTemplateObj.put("availableInAzure", false);
						tempResponse.put(tempTemplateObj);
						continue;
					}
					tempResponse.put(tempTemplateObj);
				}
			}
			response.put(groupID, tempResponse);
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
		String locale = (String) session.getAttribute("locale");
		MapCountryCompanyGroup tempMapCountryCompanyGroup;
		List<Text> tempTextList;
		JSONObject tempMapCountryCompanyGroupObj;
		while (iterator.hasNext()) {
			tempMapCountryCompanyGroup = iterator.next();
			tempMapCountryCompanyGroupObj = new JSONObject(tempMapCountryCompanyGroup.toString());
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getCountryID(), locale);
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("country_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("country_description_per_Locale",
						tempTextList.get(0).getDescription());
			}
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getCompanyID(), locale);
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("company_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("company_description_per_Locale",
						tempTextList.get(0).getDescription());
			}
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getGroupID(), locale);
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("group_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("group_description_per_Locale", tempTextList.get(0).getDescription());
			}
			response.put(tempMapCountryCompanyGroupObj);
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
		// Entity name saved in KEY column
		JSONArray responseArray = new JSONObject(callSFSingle(mapRuleField.getKey(), url)).getJSONArray("results");
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
					+ " Tried downloading document of a template templateID: " + templateID
					+ " Which is not available for the UserId provided.");
			return "You are not authorized to access this data! This event has been logged!";
		}
		// Removing all the entities data from the session for Hard Reload of data from
		// SF
		List<String> distinctEntityNames = entitiesService.getDistinctNames();
		Iterator<String> entityNamesItr = distinctEntityNames.iterator();
		while (entityNamesItr.hasNext()) {
			session.removeAttribute("directReportEntities-" + requestData.getString("userID") + entityNamesItr.next());
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
					+ " Tried downloading document of a template that is not assigned for this user, templateID: "
					+ templateID);
			return "You are not authorized to access this data! This event has been logged!";
		}

		// Removing all the entities data from the session for Hard Reload of data from
		// SF
		List<String> distinctEntityNames = entitiesService.getDistinctNames();
		Iterator<String> entityNamesItr = distinctEntityNames.iterator();
		while (entityNamesItr.hasNext()) {
			session.removeAttribute(entityNamesItr.next());
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

		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));

		JSONArray groupIdArray = requestData.getJSONArray("groupID");
		JSONObject response = new JSONObject();
		String groupID;
		for (int i = 0; i < groupIdArray.length(); i++) {
			groupID = groupIdArray.getString(i);// groupID passed from UI

			Boolean groupAvailableCheck = mapCountryCompanyGroupService
					.findByGroupCountryCompany(groupID, countryID, companyID).size() == 1 ? true : false;
			if (!groupAvailableCheck) {
				logger.error("Unauthorized access! User: " + (String) session.getAttribute("loggedInUser")
						+ " Tried accessing templates of group that is not available for this user. groupID: "
						+ groupID);
				return "You are not authorized to access this data! This event has been logged!";
			}
			// get available Templates in Azure from Session
			@SuppressWarnings("unchecked")
			Map<String, JSONObject> templatesAvailableInAzure = (Map<String, JSONObject>) session
					.getAttribute("availableTemplatesInAzure");
			List<MapGroupTemplates> mapGroupTemplate = mapGroupTemplateService.findByGroupID(groupID);
			// Now Iterating for each template assigned to the provided group
			Iterator<MapGroupTemplates> iterator = mapGroupTemplate.iterator();
			Boolean criteriaSatisfied;
			String templateID;
			Templates tempTemplate;
			JSONArray tempResponse = new JSONArray();
			MapGroupTemplates tempMapGroupTemplate;
			JSONObject tempTemplateObj;
			List<Text> tempTextList;
			String locale = (String) session.getAttribute("locale");
			while (iterator.hasNext()) {
				tempMapGroupTemplate = iterator.next();
				tempTemplate = tempMapGroupTemplate.getTemplate();
				tempTemplateObj = new JSONObject(tempTemplate.toString()); // object to save localeData and pass to
																			// response array
				tempTextList = textService.findByRefrencedIdLocale(tempTemplate.getId(), locale);// fetching locale data
																									// of
				// template
				if (tempTextList.size() > 0) {
					tempTemplateObj.put("template_text_per_Locale", tempTextList.get(0).getText());
					tempTemplateObj.put("template_description_per_Locale", tempTextList.get(0).getDescription());
				}
				// Generating criteria for each template to check if its valid for the loggedIn
				// user
				templateID = tempMapGroupTemplate.getTemplateID();
				criteriaSatisfied = checkCriteria(templateID, session, false); // forDirectReport false
				if (criteriaSatisfied) {
					// check if the template is available in Azure
					if (!templatesAvailableInAzure.containsKey(tempMapGroupTemplate.getTemplate().getName())) {
						tempTemplateObj.put("availableInAzure", false);
						tempResponse.put(tempTemplateObj);
						continue;
					}
					tempResponse.put(tempTemplateObj);
				}
			}
			response.put(groupID, tempResponse);
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
				getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport, null));
		logger.debug("Available Groups:" + availableGroups.toString() + " ::: forDirectReport" + forDirectReport);
		String groupID;
		JSONObject tempAvailableTemplatesObj;
		for (int i = 0; i < availableGroups.length(); i++) {
			// saving group ID in Session requestData attribute as its expected in Get
			// Templates function
			groupID = availableGroups.getJSONObject(i).getString("id");
			requestData.put("groupID", new JSONArray().put(groupID));
			session.setAttribute("requestData", requestData.toString());
			tempAvailableTemplatesObj = new JSONObject(
					getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport, null));// Object of
																									// Available
																									// Templates for the
																									// groups
			availableTemplates = tempAvailableTemplatesObj.getJSONArray(groupID);
			logger.debug(
					"Available templates:" + availableTemplates.toString() + " ::: forDirectReport" + forDirectReport);
			for (int j = 0; j < availableTemplates.length(); j++) {
				if (requestData.getString("templateID").equals(availableTemplates.getJSONObject(j).getString("id"))) {
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

	public ResponseEntity<?> searchUser(String ruleID, HttpSession session, Boolean forDirectReport)
			throws ClientProtocolException, IOException, URISyntaxException, NamingException {

		// rule to search a user on UI, will work for both Admin and Manager
		try {

			/*
			 *** Security Check *** Checking if user trying to login is exactly an
			 * Admin/Manager or not
			 *
			 */
			MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
			Boolean isManager = Boolean.parseBoolean(getFieldValue(mapRuleField.getField(), session, false, null)); // For
																													// directReport
																													// false
			logger.debug("Search User Rule: isManager: " + isManager);
			if (session.getAttribute("adminLoginStatus") == null && !isManager) {
				logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
						+ ", which is not an admin or a Manager in SF, tried to search a user.");
				return new ResponseEntity<>(
						"Error! You are not authorized to access this resource! This event has been logged!",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));

			HttpResponse searchResponse;
			String searchResponseJsonString;
			JSONObject searchResponseResponseObject;
			if (!requestData.has("searchString")) { // searchString is not sent from UI
				searchResponse = CommonFunctions.getDestinationCLient(CommonVariables.sfDestination).callDestinationGET(
						"/User",
						"?$format=json&$select=userId,firstName,lastName&$filter=firstName ne null and lastName ne null");
			} else { // searchString is passed from UI
				String searchString = requestData.getString("searchString");// searchString passed from UI
				searchString = searchString.toLowerCase();
				String url = "?$format=json&$select=userId,firstName,lastName&$filter=substringof('<inputParameter>',tolower(firstName)) or substringof('<inputParameter>',tolower(lastName)) or substringof('<inputParameter>',tolower(userId))";
				url = url.replace("<inputParameter>", searchString);
				searchResponse = CommonFunctions.getDestinationCLient(CommonVariables.sfDestination)
						.callDestinationGET("/User", url);
			}
			searchResponseJsonString = EntityUtils.toString(searchResponse.getEntity(), "UTF-8");
			searchResponseResponseObject = new JSONObject(searchResponseJsonString);
			return ResponseEntity.ok().body(searchResponseResponseObject.getJSONObject("d").toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	String getSelectedUserDetails(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {

		// rule to details of a user on UI, will work for both Admin and Manager
		/*
		 *** Security Check *** Checking if user trying to login is exactly an
		 * Admin/Manager or not
		 *
		 */
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		Boolean isManager = Boolean.parseBoolean(getFieldValue(mapRuleField.getField(), session, false, null)); // For
																												// directReport
																												// false
		logger.debug("getSelectedUserDetails Rule: isManager: " + isManager);
		if (session.getAttribute("adminLoginStatus") == null && !isManager) {
			logger.error("Unauthorized access! User:" + (String) session.getAttribute("loggedInUser")
					+ ", which is not an admin or a Manager in SF, tried to search a user.");
			return "Error! You are not authorized to access this resource! This event has been logged!";
		}
		JSONObject ruleData = getRuleData(ruleID, session, true);
		return (ruleData.toString());

	}

	String calculateAge(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB required to get age
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		String dob = getFieldValue(mapRuleField.getField(), session, forDirectReport, null);
		String dobms = dob.substring(dob.indexOf("(") + 1, dob.indexOf(")"));
		Date dobDate = new Date(Long.parseLong(dobms));
		Date today = new Date();

		long diffInMillies = Math.abs(today.getTime() - dobDate.getTime());
		long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
		return Long.toString(diff / 365);
	}

	String formatCurrentDate(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB Required to format dates
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		String language = getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport, null);
		Calendar cal = Calendar.getInstance();
		long dateToFormat = cal.getTimeInMillis(); // current date and time
		Date date;
		Locale locale;
		SimpleDateFormat sdf;
		String seprator1 = mapRuleField.get(0).getKey();
		String seprator2 = mapRuleField.get(1).getKey();
		switch (language) { // switch for custom or default date format
		case "HUN":
			date = new Date(dateToFormat);
			cal.setTime(date);
			return (cal.get(Calendar.YEAR) + seprator1 + hunLocale.values()[cal.get(Calendar.MONTH)] + seprator2
					+ cal.get(Calendar.DAY_OF_MONTH));

		case "DE":
			locale = new Locale(language); // as for DE required format is DD.MM.YYYY and our default is MMMM dd, yyyy
			date = new Date(dateToFormat);
			sdf = new SimpleDateFormat("dd" + seprator1 + "MM" + seprator2 + "yyyy", locale);
			return (sdf.format(date));

		default:
			// works with default languages like: fr, en, sv, es, de, etc
			locale = new Locale(language);
			date = new Date(dateToFormat);
			sdf = new SimpleDateFormat("MMMM dd, yyyy", locale);
			return (sdf.format(date));
		}
	}

	String checkIfManager(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB required to check if current loggenIn user is a manager
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		JSONArray directReports = new JSONArray(getFieldValue(mapRuleField.getField(), session, forDirectReport, null));
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
		String locale = (String) session.getAttribute("locale");
		MapCountryCompanyGroup tempMapCountryCompanyGroup;
		List<Text> tempTextList;
		JSONObject tempMapCountryCompanyGroupObj;
		while (iterator.hasNext()) {
			tempMapCountryCompanyGroup = iterator.next();
			tempMapCountryCompanyGroupObj = new JSONObject(tempMapCountryCompanyGroup.toString());
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getCountryID(), locale);
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("country_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("country_description_per_Locale",
						tempTextList.get(0).getDescription());
			}
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getCompanyID(), locale);
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("company_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("company_description_per_Locale",
						tempTextList.get(0).getDescription());
			}
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getGroupID(), locale);
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("group_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("group_description_per_Locale", tempTextList.get(0).getDescription());
			}
			logger.debug("tempMapCountryCompanyGroupObj: " + tempMapCountryCompanyGroupObj.toString());
			response.put(tempMapCountryCompanyGroupObj);
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

		JSONArray responseArray = new JSONObject(callSFSingle(mapRuleField.getKey(), url)).getJSONArray("results");
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
		return getValueFromPath(mapRuleField.getValueFromPath(), directReportData, session, forDirectReport, null);
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
		return getValueFromPath(mapRuleField.getValueFromPath(), directReportData, session, forDirectReport, null);
	}

	String getDirectReports(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, JSONException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Rule in DB to get direct report (2 levels) of the loggedIn User

		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		// getting the Parent/root array containing directReports
		JSONArray parentDirectReportArray = new JSONArray(
				getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport, null)); // get DirectReports of
																								// the user Two level
		JSONArray responseDirectReports = new JSONArray();
		JSONArray tempHoldChildDirectReports = new JSONArray();
		String childDirectReportsPath = mapRuleField.get(1).getValueFromPath();// Path to fetch Child Direct Reports
		String keyToRemoveObj = childDirectReportsPath.split("/")[0];// String to remove object from object before
		String directReportData = ""; // copying
		for (int i = 0; i < parentDirectReportArray.length(); i++) {
			directReportData = getValueFromPath(childDirectReportsPath, parentDirectReportArray.getJSONObject(i),
					session, forDirectReport, null);// fetching all the direct reports of a direct report
			if (directReportData != "") {
				tempHoldChildDirectReports = new JSONArray(directReportData);
				for (int j = 0; j < tempHoldChildDirectReports.length(); j++) {
					tempHoldChildDirectReports.getJSONObject(j).remove(keyToRemoveObj); // Removing object just make it
																						// look
																						// similar as of main obj
					responseDirectReports.put(tempHoldChildDirectReports.get(j));
				}
				// removing child directReports from Parent as those are already added to
				// response
				parentDirectReportArray.getJSONObject(i).remove(keyToRemoveObj);
				responseDirectReports.put(parentDirectReportArray.getJSONObject(i));
			}
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
					getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport, null));
			requestObj.put(mapRuleField.get(1).getKey(),
					getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport, null));
			requestObj.put(mapRuleField.get(2).getKey(),
					getFieldValue(mapRuleField.get(2).getField(), session, forDirectReport, null));
			CommonFunctions commonFunctions = new CommonFunctions();
			JSONObject apiResponse = new JSONObject(commonFunctions.callpostAPIWithJWT(mapRuleField.get(3).getUrl(),
					requestObj, mapRuleField.get(3).getDestinationName()));
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
				fieldValue = getFieldValue(tempMapRuleFields.getField(), session, forDirectReport, null);
				returnString = fieldValue.equals("") ? returnString
						: returnString + fieldValue + tempMapRuleFields.getKey();
			} else {
				fieldValue = getFieldValue(tempMapRuleFields.getField(), session, forDirectReport, null);
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
		String language = getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport, null);
		String dateToFormat = getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport, null);
		if (dateToFormat.equals("")) // return if value returned is ""
			return "";
		dateToFormat = dateToFormat.substring(dateToFormat.indexOf("(") + 1, dateToFormat.indexOf(")"));
		String seprator1 = mapRuleField.get(0).getKey();
		String seprator2 = mapRuleField.get(1).getKey();
		Date date;
		Locale locale;
		SimpleDateFormat sdf;
		switch (language) {
		case "HUN":
			date = new Date(Long.parseLong(dateToFormat));
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			return (cal.get(Calendar.YEAR) + seprator1 + hunLocale.values()[cal.get(Calendar.MONTH)] + seprator2
					+ cal.get(Calendar.DAY_OF_MONTH));
		case "DE":
			locale = new Locale(language); // as for DE required format is DD.MM.YYYY and our default is MMMM dd, yyyy
			date = new Date(Long.parseLong(dateToFormat));
			sdf = new SimpleDateFormat("dd" + seprator1 + "MM" + seprator2 + "yyyy", locale);
			return (sdf.format(date));
		default:
			// works with default languages like: fr, en, sv, es, de etc
			locale = new Locale(language);
			date = new Date(Long.parseLong(dateToFormat));
			sdf = new SimpleDateFormat("MMMM dd, yyyy", locale);
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
		int greaterThen = Integer
				.parseInt(getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport, null));
		int checkInteger = Integer
				.parseInt(getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport, null));

		if (checkInteger >= greaterThen) {
			return getFieldValue(mapRuleField.get(2).getField(), session, forDirectReport, null);
		} else {
			return getFieldValue(mapRuleField.get(3).getField(), session, forDirectReport, null);
		}
	}

	String divideBy(String ruleID, HttpSession session, Boolean forDirectReport)
			throws NumberFormatException, BatchException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		// Required to get the result from operation
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		int divideBy_devisor = Integer
				.parseInt(getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport, null));
		int toBeDivided_dividant = Integer
				.parseInt(getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport, null));

		return Double.toString(toBeDivided_dividant / divideBy_devisor);
	}

	String formatYearPlusValue(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Required to format date and add one to the year
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);
		String language = getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport, null);
		String dateToFormat = getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport, null);
		dateToFormat = dateToFormat.substring(dateToFormat.indexOf("(") + 1, dateToFormat.indexOf(")"));

		Date date = new Date(Long.parseLong(dateToFormat));
		SimpleDateFormat sdf_YYYY = new SimpleDateFormat("yyyy");
		Date decMonth = new Date(1577786942000L);
		Locale locale;
		SimpleDateFormat sdf_MMDD;
		String seprator1 = mapRuleField.get(0).getKey();
		String seprator2 = mapRuleField.get(1).getKey();
		switch (language) {
		case "HUN":
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			return (Integer.parseInt(sdf_YYYY.format(date))
					+ Integer.parseInt(getFieldValue(mapRuleField.get(2).getField(), session, forDirectReport, null))
					+ seprator1 + hunLocale.values()[11] + seprator2 + 31);
		case "DE": // as for DE required format is DD.MM.YYYY and our default is MMMM dd, yyyy
			locale = new Locale(language);
			sdf_MMDD = new SimpleDateFormat("dd" + seprator1 + "MM", locale);
			return (sdf_MMDD.format(decMonth) + seprator2 + (Integer.parseInt(sdf_YYYY.format(date))
					+ Integer.parseInt(getFieldValue(mapRuleField.get(2).getField(), session, forDirectReport, null))));
		default:
			// works with default languages like: fr, en, sv, es, etc
			locale = new Locale(language);
			sdf_MMDD = new SimpleDateFormat("MMMM dd,", locale);
			return (sdf_MMDD.format(decMonth) + " " + (Integer.parseInt(sdf_YYYY.format(date))
					+ Integer.parseInt(getFieldValue(mapRuleField.get(2).getField(), session, forDirectReport, null))));
		}
	}

	String fetchPickListValue(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// rule required to fetch value for a pick-list field
		String url = createPicklistURL(ruleID, session, forDirectReport);
		logger.debug("Picklist Fetch URL: " + url);
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
		JSONArray picklistData = new JSONObject(callSFSingle(mapRuleField.getKey(), url)).getJSONArray("results");
		// logger.debug("Picklist Fetched Data: " + picklistData);
		return picklistData.length() > 0
				? getValueFromPath(mapRuleField.getValueFromPath(), picklistData.getJSONObject(0), session,
						forDirectReport, null)
				: "";
	}

	String getCodelistValue(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// rule required to fetch code-list value from DB
		List<MapRuleFields> mapRuleFields = mapRuleFieldsService.findByRuleID(ruleID);
		String CodelistSFKey = getFieldValue(mapRuleFields.get(0).getField(), session, forDirectReport, null);// SF_key
																												// of
		// Code-list
		if (CodelistSFKey.equals(""))
			return "";
		String codeListID = codelistService.findByFieldAndKey(mapRuleFields.get(0).getFieldID(), CodelistSFKey).get(0)
				.getId();
		String language = getFieldValue(mapRuleFields.get(1).getField(), session, forDirectReport, null);
		if (language.equals(""))
			return "";
		List<CodelistText> codelistText = codelistTextService.findByCodelistLanguage(codeListID, language);
		return codelistText.size() > 0 ? codelistText.get(0).getValue() : "";
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
		Boolean isManager = Boolean.parseBoolean(getFieldValue(mapRuleField.get(0).getField(), session, false, null));
		if (!isManager) {
			logger.error("Unauthorized access! User: " + (String) session.getAttribute("loggedInUser")
					+ " who is not a manager, Tried accessing groups of user: " + requestData.getString("userID"));
			return "You are not authorized to access this data! This event has been logged!";
		}

		/*
		 *** Security Check *** Checking if userID passed from UI is actually a direct
		 * report of the loggenIn user
		 */
		Boolean isDirectReport = Boolean
				.parseBoolean(getFieldValue(mapRuleField.get(1).getField(), session, false, null));

		if (!isDirectReport) {
			logger.error("Unauthorized access! User: " + (String) session.getAttribute("loggedInUser")
					+ " Tried accessing groups of user: " + requestData.getString("userID")
					+ ", which is not its direct report or level 2");// userID passed from UI
			return "You are not authorized to access this data! This event has been logged!";
		}
		String countryID = getFieldValue(mapRuleField.get(2).getField(), session, true, null);// forDirectReport true
		String companyID = getFieldValue(mapRuleField.get(3).getField(), session, true, null);// forDirectReport true
		Iterator<MapCountryCompanyGroup> iterator = mapCountryCompanyGroupService
				.findByCountryCompany(countryID, companyID, true).iterator(); // Retrieving MapCountryCompanyGroup based
																				// on CountryID CompanyID and Manager
																				// true (sMss)
		JSONArray response = new JSONArray();
		String locale = (String) session.getAttribute("locale"); // to Fetch local specific data from Text Table
		MapCountryCompanyGroup tempMapCountryCompanyGroup;
		List<Text> tempTextList;
		JSONObject tempMapCountryCompanyGroupObj;
		while (iterator.hasNext()) {
			tempMapCountryCompanyGroup = iterator.next();
			tempMapCountryCompanyGroupObj = new JSONObject(tempMapCountryCompanyGroup.toString());
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getCountryID(), locale); // Fetching
																													// locale
																													// specific
																													// data
																													// for
																													// country
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("country_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("country_description_per_Locale",
						tempTextList.get(0).getDescription());
			}
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getCompanyID(), locale);// Fetching
																													// locale
																													// specific
																													// data
																													// for
																													// company
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("company_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("company_description_per_Locale",
						tempTextList.get(0).getDescription());
			}
			tempTextList = textService.findByRefrencedIdLocale(tempMapCountryCompanyGroup.getGroupID(), locale);// Fetching
																												// locale
																												// specific
																												// data
																												// for
																												// group
			if (tempTextList.size() > 0) {
				tempMapCountryCompanyGroupObj.put("group_text_per_Locale", tempTextList.get(0).getText());
				tempMapCountryCompanyGroupObj.put("group_description_per_Locale", tempTextList.get(0).getDescription());
			}
			response.put(tempMapCountryCompanyGroupObj);
		}
		return response.toString();
	}

	String getTemplates(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB to get templates of a Group of loggedIn user (Normal User / Non
		// Manager and NON Admin)

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

		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		JSONArray groupIdArray = requestData.getJSONArray("groupID");
		JSONObject response = new JSONObject();
		String groupID;
		for (int i = 0; i < groupIdArray.length(); i++) {
			groupID = groupIdArray.getString(i);// groupID passed from UI
			Boolean groupAvailableCheck = mapCountryCompanyGroupService
					.findByGroupCountryCompany(groupID, countryID, companyID, isManager).size() == 1 ? true : false;
			if (!groupAvailableCheck) {
				logger.error("Unauthorized access! User: " + (String) session.getAttribute("loggedInUser")
						+ " Tried accessing templates of group that is not available for this user. groupID: "
						+ groupID);
				return "You are not authorized to access this data! This event has been logged!";
			}
			// get available Templates in Azure from Session
			@SuppressWarnings("unchecked")
			Map<String, JSONObject> templatesAvailableInAzure = (Map<String, JSONObject>) session
					.getAttribute("availableTemplatesInAzure");
			List<MapGroupTemplates> mapGroupTemplate = mapGroupTemplateService.findByGroupID(groupID);
			// Now Iterating for each template assigned to the provided group
			Iterator<MapGroupTemplates> iterator = mapGroupTemplate.iterator();
			Boolean criteriaSatisfied;
			String templateID;
			Templates tempTemplate;
			JSONArray tempResponse = new JSONArray();
			MapGroupTemplates tempMapGroupTemplate;
			JSONObject tempTemplateObj;
			List<Text> tempTextList;
			String locale = (String) session.getAttribute("locale");
			while (iterator.hasNext()) {
				tempMapGroupTemplate = iterator.next();
				tempTemplate = tempMapGroupTemplate.getTemplate();
				tempTemplateObj = new JSONObject(tempTemplate.toString()); // object to save localeData and pass to
																			// response array
				tempTextList = textService.findByRefrencedIdLocale(tempTemplate.getId(), locale);// fetching locale data
																									// of
				// template
				if (tempTextList.size() > 0) {
					tempTemplateObj.put("template_text_per_Locale", tempTextList.get(0).getText());
					tempTemplateObj.put("template_description_per_Locale", tempTextList.get(0).getDescription());
				}
				// Generating criteria for each template to check if its valid for the loggedIn
				// user
				templateID = tempMapGroupTemplate.getTemplateID();
				criteriaSatisfied = checkCriteria(templateID, session, false); // forDirectReport false
				if (criteriaSatisfied) {
					// check if the template is available in Azure
					if (!templatesAvailableInAzure.containsKey(tempMapGroupTemplate.getTemplate().getName())) {
						tempTemplateObj.put("availableInAzure", false);
						tempResponse.put(tempTemplateObj);
						continue;
					}
					tempResponse.put(tempTemplateObj);
				}
			}
			response.put(groupID, tempResponse);
		}
		return response.toString();
	}

	String getTemplatesOfDirectReports(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Rule in DB to get templates of a direct report from loggerIn Manager User
		// (Not Admin User)

		JSONObject requestData = new JSONObject((String) session.getAttribute("requestData"));
		List<MapRuleFields> mapRuleField = mapRuleFieldsService.findByRuleID(ruleID);

		/*
		 *** Security Check *** Checking if loggedIn user is a manager
		 *
		 */
		Boolean isManager = Boolean.parseBoolean(getFieldValue(mapRuleField.get(0).getField(), session, false, null));
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
		Boolean isDirectReport = Boolean
				.parseBoolean(getFieldValue(mapRuleField.get(1).getField(), session, false, null));

		if (!isDirectReport) {
			logger.error("Unauthorized access! User: " + loggerInUser + " Tried accessing templates of a user: "
					+ requestData.getString("userID") + ", which is not its direct report or level 2");// userID passed
																										// from UI
			return "You are not authorized to access this data! This event has been logged!";
		}

		String directReportCountryID = getFieldValue(mapRuleField.get(2).getField(), session, true, null);// forDirectReport
																											// true
		String directReportCompanyID = getFieldValue(mapRuleField.get(3).getField(), session, true, null);// forDirectReport
																											// true
		getFieldValue(mapRuleField.get(4).getField(), session, true, null);// forDirectReport true

		/*
		 *** Security Check *** Checking if groupID passed from UI is actually available
		 * for the userID provided from the UI
		 */
		JSONArray groupIdArray = requestData.getJSONArray("groupID");
		JSONObject response = new JSONObject();
		String groupID;
		for (int i = 0; i < groupIdArray.length(); i++) {
			groupID = groupIdArray.getString(i);// groupID passed from UI

			Boolean groupAvailableCheck = mapCountryCompanyGroupService
					.findByGroupCountryCompany(groupID, directReportCountryID, directReportCompanyID, true).size() == 1
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
			Boolean criteriaSatisfied;
			String templateID;
			Templates tempTemplate;
			JSONArray tempResponse = new JSONArray();
			MapGroupTemplates tempMapGroupTemplate;
			JSONObject tempTemplateObj;
			List<Text> tempTextList;
			String locale = (String) session.getAttribute("locale");
			while (iterator.hasNext()) {
				tempMapGroupTemplate = iterator.next();
				tempTemplate = tempMapGroupTemplate.getTemplate();
				tempTemplateObj = new JSONObject(tempTemplate.toString()); // object to save localeData and pass to
																			// response array
				tempTextList = textService.findByRefrencedIdLocale(tempTemplate.getId(), locale);// fetching locale data
																									// of
				// template
				if (tempTextList.size() > 0) {
					tempTemplateObj.put("template_text_per_Locale", tempTextList.get(0).getText());
					tempTemplateObj.put("template_description_per_Locale", tempTextList.get(0).getDescription());
				}
				// Generating criteria for each template to check if its valid for the loggedIn
				// user
				templateID = tempMapGroupTemplate.getTemplateID();
				criteriaSatisfied = checkCriteria(templateID, session, true); // forDirectReport true
				if (criteriaSatisfied) {
					// check if the template is available in Azure
					if (!templatesAvailableInAzure.containsKey(tempMapGroupTemplate.getTemplate().getName())) {
						tempTemplateObj.put("availableInAzure", false);
						tempResponse.put(tempTemplateObj);
						continue;
					}
					tempResponse.put(tempTemplateObj);
				}
			}
			response.put(groupID, tempResponse);
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
					+ " Tried downloading document of a template that is not assigned for this user, templateID: "
					+ templateID);
			return "You are not authorized to access this data! This event has been logged!";
		}
		// Removing all the entities data from the session for Hard Reload of data from
		// SF
		List<String> distinctEntityNames = entitiesService.getDistinctNames();
		Iterator<String> entityNamesItr = distinctEntityNames.iterator();
		while (entityNamesItr.hasNext()) {
			session.removeAttribute(entityNamesItr.next());
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
		Boolean isManager = Boolean.parseBoolean(getFieldValue(mapRuleField.get(3).getField(), session, false, null));
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
		Boolean isDirectReport = Boolean
				.parseBoolean(getFieldValue(mapRuleField.get(2).getField(), session, false, null)); // For directReport
																									// false

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
			logger.error("Unauthorized access! User: " + loggerInUser + " Tried downloading doc of the user: " + userID
					+ " and template: " + templateID + " which is not assigned for this user");
			return "You are not authorized to access this data! This event has been logged!";
		}

		// Removing all the entities data from the session for Hard Reload of data from
		// SF
		List<String> distinctEntityNames = entitiesService.getDistinctNames();
		Iterator<String> entityNamesItr = distinctEntityNames.iterator();
		while (entityNamesItr.hasNext()) {
			session.removeAttribute("directReportEntities-" + requestData.getString("userID") + entityNamesItr.next());
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
	private Boolean checkCriteria(String templateID, HttpSession session, Boolean forDirectReport)
			throws NamingException, BatchException, ClientProtocolException, UnsupportedOperationException,
			URISyntaxException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		// This function is required to check if the provided criteria is valid for a
		// user or not
		List<MapTemplateCriteriaValues> mapTemplateCriteriaValues = mapTemplateCriteriaValuesService
				.findByTemplate(templateID);
		// This will get fields IDs required to confirm criteria
		// Now Iterating for each field mapped to Criteria generation
		Iterator<MapTemplateCriteriaValues> iterator = mapTemplateCriteriaValues.iterator();
		MapTemplateCriteriaValues tempMapTemplateCriteriaValues;
		while (iterator.hasNext()) {
			tempMapTemplateCriteriaValues = iterator.next();

			switch (tempMapTemplateCriteriaValues.getOperator().getSign()) {

			case "==":
				if (!getFieldValue(tempMapTemplateCriteriaValues.getField(), session, forDirectReport, null)
						.equals(tempMapTemplateCriteriaValues.getValue()))
					return false;
				break;

			case ">":
				if (!(Integer.parseInt(getFieldValue(tempMapTemplateCriteriaValues.getField(), session, forDirectReport,
						null)) > Integer.parseInt(tempMapTemplateCriteriaValues.getValue())))
					return false;
				break;

			case "<":
				if (!(Integer.parseInt(getFieldValue(tempMapTemplateCriteriaValues.getField(), session, forDirectReport,
						null)) < Integer.parseInt(tempMapTemplateCriteriaValues.getValue())))
					return false;
				break;

			case ">=":
				if (!(Integer.parseInt(getFieldValue(tempMapTemplateCriteriaValues.getField(), session, forDirectReport,
						null)) >= Integer.parseInt(tempMapTemplateCriteriaValues.getValue())))
					return false;
				break;

			case "<=":
				if (!(Integer.parseInt(getFieldValue(tempMapTemplateCriteriaValues.getField(), session, forDirectReport,
						null)) <= Integer.parseInt(tempMapTemplateCriteriaValues.getValue())))
					return false;
				break;

			default:
				break;
			}

		}
		return true;
	}

	private String getFieldValue(Fields field, HttpSession session, Boolean forDirectReport, String fieldBasedOnCountry)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NamingException,
			URISyntaxException, IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		logger.debug("Getting value for Field: " + field.getTechnicalName() + "  ::: RuleID: " + field.getRuleID()
				+ " ::: forDirectReport: " + forDirectReport);
		if (field.getRuleID() == null) {

			// checking if a default value is there in field if yes then return that
			if (field.getDefaultValue() != null)
				return field.getDefaultValue();
			// else fetch data
			JSONObject entityData;
			Entities entity = field.getEntity();
			logger.debug("EntityName: " + entity.getName() + " For Field: " + field.getTechnicalName());
			entity = checkForDependantEntity(entity); // Check for root entity and get root entity if current entity
														// is dependent on some other entity

			// now entity variable will be having the root entity from which will get the
			// data of our field
			entityData = getEntityData(entity, session, forDirectReport);
			return getValueFromPath(field.getValueFromPath(), entityData, session, forDirectReport,
					fieldBasedOnCountry);
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

		/*
		 * For Doc download Hard Reload start Always retrieve data from SF no need to
		 * fetch from Session
		 */
		/*
		 * if (session.getAttribute("hardReload") != null) { if (!forDirectReport) {//
		 * if false then data needs to get for the loggedIn user entityData =
		 * fetchDataFromSF(entity, session, forDirectReport);
		 * logger.debug("HardReload Data fetched from SF for entity: " + entityName +
		 * " ::: For Direct report: " + forDirectReport); return entityData; } // Else
		 * retrieve data for direct report entityData = fetchDataFromSF(entity, session,
		 * forDirectReport);
		 * logger.debug("Data fetched from SF for direct report entity: " + entityName +
		 * " ::: For Direct report: " + forDirectReport); return entityData; }
		 */
		/*
		 * For Doc download Hard Reload End
		 */

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
		String tempPath = entity.getExpandPath();
		String expandPath = tempPath != null ? tempPath.length() > 0 ? tempPath + "," : "" : "";
		List<Entities> dependentEntities = getDependentEntities(entity); // Get all the entities those are dependent on
																			// the root entity
		if (dependentEntities != null) {
			Iterator<Entities> iterator = dependentEntities.iterator(); // Iterating for each Entity for creating select
																		// and expand path
			Entities tempEntity;
			tempPath = "";
			while (iterator.hasNext()) {
				tempEntity = iterator.next();
				tempPath = createSelectPath(tempEntity);
				selectPath = selectPath == "" ? tempPath : tempPath != "" ? selectPath + "," + tempPath : selectPath;
				tempPath = tempEntity.getExpandPath();
				expandPath = tempPath.toString() != "null" || tempPath.toString() != "" ? expandPath + tempPath + ","
						: expandPath;
			}
		}
		expandPath = expandPath.length() > 0 ? expandPath.substring(0, expandPath.length() - 1) : "";
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
			responseObj = new JSONObject(batchResponse.getBody()).getJSONObject("d");
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
			Boolean forDirectReport, String basedOnCountry)
			throws JSONException, BatchException, ClientProtocolException, UnsupportedOperationException,
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NamingException, URISyntaxException, IOException {
		String[] pathArray = path.split("/");
		JSONObject currentObject = retriveFromObj;
		String value = "";
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
												.equals("null")
														? ""
														: currentObject.get(key.substring(0, key.length() - 2))
																.toString()
										: "";
			} else if (key.endsWith("]") && currentObject != null) { // in case of array get the indexed Object

				JSONArray tempArray = null;
				if (key.contains("?")) { // if ? then object to be retrieved from the Array is dynamic means NO static
											// index is provided
					tempArray = currentObject.getJSONArray(key.substring(0, key.indexOf('~')));

					String keyToSearchInEachObj = key.substring(key.indexOf("~SearchForKey~") + 14,
							key.indexOf("~FieldID~"));
					String fieldID = key.substring(key.indexOf("~FieldID~") + 9, key.indexOf('['));
					// System.out.println("FieldID: " + fieldID + " Key: " + keyToSearchInEachObj);
					String valueToSearch;
					if (fieldID.equals("PARAMETER")) // if fieldID equals PARAMETER then the value to be search is
														// coming from the parameter passed by the calling function
						valueToSearch = basedOnCountry; // so setting valueToSearch to the value passed from the
														// Calling function
					else if (fieldID.indexOf("TABLE_SF_DATA") != -1) { // else if value to search in the object is
																		// coming from Table
						fieldID = key.substring(key.indexOf("~TABLE_SF_DATA~") + 15, key.indexOf('['));
						valueToSearch = getFieldValue(fieldsService.findByID(fieldID).get(0), session, forDirectReport,
								null);
						valueToSearch = sFDataMappingService.findByKey(valueToSearch).get(0).getData();
					} else // else Value to search will come from a field
						valueToSearch = getFieldValue(fieldsService.findByID(fieldID).get(0), session, forDirectReport,
								null);
					// logger.debug("valueToSearch: " + valueToSearch);
					JSONObject tempJsonObj;
					for (int i = 0; i < tempArray.length(); i++) { // now Iterating each object in the array till a
																	// object with the value "valueToSearch" is not
																	// found, once found it will be returned
						tempJsonObj = tempArray.getJSONObject(i);
						if (tempJsonObj.getString(keyToSearchInEachObj).equals(valueToSearch)) {
							currentObject = tempJsonObj;
						}
					}
				} else { // else get the Static, which is set in the DB to retrieve object from the Array
					int index = key.indexOf('[');
					index = Integer.parseInt(key.substring(index + 1, index + 2)); // to get the index between []
					tempArray = currentObject.getJSONArray(key.substring(0, key.length() - 3));
					currentObject = tempArray.length() > 0 ? tempArray.getJSONObject(index) : null;
				}
			} else if (currentObject != null) {// in case of Obj
				currentObject = currentObject.has(key)// checking if that object has the key and that key is not null
						? currentObject.get(key).toString().equals("null") ? null : currentObject.getJSONObject(key)
						: null;
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
			responseObj.put(field.getTechnicalName(), getFieldValue(field, session, forDirectReport, null));
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
		// Note the complete results array/ is returned not the object inside results
		// array
		JSONObject responseObject = new JSONObject(batchResponses.get(0).getBody()).getJSONObject("d");

		String response = responseObject.toString();
		// logger.debug("Response from single request: " + response);
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
				getFieldValue(mapRuleField.get(0).getField(), session, forDirectReport, null));
		logger.debug("Available Groups:" + availableGroups.toString() + " ::: forDirectReport" + forDirectReport);
		String groupID;
		JSONObject tempAvailableTemplatesObj;
		for (int i = 0; i < availableGroups.length(); i++) {
			// saving group ID in Session requestData attribute as its expected in Get
			// Templates function
			groupID = availableGroups.getJSONObject(i).getString("id");
			requestData.put("groupID", new JSONArray().put(groupID));
			session.setAttribute("requestData", requestData.toString()); // Saving groups in session as its required in
																			// checkAvailable Templates Function
			tempAvailableTemplatesObj = new JSONObject(
					getFieldValue(mapRuleField.get(1).getField(), session, forDirectReport, null));
			availableTemplates = tempAvailableTemplatesObj.getJSONArray(groupID);
			logger.debug(
					"Available templates:" + availableTemplates.toString() + " ::: forDirectReport" + forDirectReport);
			for (int j = 0; j < availableTemplates.length(); j++) {
				if (requestData.getString("templateID").equals(availableTemplates.getJSONObject(j).getString("id"))) {
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
		JSONArray templateFieldTagBasedOnCountryArr = new JSONArray(); // array to hold
																		// tempTemplateFieldTagBasedOnCountry

		String fieldType = null;
		JSONObject objToPlace;
		while (iterator.hasNext()) {
			mapTemplateField = iterator.next();
			fieldType = mapTemplateField.getTemplateFiledTag().getType();
			if (fieldType != null) {
				templateFieldTagBasedOnCountryArr.put(mapTemplateField.getTemplateFiledTag());
				continue; // continue the loop
			}
			objToPlace = new JSONObject();
			objToPlace.put("Key", mapTemplateField.getTemplateFiledTag().getId());
			objToPlace.put("Value",
					getFieldValue(mapTemplateField.getTemplateFiledTag().getField(), session, forDirectReport, null));
			// To place value at specific location in POST object
			docPostObject = placeValue(objToPlace, mapTemplateField.getTemplateFiledTag().getPlaceFieldAtPath(),
					docPostObject);

		}
		processCountrySpecificFields(docPostObject, templateFieldTagBasedOnCountryArr, session, forDirectReport);
		logger.debug("Doc generation Post Obj:: " + docPostObject.toString());
		return docPostObject;
	}

	private JSONObject processCountrySpecificFields(JSONObject docPostObject,
			JSONArray templateFieldTagBasedOnCountryArr, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// Required to process Country specific fields
		// Now processing fields with country specific type; Kind of bad thing...
		// remember? ;)
		// Now fieldsWithType will have format like:
		/*
		 * { Type1: [ TemplateFiledTagObj1, TemplateFiledTagObj2, TemplateFiledTagObj3],
		 * Type2: [ TemplateFiledTagObj4, TemplateFiledTagObj5, TemplateFiledTagObj6 ] }
		 */

		Iterator<CountrySpecificFields> countrySpecificFieldsItr;
		String fieldValue;
		JSONObject objToPlace;
		for (int i = 0; i < templateFieldTagBasedOnCountryArr.length(); i++) {
			TemplateFieldTag tempTemplateFieldTag = (TemplateFieldTag) templateFieldTagBasedOnCountryArr.get(i);
			String country = getFieldValue(mapRuleFieldsService
					.findByRuleID(rulesService.findByRuleName("processCountrySpecificFields").get(0).getId()).get(0)
					.getField(), session, forDirectReport, tempTemplateFieldTag.getType());

			countrySpecificFieldsItr = countrySpecificFieldsService
					.findByTypeAndCountry(tempTemplateFieldTag.getType(), country).iterator();
			int counter = 1;
			while (countrySpecificFieldsItr.hasNext()) {
				fieldValue = getFieldValue(countrySpecificFieldsItr.next().getField(), session, forDirectReport,
						tempTemplateFieldTag.getType());
				logger.debug("fieldValue::: " + fieldValue);
				if (fieldValue.equals("")) // Continue if "" and move to next field mapped to the type if any ;D
					continue;
				// else add the value to the post object
				objToPlace = new JSONObject();
				objToPlace.put("Key", tempTemplateFieldTag.getId() + 0 + counter++);
				objToPlace.put("Value", fieldValue);
				docPostObject = placeValue(objToPlace, tempTemplateFieldTag.getPlaceFieldAtPath(), docPostObject);
			}
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
			throws URISyntaxException, NamingException, ParseException, IOException {// function to get document from
																						// doc gen API
		Rules rule = rulesService.findByRuleName("getDocFromAPI").get(0);
		MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(rule.getId()).get(0);
		CommonFunctions commonFunctions = new CommonFunctions();
		return commonFunctions.callpostAPIWithJWT(mapRuleField.getUrl(), requestObj, mapRuleField.getDestinationName());
	}

	private String createPicklistURL(String ruleID, HttpSession session, Boolean forDirectReport)
			throws BatchException, ClientProtocolException, UnsupportedOperationException, NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NamingException, URISyntaxException, IOException {
		// required to create URL for PickList
		List<MapRuleFields> mapRuleFields = mapRuleFieldsService.findByRuleID(ruleID);
		MapRuleFields mapRuleField;
		String url = mapRuleFields.get(0).getUrl();
		url = url + "&$filter=";
		mapRuleFields.remove(0);
		Iterator<MapRuleFields> iterator = mapRuleFields.iterator();
		while (iterator.hasNext()) {
			mapRuleField = iterator.next();
			url = url + mapRuleField.getKey() + " eq '"
					+ getFieldValue(mapRuleField.getField(), session, forDirectReport, null) + "' and ";
		}
		url = url.substring(0, url.length() - 5);
		return url;
	}

	private String getLocale(HttpSession session) throws BatchException, ClientProtocolException,
			UnsupportedOperationException, NoSuchMethodException, SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NamingException, URISyntaxException, IOException {
		String locale = getFieldValue(mapRuleFieldsService
				.findByRuleID(rulesService.findByRuleName("getLocale").get(0).getId()).get(0).getField(), session,
				false, null);
		return locale;
	}
	/*
	 *** Helper functions END***
	 */
}