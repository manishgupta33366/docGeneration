package com.nga.xtendhr.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.nga.xtendhr.connection.BatchRequest;
import com.nga.xtendhr.connection.DestinationClient;
import com.nga.xtendhr.utility.GetDestinationClient;

/*
 * Initial_SF_PEX Controller 
 * Demonstrating how application apis can be separated, accessed using URL separation and Role
 * 
 * Here /StandartUser in the URL tells this endpoint
 * can only be accessed by a user having StandardUser role
 * Role and its configuration is defined in web.xml file
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@RestController
@RequestMapping("/StandardUser/api")
public class Initial_SF_PEX {

	/*
	 * Following destinations must be imported in SCP account Both of these
	 * destinations can be found in the root Zip file In SF destination a valid SF
	 * userName and password must be specified And in Pex destination a valid JWT---
	 * ---token must be passed in the password field of the destination
	 */
	public static final String sfDestinationName = "AmitSF";
	public static final String pexDestinationName = "AmitPex";

	@GetMapping(value = "/initial")
	public String initialGet(HttpServletRequest request) throws NamingException, BatchException,
			ClientProtocolException, UnsupportedOperationException, URISyntaxException, IOException {
		try {
			HttpSession session = request.getSession(true);// True to create a new session for the loggedin user as its
															// the initial call
			String loggedInUser = request.getUserPrincipal().getName();

			/*
			 * Using static Username for now as bydefault SCP SSO is implemented in any SCP
			 * account. So any User with a SCP account credentials and Role 'StandardUser'
			 * assigned will be able to access this endpoint.
			 */
			loggedInUser = "E00000023";

			// get the Emjob and User Details of the logged In user
			Map<String, String> entityMap = new HashMap<String, String>();
			BatchRequest batchRequest = new BatchRequest();
			batchRequest.configureDestination(sfDestinationName);
			entityMap.put("EmpJob", "?$filter=userId eq '" + loggedInUser
					+ "' &$expand=userNav&$select=company,userNav/defaultFullName,userNav/defaultLocale,userNav/firstName,userNav/lastName&$format=JSON");
			// adding requests to Batches
			for (Map.Entry<String, String> entity : entityMap.entrySet()) {
				batchRequest.createQueryPart("/" + entity.getKey() + entity.getValue(), entity.getKey());
			}
			batchRequest.callBatchPOST("/$batch", "");// Executing Batch requests
			JSONArray sfentityArray = new JSONArray();
			List<BatchSingleResponse> batchResponses = batchRequest.getResponses();
			// reading responses from Batch calls
			for (BatchSingleResponse batchResponse : batchResponses) {
				sfentityArray.put(new JSONObject(batchResponse.getBody()));
			}

			// Calling PEX --- Rest Template is used for making the Call
			ClientHttpRequestFactory requestFactory = getClientHttpRequestFactory();
			RestTemplate restTemplate = new RestTemplate(requestFactory);
			// get destination client
			GetDestinationClient getDestClient = new GetDestinationClient(pexDestinationName);
			DestinationClient destClient = getDestClient.getDestinationClient();
			String url = destClient.getDestProperty("URL");
			// get JWT token saved in destination password property
			// In Production application JWT token will be generated at runtime with a
			// particular expiration time
			String jwtToken = destClient.getDestProperty("Password");
			// Creating Headers for PEX call and setting properties on them
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
			headers.set("Authorization", "Bearer " + jwtToken);
			HttpEntity<?> entity = new HttpEntity<>(headers);
			JSONObject pexResponseObj = new JSONObject();
			JSONObject batchResponse = sfentityArray.getJSONObject(0).getJSONObject("d").getJSONArray("results")
					.getJSONObject(0);
			String countryCode = batchResponse.getString("company");
			String locale = batchResponse.getJSONObject("userNav").getString("defaultLocale");
			session.setAttribute("company", countryCode);
			session.setAttribute("defaultLocale", locale);
			ResponseEntity<String> restTemplateResponse = restTemplate.exchange(
					url + "/forms?personId=" + loggedInUser + "&countryCode=" + countryCode + "&locale=" + locale,
					HttpMethod.GET, entity, String.class);
			pexResponseObj.put("pexForms", new JSONObject(restTemplateResponse.getBody()));
			JSONObject responeObj = new JSONObject();
			responeObj.put("SF_Data", sfentityArray);
			responeObj.put("PEX_Data", pexResponseObj.getJSONObject("pexForms"));
			return responeObj.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error!";
		}
	}

	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 5000;
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout).build();
		CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		return new HttpComponentsClientHttpRequestFactory(client);
	}
}
