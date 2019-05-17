package com.nga.xtendhr.model;

import java.util.Date;

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
 * TableName: DGEN_MAP_COUNTRY_COMPANY_GROUP
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.MAP_COUNTRY_COMPANY_GROUP, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({
		@NamedQuery(name = "MapCountryCompanyGroup.findByCountryCompany_Manager", query = "SELECT MCCG FROM MapCountryCompanyGroup MCCG WHERE MCCG.countryID = :countryID AND MCCG.companyID = :companyID AND MCCG.isMssRelevant = :isMssRelevant"),
		@NamedQuery(name = "MapCountryCompanyGroup.findByCountryCompany_Employee", query = "SELECT MCCG FROM MapCountryCompanyGroup MCCG WHERE MCCG.countryID = :countryID AND MCCG.companyID = :companyID AND MCCG.isEssRelevant = :isEssRelevant"),
		@NamedQuery(name = "MapCountryCompanyGroup.findByGroupCountryCompany_Manager", query = "SELECT MCCG FROM MapCountryCompanyGroup MCCG WHERE MCCG.groupID = :groupID AND MCCG.countryID = :countryID AND MCCG.companyID = :companyID AND MCCG.isMssRelevant = :isMssRelevant"),
		@NamedQuery(name = "MapCountryCompanyGroup.findByGroupCountryCompany_Employee", query = "SELECT MCCG FROM MapCountryCompanyGroup MCCG WHERE MCCG.groupID = :groupID AND MCCG.countryID = :countryID AND MCCG.companyID = :companyID AND MCCG.isEssRelevant = :isEssRelevant") })

public class MapCountryCompanyGroup {
	@Id
	@Column(name = "\"COUNTRY.ID\"", columnDefinition = "VARCHAR(32)")
	private String countryID;

	@Id
	@Column(name = "\"COMPANY.ID\"", columnDefinition = "VARCHAR(32)")
	private String companyID;

	@Id
	@Column(name = "\"GRP.ID\"", columnDefinition = "VARCHAR(32)")
	private String groupID;

	@Column(name = "\"IS_ESS_RELEVANT\"", columnDefinition = "BOOLEAN")
	private Boolean isEssRelevant;

	@Column(name = "\"IS_MSS_RELEVANT\"", columnDefinition = "BOOLEAN")
	private Boolean isMssRelevant;

	@Column(name = "\"IS_ACTIVE\"", columnDefinition = "BOOLEAN")
	private Boolean isActive;

	@Column(name = "\"SHOW_ON_UI\"", columnDefinition = "BOOLEAN")
	private Boolean showOnUI;

	@Column(name = "\"START_DATE\"", columnDefinition = "SECONDDATE")
	private Date startDate;

	@Column(name = "\"END_DATE\"", columnDefinition = "SECONDDATE")
	private Date endDate;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"GRP.ID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false)
	private Groups group;

	public String getCountryID() {
		return countryID;
	}

	public void setCountryID(String countryID) {
		this.countryID = countryID;
	}

	public String getCompanyID() {
		return companyID;
	}

	public void setCompanyID(String companyID) {
		this.companyID = companyID;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public Boolean getIsEssRelevant() {
		return isEssRelevant;
	}

	public void setIsEssRelevant(Boolean isEssRelevant) {
		this.isEssRelevant = isEssRelevant;
	}

	public Boolean getIsMssRelevant() {
		return isMssRelevant;
	}

	public void setIsMssRelevant(Boolean isMssRelevant) {
		this.isMssRelevant = isMssRelevant;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getShowOnUI() {
		return showOnUI;
	}

	public void setShowOnUI(Boolean showOnUI) {
		this.showOnUI = showOnUI;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Groups getGroup() {
		return group;
	}

	public void setGroup(Groups group) {
		this.group = group;
	}

	public String toString() {// overriding the toString() method
		JSONObject obj = new JSONObject();
		obj.put("id", this.group.getId());
		obj.put("name", this.group.getName());
		obj.put("description", this.group.getDescription());
		return obj.toString();
	}
}
