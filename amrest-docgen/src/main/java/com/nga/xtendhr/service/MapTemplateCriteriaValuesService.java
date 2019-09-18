package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.MapTemplateCriteriaValues;

public interface MapTemplateCriteriaValuesService {
	public MapTemplateCriteriaValues create(MapTemplateCriteriaValues item);

	public MapTemplateCriteriaValues update(MapTemplateCriteriaValues item);

	public void delete(MapTemplateCriteriaValues item);

	public List<MapTemplateCriteriaValues> findByTemplate(String templateId);

	public List<MapTemplateCriteriaValues> findAll();
}
