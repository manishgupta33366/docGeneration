package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.TemplateFieldTag;

@Transactional
@Component
public class TemplateFieldTagServiceImp implements TemplateFieldTagService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public TemplateFieldTag create(TemplateFieldTag item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public TemplateFieldTag update(TemplateFieldTag item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(TemplateFieldTag item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TemplateFieldTag> findAll() {
		Query query = em.createNamedQuery("TemplateFieldTag.selectAll");
		List<TemplateFieldTag> items = query.getResultList();
		return items;
	}
}
