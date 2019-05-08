package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.MapRuleFields;

public interface MapRuleFieldsService {
	public MapRuleFields create(MapRuleFields item);

	public MapRuleFields update(MapRuleFields item);

	public void delete(MapRuleFields item);

	public List<MapRuleFields> findByRuleID(String ruleID);
}
