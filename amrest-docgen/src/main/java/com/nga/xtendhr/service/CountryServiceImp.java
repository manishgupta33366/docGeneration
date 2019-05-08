package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Countries;

@Transactional
@Component
public class CountryServiceImp implements CountryService {

	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public Countries create(Countries item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public Countries update(Countries item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(Countries item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Countries> findAll() {
		Query query = em.createNamedQuery("Countries.selectAll");
		List<Countries> items = query.getResultList();
		return items;
	}

	@Override
	public Countries findById(String id) {
		Countries item = em.find(Countries.class, id);
		return item;
	}

}
