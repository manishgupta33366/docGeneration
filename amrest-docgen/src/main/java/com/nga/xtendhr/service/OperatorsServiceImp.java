package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Operators;

@Transactional
@Component
public class OperatorsServiceImp implements OperatorsService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public Operators create(Operators item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public Operators update(Operators item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(Operators item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Operators> findAll() {
		Query query = em.createNamedQuery("Operators.selectAll");
		List<Operators> items = query.getResultList();
		return items;
	}

}
