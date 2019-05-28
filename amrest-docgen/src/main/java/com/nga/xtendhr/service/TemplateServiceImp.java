package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Templates;

@Transactional
@Component
public class TemplateServiceImp implements TemplateService {
	@PersistenceContext
	EntityManager em;

	@Override
	public Templates create(Templates item) {
		em.persist(item);
		return item;
	}

	@Override
	public Templates update(Templates item) {
		em.merge(item);
		return item;
	}

	@Override
	public void delete(Templates item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Templates> findByIdAndCriteria(String id, String criteria) {
		Query query = em.createNamedQuery("Templates.findByTemplateAndCriteria").setParameter("criteria", criteria)
				.setParameter("id", id);
		List<Templates> items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Templates> findById(String id) {
		Query query = em.createNamedQuery("Templates.findById").setParameter("id", id);
		List<Templates> items = query.getResultList();
		return items;
	}

}
