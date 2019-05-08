package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.MapRuleFields;

@Transactional
@Component
public class MapRuleFieldsServiceImp implements MapRuleFieldsService {

	@PersistenceContext
	EntityManager em;

	@Override
	public MapRuleFields create(MapRuleFields item) {
		em.persist(item);
		return item;
	}

	@Override
	public MapRuleFields update(MapRuleFields item) {
		em.merge(item);
		return item;
	}

	@Override
	public void delete(MapRuleFields item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapRuleFields> findByRuleID(String ruleID) {
		Query query = em.createNamedQuery("MapRuleFields.findByRuleID").setParameter("ruleID", ruleID);
		List<MapRuleFields> items = query.getResultList();
		return items;
	}
}
