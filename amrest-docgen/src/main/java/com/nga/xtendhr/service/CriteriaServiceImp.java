package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Criteria;

@Transactional
@Component
public class CriteriaServiceImp implements CriteriaService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public Criteria create(Criteria item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public Criteria update(Criteria item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(Criteria item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Criteria> findAll() {
		Query query = em.createNamedQuery("Criteria.selectAll");
		List<Criteria> items = query.getResultList();
		return items;
	}
}
