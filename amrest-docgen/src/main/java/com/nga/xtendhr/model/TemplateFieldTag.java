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

import org.json.JSONObject;

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
@NamedQueries({ @NamedQuery(name = "TemplateFieldTag.selectAll", query = "SELECT TFT FROM TemplateFieldTag TFT") })
public class TemplateFieldTag {

	@Id
	@Column(name = "\"ID\"", columnDefinition = "VARCHAR(32)")
	private String id;

	@Column(name = "\"FIELD.ID\"", columnDefinition = "VARCHAR(32)")
	private String fieldId;

	@Column(name = "\"PLACE_FIELD_AT_PATH\"", columnDefinition = "VARCHAR(128)")
	private String placeFieldAtPath;

	@Column(name = "\"TYPE\"", columnDefinition = "VARCHAR(32)")
	private String type;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"FIELD.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Fields field;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

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

	public String toString() {// overriding the toString() method
		JSONObject obj = new JSONObject();
		obj.put("id", this.getId());
		obj.put("fieldId", this.getFieldId());
		obj.put("type", this.getType());
		obj.put("placeFieldAtPath", this.getPlaceFieldAtPath());
		return obj.toString();
	}
}
