package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Entities;

@Transactional
@Component
public class EntitiesServiceImp implements EntitiesService {

	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public Entities create(Entities item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public Entities update(Entities item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(Entities item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Entities> findAll() {
		Query query = em.createNamedQuery("Entites.selectAll");
		List<Entities> items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Entities> findAllDependant(String entityID) {
		Query query = em.createNamedQuery("Entites.findAllDependant").setParameter("entityID", entityID);
		List<Entities> items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getDistinctNames() {
		Query query = em.createNamedQuery("Entites.getDistinctNames");
		List<String> items = query.getResultList();
		return items;
	}

}
