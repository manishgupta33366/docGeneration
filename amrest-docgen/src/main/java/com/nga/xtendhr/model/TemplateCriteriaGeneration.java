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
 * TableName: DGEN_TEMPLATE_CRITERIA_GENERATION
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.TEMPLATE_CRITERIA_GENERATION, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({
		@NamedQuery(name = "TemplateCriteriaGeneration.findByTemplateID", query = "SELECT TCG FROM TemplateCriteriaGeneration TCG WHERE TCG.templateID = :templateID ORDER BY TCG.seq"),
		@NamedQuery(name = "TemplateCriteriaGeneration.getDistinctFields", query = "SELECT DISTINCT TCG.field FROM TemplateCriteriaGeneration TCG") })
public class TemplateCriteriaGeneration {
	@Id
	@Column(name = "\"TEMPLATE.ID\"", columnDefinition = "VARCHAR(32)")
	private String templateID;

	@Id
	@Column(name = "\"FIELD.ID\"", columnDefinition = "VARCHAR(32)")
	private String fieldID;

	@Column(name = "\"SEQ\"", columnDefinition = "INTEGER")
	private Integer seq;

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

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Fields getField() {
		return field;
	}

	public void setField(Fields field) {
		this.field = field;
	}

}
