package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.Groups;

public interface GroupsService {
	public Groups create(Groups item);

	public Groups update(Groups item);

	public void delete(Groups item);

	public List<Groups> findAll();

}
