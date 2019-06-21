package com.nga.xtendhr.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nga.xtendhr.model.MapRuleFields;
import com.nga.xtendhr.service.MapRuleFieldsService;
import com.nga.xtendhr.utility.CommonFunctions;

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

	@Autowired
	MapRuleFieldsService mapRuleFieldsService;

	@PostMapping(value = "executePostRule")
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

	/*
	 *** Helper functions END***
	 */
}
