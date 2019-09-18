package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.MapTemplateCriteriaValues;

@Transactional
@Component
public class MapTemplateCriteriaValuesServiceImp implements MapTemplateCriteriaValuesService {

	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public MapTemplateCriteriaValues create(MapTemplateCriteriaValues item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public MapTemplateCriteriaValues update(MapTemplateCriteriaValues item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(MapTemplateCriteriaValues item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapTemplateCriteriaValues> findByTemplate(String templateId) {
		Query query;
		List<MapTemplateCriteriaValues> items;
		query = em.createNamedQuery("MapTemplateCriteriaValues.findByTemplate").setParameter("templateId", templateId);
		items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapTemplateCriteriaValues> findAll() {
		Query query = em.createNamedQuery("MapTemplateCriteriaValues.selectAll");
		List<MapTemplateCriteriaValues> items = query.getResultList();
		return items;
	}
}
