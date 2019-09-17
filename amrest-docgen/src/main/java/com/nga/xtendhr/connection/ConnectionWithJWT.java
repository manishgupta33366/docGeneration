package com.nga.xtendhr.connection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nga.xtendhr.controller.DocGen;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@SuppressWarnings("deprecation")
public class ConnectionWithJWT {
	Logger logger = LoggerFactory.getLogger(DocGen.class);
	private DestinationConfiguration destination;

	public void setDestination(DestinationConfiguration destination) throws NamingException, IOException {
		this.destination = destination;
	}

	protected String getHS256JWTToken() {

		// Generate signing key
		String base64Key = DatatypeConverter.printBase64Binary(this.destination.getProperty("Password").getBytes());
		byte[] secretBytes = DatatypeConverter.parseBase64Binary(base64Key);

		// Generate Header Section
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("typ", "JWT");
		header.put("alg", "HS256");

		// Generate Payload section
		Map<String, Object> payload = new HashMap<String, Object>();
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		Date exp = new Date(nowMillis + 300000);
		payload.put("iat", now.getTime() / 1000);
		payload.put("exp", exp.getTime() / 1000);
		payload.put("sub", this.destination.getProperty("User"));

		// Generate token
		JwtBuilder builder = Jwts.builder().setHeader(header).setClaims(payload).signWith(SignatureAlgorithm.HS256,
				secretBytes);

		String token = builder.compact();
		return token;
	}

	public HttpResponse callDestinationPOST(String path, String postJson)
			throws URISyntaxException, ClientProtocolException, IOException {

		URL url = new URL(this.destination.getProperty("URL") + path);
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
				url.getQuery(), url.getRef());
		String urlString = uri.toASCIIString();
		logger.debug("urlString" + urlString);
		@SuppressWarnings({ "resource" })
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost request = new HttpPost(urlString);

		StringEntity entity = new StringEntity(postJson, "UTF-8");
		request.setEntity(entity);
		request.setHeader("Accept", "application/json");
		request.setHeader("Content-type", "application/json");
		request.setHeader("Authorization", getHS256JWTToken());
		HttpResponse response = httpClient.execute(request);
		logger.debug("responseJson: " + response);
		return response;
	}
}
