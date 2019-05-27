package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.MapTemplateFields;

public interface MapTemplateFieldsService {

	public MapTemplateFields create(MapTemplateFields item);

	public MapTemplateFields update(MapTemplateFields item);

	public void delete(MapTemplateFields item);

	public List<MapTemplateFields> findByTemplateID(String templateID);
}
