package com.nga.xtendhr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * ApplicationTest controller 
 * A Test Controller used to check if the application is 'UP' right now or not
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@RestController
public class ApplicationTest {
	@GetMapping("/checkApplication")
	public String checkApplication() {
		return "Application is UP!";
	}
}
