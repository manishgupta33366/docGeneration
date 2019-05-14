package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Rules;

@Transactional
@Component
public class RulesServiceImp implements RulesService {

	@PersistenceContext
	EntityManager em;

	@Override
	public Rules create(Rules item) {
		em.persist(item);
		return item;
	}

	@Override
	public Rules update(Rules item) {
		em.merge(item);
		return item;
	}

	@Override
	public void delete(Rules item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Rules> findByRuleID(String id) {
		Query query = em.createNamedQuery("Rules.findByID").setParameter("id", id);
		List<Rules> items = query.getResultList();
		return items;
	}
}
