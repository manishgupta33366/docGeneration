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
	@Column(name = "\"TEMPALTE_FIELD_TAG.ID\"", columnDefinition = "VARCHAR(32)")
	private String templateFieldTagId;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"TEMPALTE_FIELD_TAG.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private TemplateFieldTag templateFiledTag;

	public String getTemplateID() {
		return templateID;
	}

	public void setTemplateID(String templateID) {
		this.templateID = templateID;
	}

	public String getTemplateFieldTagId() {
		return templateFieldTagId;
	}

	public void setTemplateFieldTagId(String templateFieldTagId) {
		this.templateFieldTagId = templateFieldTagId;
	}

	public TemplateFieldTag getTemplateFiledTag() {
		return templateFiledTag;
	}

	public void setTemplateFiledTag(TemplateFieldTag templateFiledTag) {
		this.templateFiledTag = templateFiledTag;
	}
}
