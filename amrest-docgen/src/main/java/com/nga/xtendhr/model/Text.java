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
 * TableName: DGEN_TEXT
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.TEXT, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({ @NamedQuery(name = "Text.selectAll", query = "SELECT T FROM Text T"),
		@NamedQuery(name = "Text.findByRefrencedIdLocale", query = "SELECT T FROM Text T WHERE T.referencedID = :referencedID and T.locale = :locale") })
public class Text {

	@Id
	@Column(name = "\"ID\"", columnDefinition = "VARCHAR(32)")
	private String id;

	@Column(name = "\"REFERENCED_ID\"", columnDefinition = "VARCHAR(8)")
	private String referencedID;

	@Column(name = "\"LOCALE\"", columnDefinition = "VARCHAR(8)")
	private String locale;

	@Column(name = "\"TEXT\"", columnDefinition = "VARCHAR(64)")
	private String text;

	@Column(name = "\"DESCRIPTION\"", columnDefinition = "VARCHAR(128)")
	private String description;

	public String getReferencedID() {
		return referencedID;
	}

	public void setReferencedID(String referencedID) {
		this.referencedID = referencedID;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
