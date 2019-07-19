package com.nga.xtendhr.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

import com.nga.xtendhr.config.DBConfiguration;
import com.nga.xtendhr.model.ConfigurableColumns;
import com.nga.xtendhr.model.ConfigurableTables;
import com.nga.xtendhr.model.Countries;
import com.nga.xtendhr.service.CodelistService;
import com.nga.xtendhr.service.CodelistTextService;
import com.nga.xtendhr.service.ConfigurableColumnsService;
import com.nga.xtendhr.service.ConfigurableTablesService;
import com.nga.xtendhr.service.CountryService;
import com.nga.xtendhr.service.EntitiesService;
import com.nga.xtendhr.service.FieldsService;
import com.nga.xtendhr.service.MapCountryCompanyGroupService;
import com.nga.xtendhr.service.MapGroupTemplatesService;
import com.nga.xtendhr.service.MapRuleFieldsService;
import com.nga.xtendhr.service.MapTemplateFieldsService;
import com.nga.xtendhr.service.RulesService;
import com.nga.xtendhr.service.TemplateCriteriaGenerationService;
import com.nga.xtendhr.service.TemplateService;

@RestController
@RequestMapping("/DocGen")
public class Configurator {
	Logger logger = LoggerFactory.getLogger(DocGen.class);

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

	@Autowired
	CodelistTextService codelistTextService;

	@Autowired
	CodelistService codelistService;

	@Autowired
	ConfigurableTablesService configurableTablesService;

	@Autowired
	ConfigurableColumnsService configurableColumnsService;

	@Autowired
	CountryService countryService;

	@GetMapping(value = "/docGenAdmin/configurator/getConfigurableTables")
	public ResponseEntity<?> getTableNames(HttpServletRequest request) {

		try {
			HttpSession session = request.getSession(false);// false is not create new session and use the existing
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
						+ ", which is not an admin in SF, tried to access TableNames in configurator App.");
				return new ResponseEntity<>(
						"Error! You are not authorized to access this resource! This event has been logged!",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return ResponseEntity.ok().body(configurableTablesService.findAll());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/docGenAdmin/configurator/getTableData")
	public ResponseEntity<?> getTableData(@RequestParam(name = "tableID") String tableID, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession(false);// false is not create new session and use the existing
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
						+ ", which is not an admin in SF, tried to access TableNames in configurator App.");
				return new ResponseEntity<>(
						"Error! You are not authorized to access this resource! This event has been logged!",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			ConfigurableTables configurableTables = configurableTablesService.findById(tableID);
			if (configurableTables == null)
				return new ResponseEntity<>("Error! Table not found.", HttpStatus.INTERNAL_SERVER_ERROR);

			List<ConfigurableColumns> configurableColumns = configurableColumnsService
					.findByTableID(configurableTables.getId()); // get all configurable columns
			String tablePath = configurableTables.getPath();
			JSONObject response = new JSONObject();
			response.put("columns", configurableColumns);
			switch (tablePath) {
			case DBConfiguration.COUNTRIES:
				List<Countries> countries = countryService.dynamicSelect(configurableColumns); // get only the
																								// configurable columns
				response.put("data", countries);
				return ResponseEntity.ok().body(response.toString());
			case DBConfiguration.GROUPS:
				break;
			default:
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return null;
	}
}
