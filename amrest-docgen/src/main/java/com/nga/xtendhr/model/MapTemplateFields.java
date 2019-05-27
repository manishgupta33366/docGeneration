package com.nga.xtendhr.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.nga.xtendhr.config.DBConfiguration;

/*
 * AppName: DocGen
 * TableName: DGENC_MAP_RULE_FIELDS
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.MAP_TEMPLATE_FIELDS, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({
		@NamedQuery(name = "MapTemplateFields.findBytemplateID", query = "SELECT MTF FROM MapTemplateFields MTF WHERE MTF.templateID = :templateID") })
public class MapTemplateFields {
	@Id
	@Column(name = "\"TEMPLATE.ID\"", columnDefinition = "VARCHAR(32)")
	private String templateID;

	@Id
	@Column(name = "\"FIELD.ID\"", columnDefinition = "VARCHAR(32)")
	private String fieldID;

	@Column(name = "\"TEMPLATE_FIELD_NAME\"", columnDefinition = "VARCHAR(64)")
	private String templateFieldName;

	@Column(name = "\"PLACE_FIELD_AT_PATH\"", columnDefinition = "VARCHAR(128)")
	private String placeFieldAtPath;

	public String getPlaceFieldAtPath() {
		return placeFieldAtPath;
	}

	public void setPlaceFieldAtPath(String placeFieldAtPath) {
		this.placeFieldAtPath = placeFieldAtPath;
	}

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"TEMPLATE.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Templates template;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"FIELD.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Fields field;

	public String getTemplateID() {
		return templateID;
	}

	public void setTemplateID(String templateID) {
		this.templateID = templateID;
	}

	public String getFieldID() {
		return fieldID;
	}

	public void setFieldID(String fieldID) {
		this.fieldID = fieldID;
	}

	public String getTemplateFieldName() {
		return templateFieldName;
	}

	public void setTemplateFieldName(String templateFieldName) {
		this.templateFieldName = templateFieldName;
	}

	public Templates getTemplate() {
		return template;
	}

	public void setTemplate(Templates template) {
		this.template = template;
	}

	public Fields getField() {
		return field;
	}

	public void setField(Fields field) {
		this.field = field;
	}
}
