package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.CountrySpecificFields;

@Transactional
@Component
public class CountrySpecificFieldsServiceImp implements CountrySpecificFieldsService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public CountrySpecificFields create(CountrySpecificFields item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public CountrySpecificFields update(CountrySpecificFields item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(CountrySpecificFields item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CountrySpecificFields> findByType(String type) {
		Query query;
		List<CountrySpecificFields> items;
		query = em.createNamedQuery("CountrySpecificFields.findByType").setParameter("type", type);
		items = query.getResultList();
		return items;
	}

}
