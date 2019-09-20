package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.MapFieldOperators;

public interface MapFieldOperatorsService {
	public MapFieldOperators create(MapFieldOperators item);

	public MapFieldOperators update(MapFieldOperators item);

	public void delete(MapFieldOperators item);

	public List<MapFieldOperators> findAll();

	List<MapFieldOperators> findByFieldId(String fieldId);
}
