package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.MapFieldOperators;

@Transactional
@Component
public class MapFieldOperatorsServiceImp implements MapFieldOperatorsService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public MapFieldOperators create(MapFieldOperators item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public MapFieldOperators update(MapFieldOperators item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(MapFieldOperators item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapFieldOperators> findAll() {
		Query query = em.createNamedQuery("MapFieldOperators.selectAll");
		List<MapFieldOperators> items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapFieldOperators> findByFieldId(String fieldId) {
		Query query = em.createNamedQuery("MapFieldOperators.findByFieldId").setParameter("fieldId", fieldId);
		List<MapFieldOperators> items = query.getResultList();
		return items;
	}
}
