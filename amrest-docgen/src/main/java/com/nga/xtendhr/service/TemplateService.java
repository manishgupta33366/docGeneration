package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.Templates;

public interface TemplateService {
	public Templates create(Templates item);

	public Templates update(Templates item);

	public void delete(Templates item);

	public List<Templates> findByIdAndCriteria(String id, String criteria);
}
