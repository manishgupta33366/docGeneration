package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.Criteria;

public interface CriteriaService {
	public Criteria create(Criteria item);

	public Criteria update(Criteria item);

	public void delete(Criteria item);

	public List<Criteria> findAll();

	public List<Criteria> findById(String id);
}
