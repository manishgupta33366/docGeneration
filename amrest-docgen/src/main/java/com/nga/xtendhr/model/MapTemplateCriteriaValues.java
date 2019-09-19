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
 * TableName: DGENC_MAP_CRITERIA_FIELDS
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.MAP_TEMPLATE_CRITERIA_VALUES, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({
		@NamedQuery(name = "MapTemplateCriteriaValues.findByTemplate", query = "SELECT MTCV FROM MapTemplateCriteriaValues MTCV WHERE MTCV.templateId = :templateId"),
		@NamedQuery(name = "MapTemplateCriteriaValues.selectAll", query = "SELECT MTCV FROM MapTemplateCriteriaValues MTCV") })
public class MapTemplateCriteriaValues {

	@Id
	@Column(name = "\"TEMPLATE.ID\"", columnDefinition = "VARCHAR(32)")
	private String templateId;

	@Id
	@Column(name = "\"CRITERIA.ID\"", columnDefinition = "VARCHAR(32)")
	private String criteriaId;

	@Id
	@Column(name = "\"FIELD.ID\"", columnDefinition = "VARCHAR(32)")
	private String fieldId;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"FIELD.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Fields field;

	public Fields getField() {
		return field;
	}

	public void setField(Fields field) {
		this.field = field;
	}

	public String getCriteriaId() {
		return criteriaId;
	}

	public void setCriteriaId(String criteriaId) {
		this.criteriaId = criteriaId;
	}

	@Column(name = "\"VALUE\"", columnDefinition = "VARCHAR(32)")
	private String value;

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {// overriding the toString() method
		JSONObject obj = new JSONObject();
		obj.put("templateId", this.getTemplateId());
		obj.put("criteriaId", this.getCriteriaId());
		obj.put("fieldId", this.getFieldId());
		obj.put("value", this.getValue());
		return obj.toString();
	}
}
