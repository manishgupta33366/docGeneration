package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.MapCriteriaFields;

@Transactional
@Component
public class MapCriteriaFieldsServiceImp implements MapCriteriaFieldsService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public MapCriteriaFields create(MapCriteriaFields item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public MapCriteriaFields update(MapCriteriaFields item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(MapCriteriaFields item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapCriteriaFields> findAll() {
		Query query = em.createNamedQuery("MapCriteriaFields.selectAll");
		List<MapCriteriaFields> items = query.getResultList();
		return items;
	}
}
