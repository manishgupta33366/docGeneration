package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.MapCriteriaFields;

public interface MapCriteriaFieldsService {
	public MapCriteriaFields create(MapCriteriaFields item);

	public MapCriteriaFields update(MapCriteriaFields item);

	public void delete(MapCriteriaFields item);

	public List<MapCriteriaFields> findAll();

}
