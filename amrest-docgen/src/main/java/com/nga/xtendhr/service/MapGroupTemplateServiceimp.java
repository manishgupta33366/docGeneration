package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.MapGroupTemplates;

@Transactional
@Component
public class MapGroupTemplateServiceimp implements MapGroupTemplatesService {

	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public MapGroupTemplates create(MapGroupTemplates item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public MapGroupTemplates update(MapGroupTemplates item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(MapGroupTemplates item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapGroupTemplates> findByGroupID(String groupID) {
		Query query = em.createNamedQuery("MapGroupTemplates.findByGroupID").setParameter("groupID", groupID);
		List<MapGroupTemplates> items = query.getResultList();
		return items;
	}

}
