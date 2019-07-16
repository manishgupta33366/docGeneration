package com.nga.xtendhr.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.nga.xtendhr.config.DBConfiguration;

/*
 * AppName: DocGen
 * TableName: DGEN_COUNTRIES
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.CODELIST_TEXT, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({
		@NamedQuery(name = "CodelistText.findByCodelist_Language", query = "SELECT CT FROM CodelistText CT WHERE CT.codeListID = :codeListID AND CT.language = :language") })
public class CodelistText {
	@Id
	@Column(name = "\"CODELIST.ID\"", columnDefinition = "VARCHAR(32)")
	private String codeListID;

	@Id
	@Column(name = "\"LANGUAGE\"", columnDefinition = "VARCHAR(5)")
	private String language;

	@Id
	@Column(name = "\"VALUE\"", columnDefinition = "VARCHAR(32)")
	private String value;

	@Column(name = "\"DESCRIPTION\"", columnDefinition = "VARCHAR(128)")
	private String description;

	public String getId() {
		return codeListID;
	}

	public void setId(String codeListId) {
		this.codeListID = codeListId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
