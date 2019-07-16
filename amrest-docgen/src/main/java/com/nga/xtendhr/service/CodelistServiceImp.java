package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Codelist;

@Transactional
@Component
public class CodelistServiceImp implements CodelistService {

	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public Codelist create(Codelist item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public Codelist update(Codelist item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(Codelist item) {
		em.remove(item);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Codelist> findByFieldAndKey(String fieldID, String sfKey) {
		Query query;
		List<Codelist> items;
		query = em.createNamedQuery("Codelist.findByField_Key").setParameter("fieldID", fieldID).setParameter("sfKey",
				sfKey);
		items = query.getResultList();
		return items;
	}
}
