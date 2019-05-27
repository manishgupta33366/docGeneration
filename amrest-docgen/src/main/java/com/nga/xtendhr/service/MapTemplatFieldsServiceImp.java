package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.MapTemplateFields;

@Transactional
@Component
public class MapTemplatFieldsServiceImp implements MapTemplateFieldsService {

	@PersistenceContext
	EntityManager em;

	@Override
	public MapTemplateFields create(MapTemplateFields item) {
		em.persist(item);
		return item;
	}

	@Override
	public MapTemplateFields update(MapTemplateFields item) {
		em.merge(item);
		return item;
	}

	@Override
	public void delete(MapTemplateFields item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapTemplateFields> findByTemplateID(String templateID) {
		Query query = em.createNamedQuery("MapTemplateFields.findBytemplateID").setParameter("templateID", templateID);
		List<MapTemplateFields> items = query.getResultList();
		return items;
	}
}
