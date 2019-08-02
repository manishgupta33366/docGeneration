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
 * TableName: DGEN_COUNTRY_SPECIFIC_FIELDS
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.COUNTRY_SPECIFIC_FIELDS, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({
		@NamedQuery(name = "CountrySpecificFields.findByType", query = "SELECT CSF FROM CountrySpecificFields CSF WHERE CSF.type = :type") })
public class CountrySpecificFields {
	@Id
	@Column(name = "\"COUNTRY.ID\"", columnDefinition = "VARCHAR(32)")
	private String countryId;

	@Id
	@Column(name = "\"FIELD.ID\"", columnDefinition = "VARCHAR(32)")
	private String fieldId;

	@Column(name = "\"TYPE\"", columnDefinition = "VARCHAR(32)")
	private String type;

	@Column(name = "\"SEQ\"", columnDefinition = "VARCHAR(32)")
	private String seq;

	@Column(name = "\"DESCRIPTION\"", columnDefinition = "VARCHAR(32)")
	private String description;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"FIELD.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Fields field;

	public Fields getField() {
		return field;
	}

	public void setField(Fields field) {
		this.field = field;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
