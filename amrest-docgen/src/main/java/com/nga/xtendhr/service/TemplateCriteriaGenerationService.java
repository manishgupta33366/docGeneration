package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.Fields;
import com.nga.xtendhr.model.TemplateCriteriaGeneration;

public interface TemplateCriteriaGenerationService {
	public TemplateCriteriaGeneration create(TemplateCriteriaGeneration item);

	public TemplateCriteriaGeneration update(TemplateCriteriaGeneration item);

	public void delete(TemplateCriteriaGeneration item);

	public List<TemplateCriteriaGeneration> findByTemplateID(String templateID);

	public List<Fields> getDistinctFields();
}
