package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.ConfigurableColumns;
import com.nga.xtendhr.model.Countries;

public interface CountryService {

	public Countries create(Countries item);

	public Countries update(Countries item);

	public void delete(Countries item);

	public List<Countries> findAll();

	public Countries findById(String id);

	public List<Countries> dynamicSelect(List<ConfigurableColumns> requiredColumns);
}
