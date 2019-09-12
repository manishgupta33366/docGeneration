package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Companies;

@Transactional
@Component
public class CompaniesServiceImp implements CompaniesService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public Companies create(Companies item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public Companies update(Companies item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(Companies item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Companies> findAll() {
		Query query = em.createNamedQuery("Companies.selectAll");
		List<Companies> items = query.getResultList();
		return items;
	}
}
