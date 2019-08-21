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
 * TableName: DGENC_ENTITIES
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.ENTITIES, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({ @NamedQuery(name = "Entites.selectAll", query = "SELECT E FROM Fields E"),
		@NamedQuery(name = "Entites.findAllDependant", query = "SELECT E FROM Entities E WHERE E.dependantOn = :entityID"),
		@NamedQuery(name = "Entites.getDistinctNames", query = "SELECT DISTINCT E.name FROM Entities E WHERE E.isDependant=false") })
public class Entities {
	@Id
	@Column(name = "\"ID\"", columnDefinition = "VARCHAR(32)")
	private String id;

	@Column(name = "\"NAME\"", columnDefinition = "VARCHAR(64)")
	private String name;

	@Column(name = "\"IS_DEPENDANT\"", columnDefinition = "BOOLEAN")
	private Boolean isDependant;

	@Column(name = "\"DEPENDANT_ON\"", columnDefinition = "VARCHAR(32)")
	private String dependantOn;

	@Column(name = "\"FILTER\"", columnDefinition = "VARCHAR(128)")
	private String filter;

	@Column(name = "\"EXPAND_PATH\"", columnDefinition = "VARCHAR(320)")
	private String expandPath;

	@Column(name = "\"DESCRIPTION\"", columnDefinition = "VARCHAR(128)")
	private String description;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"DEPENDANT_ON\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Entities dependantOnEntity;

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

	public Boolean getIsDependant() {
		return isDependant;
	}

	public void setIsDependant(Boolean isDependant) {
		this.isDependant = isDependant;
	}

	public String getDependantOnEntityID() {
		return dependantOn;
	}

	public Entities getDependantOnEntity() {
		return dependantOnEntity;
	}

	public void setDependantOnEntity(Entities dependantOnEntity) {
		this.dependantOnEntity = dependantOnEntity;
	}

	public void setDependantOnEntityID(String dependantOn) {
		this.dependantOn = dependantOn;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getExpandPath() {
		return expandPath;
	}

	public void setExpandPath(String expandPath) {
		this.expandPath = expandPath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
