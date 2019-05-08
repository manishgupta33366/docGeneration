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
@NamedQueries({
		@NamedQuery(name = "MapRuleFields.findByRuleID", query = "SELECT MRF FROM MapRuleFields MRF WHERE MRF.ruleID = :ruleID ORDER BY MRF.seq") })
@Table(name = DBConfiguration.MAP_RULE_FIELDS, schema = DBConfiguration.SCHEMA_NAME)
public class MapRuleFields {
	@Id
	@Column(name = "\"ID\"", columnDefinition = "VARCHAR(32)")
	private String id;

	@Column(name = "\"RULE.ID\"", columnDefinition = "VARCHAR(32)")
	private String ruleID;

	@Column(name = "\"SEQ\"", columnDefinition = "INTEGER")
	private Integer seq;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"RULE.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Rules rule;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"FIELD.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Fields field;

	public String getId() {
		return id;
	}

	public Fields getField() {
		return field;
	}

	public void setField(Fields field) {
		this.field = field;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRuleID() {
		return ruleID;
	}

	public void setRuleID(String ruleID) {
		this.ruleID = ruleID;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Rules getRule() {
		return rule;
	}

	public void setRule(Rules rule) {
		this.rule = rule;
	}

}
