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
 * TableName: DGEN_MAP_GROUP_TEMPLATES
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.MAP_GROUP_TEMPLATES, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({
		@NamedQuery(name = "MapGroupTemplates.findByGroupID", query = "SELECT MGT FROM MapGroupTemplates MGT WHERE MGT.groupID = :groupID") })

public class MapGroupTemplates {
	@Id
	@Column(name = "\"GRP.ID\"", columnDefinition = "VARCHAR(32)")
	private String groupID;

	@Id
	@Column(name = "\"TEMPLATE.ID\"", columnDefinition = "VARCHAR(32)")
	private String templateID;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"TEMPLATE.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Templates template;

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getTemplateID() {
		return templateID;
	}

	public void setTemplateID(String templateID) {
		this.templateID = templateID;
	}

	public Templates getTemplate() {
		return template;
	}

	public void setTemplate(Templates template) {
		this.template = template;
	}

}
