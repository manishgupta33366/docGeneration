package com.nga.xtendhr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/*
 * Template Application Note:
 * 					
 * 					This Spring application template provides a standard Directory structure 
 * 					along with minimum configurations and dependencies required to run a spring application
 * 			
 *					
 *					Steps to be performed in order to import and work with this Template
 * 
 * 					Import this application in eclipse
 * 					RightClick on the root package i.e. xtendhr-java template > Click Refactor > Rename Maven Artifact 
 * 					And change ArtifactID, GroupID as per the client requirement
 * 
 * 					After Refactoring you can choose to rename the packages as per client requirement
 * 					Just right click on the root package i.e com.nga.xtendhr
 * 					Click Refactor  > Rename 
 * 					Check both 'Update References' and 'Rename sub packages' to rename all the sub packages
 * 					For example: clientName can be added in place of xtendhr 
 * 								 'com.nga.xtendhr' can be changed to 'com.nga.clientname' 
 * 					
 */

/*
 * Application class 
 * Class to initialize this complete application as a Spring Boot application
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SpringApplication.run(Application.class, args);
	}

}
