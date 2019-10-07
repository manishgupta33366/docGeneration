package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.TemplateFieldTag;

public interface TemplateFieldTagService {
	public TemplateFieldTag create(TemplateFieldTag item);

	public TemplateFieldTag update(TemplateFieldTag item);

	public void delete(TemplateFieldTag item);

	public List<TemplateFieldTag> findAll();
}
