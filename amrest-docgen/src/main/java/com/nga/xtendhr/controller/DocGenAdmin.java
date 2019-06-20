package com.nga.xtendhr.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
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

import com.nga.xtendhr.connection.DestinationClient;
import com.nga.xtendhr.model.MapRuleFields;
import com.nga.xtendhr.service.MapRuleFieldsService;
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
@RequestMapping("/DocGen/docGenAdmin")
public class DocGenAdmin {

	Logger logger = LoggerFactory.getLogger(DocGen.class);
	private final String sfDestination = CommonVariables.sfDestination;

	@Autowired
	MapRuleFieldsService mapRuleFieldsService;

	@GetMapping(value = "/login")
	public ResponseEntity<?> login(HttpServletRequest request) {
		try {
			HttpSession session = request.getSession(false);
			if (session != null) {
				// invalidating any old session
				session.invalidate();
			}

			String loggedInUser = request.getUserPrincipal().getName();
			loggedInUser = loggedInUser.equals("S0014379281") || loggedInUser.equals("S0018269301")
					|| loggedInUser.equals("S0019013022") || loggedInUser.equals("S0020227452") ? "E00000638"
							: loggedInUser;
			/*
			 *** Security Check *** Checking if user trying to login is exactly an Admin or
			 * not
			 */
			Boolean isAdmin = checkIfAdmin(loggedInUser);
			if (!isAdmin) {
				logger.error("Unauthorized access! User:" + loggedInUser
						+ ", which is not an admin in SF, tried to login as admin.");
				return new ResponseEntity<>(
						"Error! You are not authorized to access this resource! This event has been logged!",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			session = request.getSession(true);// True to create a new session for the logged-in user as
												// its the initial call
			session.setAttribute("adminLoginStatus", "Success");
			session.setAttribute("loggedInUser", request.getUserPrincipal().getName());
			return ResponseEntity.ok().body("Login Success!");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "executePostRule")
	public ResponseEntity<?> executeRule(@RequestParam(name = "ruleID") String ruleID, @RequestBody String requestData,
			HttpServletRequest request) {

		try {
			HttpSession session = request.getSession(false);// false is not create new session and use the existing
			// session
			if (session.getAttribute("adminLoginStatus") == null) {
				return new ResponseEntity<>("Session timeout! Please Login again!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			MapRuleFields mapRuleField = mapRuleFieldsService.findByRuleID(ruleID).get(0);
			CommonFunctions commonFunctions = new CommonFunctions();
			return ResponseEntity.ok()
					.body(commonFunctions.callpostAPI(mapRuleField.getUrl(), new JSONObject(requestData)));
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
	 *** Helper functions START***
	 */
	private Boolean checkIfAdmin(String loggedInUser)
			throws NamingException, ClientProtocolException, IOException, URISyntaxException {
		CommonFunctions commonFunctionsObj = new CommonFunctions();
		DestinationClient destClient = commonFunctionsObj.getDestinationCLient(sfDestination);

		// calling users API to get country of the loggedIn user
		HttpResponse response = destClient.callDestinationGET("/User",
				"?$filter=userId eq '" + loggedInUser + "'&$format=json&$select=country");
		String responseJsonString = EntityUtils.toString(response.getEntity(), "UTF-8");
		JSONObject responseObject = new JSONObject(responseJsonString);
		responseObject = responseObject.getJSONObject("d").getJSONArray("results").getJSONObject(0);

		// calling DynamicGroup to get GroupID
		logger.debug("1st call: " + "/DynamicGroup", "?$format=json&$filter=groupName eq 'CS_HR_ADMIN_"
				+ responseObject.getString("country") + "' and groupType eq 'permission'&$select=groupID");
		response = destClient.callDestinationGET("/DynamicGroup", "?$format=json&$filter=groupName eq 'CS_HR_ADMIN_"
				+ responseObject.getString("country") + "' and groupType eq 'permission'&$select=groupID");
		responseJsonString = EntityUtils.toString(response.getEntity(), "UTF-8");
		responseObject = new JSONObject(responseJsonString);
		responseObject = responseObject.getJSONObject("d").getJSONArray("results").getJSONObject(0);

		// calling getUsersByDynamicGroup to check if user trying to logging is an Admin
		logger.debug("2nd call: " + "/getUsersByDynamicGroup", "?$format=json&$filter=userId eq '" + loggedInUser
				+ "'&groupId=" + responseObject.getString("groupID") + "L");
		response = destClient.callDestinationGET("/getUsersByDynamicGroup", "?$format=json&$filter=userId eq '"
				+ loggedInUser + "'&groupId=" + responseObject.getString("groupID") + "L");
		responseJsonString = EntityUtils.toString(response.getEntity(), "UTF-8");
		responseObject = new JSONObject(responseJsonString);
		JSONArray responseArray = responseObject.getJSONArray("d");
		for (int i = 0; i < responseArray.length(); i++) {
			if (responseArray.getJSONObject(i).getString("userId").equals(loggedInUser)) {
				return true;
			}
		}
		return false;
	}
	/*
	 *** Helper functions END***
	 */
}
