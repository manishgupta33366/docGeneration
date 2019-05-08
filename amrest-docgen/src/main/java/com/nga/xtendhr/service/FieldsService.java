package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.Fields;

public interface FieldsService {

	public Fields create(Fields item);

	public Fields update(Fields item);

	public void delete(Fields item);

	public List<Fields> findAll();

	public List<Fields> findByEntity(String entityID);

	public List<Fields> findByID(String id);
}
