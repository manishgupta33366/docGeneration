package com.nga.xtendhr.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.json.JSONObject;

import com.nga.xtendhr.config.DBConfiguration;

/*
 * AppName: DocGen
 * TableName: DGEN_OPERATORS
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.OPERATORS, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({ @NamedQuery(name = "Operators.selectAll", query = "SELECT o FROM Operators o") })
public class Operators {

	@Id
	@Column(name = "\"ID\"", columnDefinition = "VARCHAR(32)")
	private String id;

	@Column(name = "\"NAME\"", columnDefinition = "VARCHAR(32)")
	private String name;

	@Column(name = "\"SIGN\"", columnDefinition = "VARCHAR(8)")
	private String sign;

	@Column(name = "\"DESCRIPTION\"", columnDefinition = "VARCHAR(128)")
	private String description;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {// overriding the toString() method
		JSONObject obj = new JSONObject();
		obj.put("id", this.getId());
		obj.put("name", this.getName());
		obj.put("sign", this.getSign());
		obj.put("description", this.getDescription());
		return obj.toString();
	}
}
