package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.Operators;

public interface OperatorsService {
	public Operators create(Operators item);

	public Operators update(Operators item);

	public void delete(Operators item);

	public List<Operators> findAll();

}
