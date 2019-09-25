package com.nga.xtendhr.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.json.JSONObject;

import com.nga.xtendhr.config.DBConfiguration;

/*
 * AppName: DocGen
 * TableName: DGEN_COUNTRIES
 * 
 * @author	:	Manish Gupta  
 * @email	:	manish.g@ngahr.com
 * @version	:	0.0.1
 */

@Entity
@Table(name = DBConfiguration.COUNTRIES, schema = DBConfiguration.SCHEMA_NAME)
@NamedQueries({ @NamedQuery(name = "Countries.selectAll", query = "SELECT co FROM Countries co"),
		@NamedQuery(name = "Countries.selectAllbasedOnLocale", query = "SELECT co FROM Countries co where co.text.locale=:locale") })
public class Countries {

	@Id
	@Column(name = "\"ID\"", columnDefinition = "VARCHAR(32)")
	private String id;

	@Column(name = "\"NAME\"", columnDefinition = "VARCHAR(64)")
	private String name;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "\"ID\"", referencedColumnName = "\"REFRENCED_ID\"", insertable = false, updatable = false)
	private Text text;

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

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	public String toString() {// overriding the toString() method
		JSONObject obj = new JSONObject();
		obj.put("id", this.getId());
		obj.put("name", this.getName());
		obj.put("name_per_locale", this.getText().getName());
		obj.put("description_per_locale", this.getText().getDescription());
		return obj.toString();
	}
}