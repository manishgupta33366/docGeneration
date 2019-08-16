package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Fields;
import com.nga.xtendhr.model.TemplateCriteriaGeneration;

@Transactional
@Component
public class TemplateCriteriaGenerationServiceImp implements TemplateCriteriaGenerationService {
	@PersistenceContext
	EntityManager em;

	@Override
	public TemplateCriteriaGeneration create(TemplateCriteriaGeneration item) {
		em.persist(item);
		return item;
	}

	@Override
	public TemplateCriteriaGeneration update(TemplateCriteriaGeneration item) {
		em.merge(item);
		return item;
	}

	@Override
	public void delete(TemplateCriteriaGeneration item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TemplateCriteriaGeneration> findByTemplateID(String templateID) {
		Query query = em.createNamedQuery("TemplateCriteriaGeneration.findByTemplateID").setParameter("templateID",
				templateID);
		List<TemplateCriteriaGeneration> items = query.getResultList();
		return items;
	}

	@Override
	public List<Fields> getDistinctFields() {
		Query query = em.createNamedQuery("TemplateCriteriaGeneration.getDistinctFields");
		@SuppressWarnings("unchecked")
		List<Fields> items = query.getResultList();
		return items;
	}
}
