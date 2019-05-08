package com.nga.xtendhr.utility;

import javax.naming.NamingException;

import com.nga.xtendhr.connection.DestinationClient;

/*
 * GetDestination class 
 * Used to get destination client 
 * which is required to make external calls
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

public class GetDestinationClient {

	private String destinationName;

	public GetDestinationClient(String destinationName) {
		this.destinationName = destinationName;
	}

	public String getDestinationName() {
		return destinationName;
	}

	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	public DestinationClient getDestinationClient() throws NamingException {
		DestinationClient destClient = new DestinationClient();
		destClient.setDestName(destinationName);
		destClient.setHeaderProvider();
		destClient.setConfiguration();
		destClient.setDestConfiguration();
		destClient.setHeaders(destClient.getDestProperty("Authentication"));
		return destClient;
	}
}
