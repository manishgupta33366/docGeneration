package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.SFDataMapping;

@Transactional
@Component
public class SFDataMappingImp implements SFDataMappingService {
	@PersistenceContext
	EntityManager em;

	@Override
	public SFDataMapping create(SFDataMapping item) {
		em.persist(item);
		return item;
	}

	@Override
	public SFDataMapping update(SFDataMapping item) {
		em.merge(item);
		return item;
	}

	@Override
	public void delete(SFDataMapping item) {
		em.remove(item);
	}

	@Override
	public List<SFDataMapping> findByKey(String key) {
		Query query = em.createNamedQuery("SFDataMapping.findByKey").setParameter("key", key);
		@SuppressWarnings("unchecked")
		List<SFDataMapping> items = query.getResultList();
		return items;
	}
}
