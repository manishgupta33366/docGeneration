package com.nga.xtendhr.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nga.xtendhr.controller.DocGen;

/*
 * CommonFunctions class 
 * Containing all the common functions required for the complete application
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

public class CommonFunctions {
	Logger logger = LoggerFactory.getLogger(DocGen.class);

	public String callpostAPI(String url, JSONObject body) throws IOException {
		logger.debug("POST Body to send:" + body.toString());
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(body.toString().getBytes());
		os.flush();
		os.close();
		// For POST only - END

		int responseCode = con.getResponseCode();
		logger.debug("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			logger.debug("POST resposne:" + response.toString());
			return response.toString();
		} else {
			logger.debug("Error while retriving Templates from API, ResponseCode: " + responseCode);
			return new JSONObject().put("templates", new JSONArray()).toString();
		}
	}
}
