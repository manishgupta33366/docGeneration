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
 * TableName: DGEN_TEMPLATES
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.TEMPLATES, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({ @NamedQuery(name = "Templates.findById", query = "SELECT T FROM Templates T WHERE T.id = :id") })
public class Templates {
	@Id
	@Column(name = "\"ID\"", columnDefinition = "VARCHAR(32)")
	private String id;

	@Column(name = "\"NAME\"", columnDefinition = "VARCHAR(64)")
	private String name;

	@Column(name = "\"CRITERIA.ID\"", columnDefinition = "VARCHAR(128)")
	private String criteriaId;

	@Column(name = "\"DESCRIPTION\"", columnDefinition = "VARCHAR(128)")
	private String description;

	@Column(name = "\"DISPLAY_NAME\"", columnDefinition = "VARCHAR(128)")
	private String displayName;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

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

	public String getCriteriaId() {
		return criteriaId;
	}

	public void setCriteriaId(String criteriaId) {
		this.criteriaId = criteriaId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {// overriding the toString() method
		JSONObject obj = new JSONObject();
		obj.put("id", this.id);
		obj.put("name", this.name);
		obj.put("description", this.description);
		return obj.toString();
	}
}
