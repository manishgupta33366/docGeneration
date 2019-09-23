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
 * TableName: DGENC_MAP_FIELD_OPERATORS
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.MAP_FIELD_OPERATORS, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({ @NamedQuery(name = "MapFieldOperators.selectAll", query = "SELECT MFO FROM MapFieldOperators MFO"),
		@NamedQuery(name = "MapFieldOperators.findByFieldId", query = "SELECT MFO FROM MapFieldOperators MFO WHERE MFO.fieldId = :fieldId") })
public class MapFieldOperators {
	@Id
	@Column(name = "\"FIELD.ID\"", columnDefinition = "VARCHAR(32)")
	private String fieldId;

	@Id
	@Column(name = "\"OPERATOR.ID\"", columnDefinition = "VARCHAR(32)")
	private String operatorId;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"OPERATOR.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Operators operator;

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public Operators getOperator() {
		return operator;
	}

	public void setOperator(Operators operator) {
		this.operator = operator;
	}

	public String toString() {// overriding the toString() method
		JSONObject obj = new JSONObject();
		obj.put("fieldId", this.getFieldId());
		obj.put("operator", this.getOperator().toString());
		return obj.toString();
	}
}
