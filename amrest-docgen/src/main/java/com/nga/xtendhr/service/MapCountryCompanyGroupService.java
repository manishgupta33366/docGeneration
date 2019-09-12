package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.MapCountryCompanyGroup;

public interface MapCountryCompanyGroupService {

	public MapCountryCompanyGroup create(MapCountryCompanyGroup item);

	public MapCountryCompanyGroup update(MapCountryCompanyGroup item);

	public void delete(MapCountryCompanyGroup item);

	public List<MapCountryCompanyGroup> findByCountry(String countryID);

	public List<MapCountryCompanyGroup> findByCountryCompany(String countryID, String companyID, Boolean isManager);

	public List<MapCountryCompanyGroup> findByGroupCountryCompany(String groupID, String countryID, String companyID,
			Boolean isManager);

	public List<MapCountryCompanyGroup> findByCountryCompanyAdmin(String countryID, String companyID);

	public List<MapCountryCompanyGroup> findByGroupCountryCompany(String groupID, String countryID, String companyID);

}
