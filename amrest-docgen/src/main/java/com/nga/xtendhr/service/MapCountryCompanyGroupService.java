package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.MapCountryCompanyGroup;

public interface MapCountryCompanyGroupService {

	public MapCountryCompanyGroup create(MapCountryCompanyGroup item);

	public MapCountryCompanyGroup update(MapCountryCompanyGroup item);

	public void delete(MapCountryCompanyGroup item);

	public List<MapCountryCompanyGroup> findByCountryCompany(String country, String company, Boolean isManager);

}
