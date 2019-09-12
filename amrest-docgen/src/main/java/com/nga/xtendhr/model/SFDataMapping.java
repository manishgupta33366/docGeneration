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
 * TableName: DGEN_SF_DATA_MAPPING
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */
@NamedQueries({
		@NamedQuery(name = "SFDataMapping.findByKey", query = "SELECT SFDM FROM SFDataMapping SFDM WHERE SFDM.key = :key") })
@Entity
@Table(name = DBConfiguration.SF_DATA_MAPPING, schema = DBConfiguration.SCHEMA_NAME)
public class SFDataMapping {

	@Id
	@Column(name = "\"KEY\"", columnDefinition = "VARCHAR(32)")
	private String key;

	@Column(name = "\"DATA\"", columnDefinition = "VARCHAR(64)")
	private String data;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
