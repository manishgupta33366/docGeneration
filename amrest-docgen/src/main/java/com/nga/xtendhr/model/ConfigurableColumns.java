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
 * TableName: DGEN_CONFIGURABLE_COLUMNS
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.CONFIGURABLE_COLUMNS, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({
		@NamedQuery(name = "ConfigurableColumns.findByTableID", query = "SELECT CC FROM ConfigurableColumns CC WHERE CC.tableID = :tableID"),
		@NamedQuery(name = "ConfigurableColumns.getColumnNamesByTableID", query = "SELECT CC.columnName FROM ConfigurableColumns CC WHERE CC.tableID = :tableID") })
public class ConfigurableColumns {

	@Id
	@Column(name = "\"TABLE.ID\"", columnDefinition = "VARCHAR(32)")
	private String tableID;

	@Id
	@Column(name = "\"COLUMN_NAME\"", columnDefinition = "VARCHAR(32)")
	private String columnName;

	@Column(name = "\"EDITABLE\"", columnDefinition = "BOOLEAN")
	private Boolean editable;

	@Column(name = "\"TYPE\"", columnDefinition = "VARCHAR(32)")
	private String type;

	@Column(name = "\"IS_DEPENDANT\"", columnDefinition = "BOOLEAN")
	private Boolean isDependant;

	@Column(name = "\"DEPENDANT_ON_TABLE.ID\"", columnDefinition = "VARCHAR(32)")
	private String dependantOnTableID;

	@Column(name = "\"DEPENDANT_ON_COLUMN\"", columnDefinition = "VARCHAR(32)")
	private String dependantOnColumn;

	@Column(name = "\"DESCRIPTION\"", columnDefinition = "VARCHAR(128)")
	private String description;

	public String getTableID() {
		return tableID;
	}

	public void setTableID(String tableID) {
		this.tableID = tableID;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getIsDependant() {
		return isDependant;
	}

	public void setIsDependant(Boolean isDependant) {
		this.isDependant = isDependant;
	}

	public String getDependantOnTableID() {
		return dependantOnTableID;
	}

	public void setDependantOnTableID(String dependantOnTableID) {
		this.dependantOnTableID = dependantOnTableID;
	}

	public String getDependantOnColumn() {
		return dependantOnColumn;
	}

	public void setDependantOnColumn(String dependantOnColumn) {
		this.dependantOnColumn = dependantOnColumn;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
