package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.Rules;

public interface RulesService {
	public Rules create(Rules item);

	public Rules update(Rules item);

	public void delete(Rules item);

	public List<Rules> findByRuleID(String ruleID);
}
