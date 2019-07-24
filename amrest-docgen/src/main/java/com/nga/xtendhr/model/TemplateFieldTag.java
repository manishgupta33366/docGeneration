package com.nga.xtendhr.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nga.xtendhr.config.DBConfiguration;

/*
 * AppName: DocGen
 * TableName: DGEN_MAP_TEMPLATE_FIELD_NAME
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.TEMPLATE_FIELD_TAG, schema = DBConfiguration.SCHEMA_NAME)
public class TemplateFieldTag {

	@Id
	@Column(name = "\"ID\"", columnDefinition = "VARCHAR(32)")
	private String id;

	@Column(name = "\"FIELD.ID\"", columnDefinition = "VARCHAR(32)")
	private String fieldId;

	@Column(name = "\"PLACE_FIELD_AT_PATH\"", columnDefinition = "VARCHAR(128)")
	private String placeFieldAtPath;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"FIELD.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Fields field;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getPlaceFieldAtPath() {
		return placeFieldAtPath;
	}

	public void setPlaceFieldAtPath(String placeFieldAtPath) {
		this.placeFieldAtPath = placeFieldAtPath;
	}

	public Fields getField() {
		return field;
	}

	public void setField(Fields field) {
		this.field = field;
	}
}