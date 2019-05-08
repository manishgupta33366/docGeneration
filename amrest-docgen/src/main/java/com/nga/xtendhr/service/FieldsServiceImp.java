package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Fields;

@Transactional
@Component
public class FieldsServiceImp implements FieldsService {

	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public Fields create(Fields item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public Fields update(Fields item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(Fields item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Fields> findAll() {
		Query query = em.createNamedQuery("Fields.selectAll");
		List<Fields> items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Fields> findByEntity(String entityID) {
		Query query = em.createNamedQuery("Fields.findByEntity").setParameter("entityID", entityID);
		List<Fields> items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Fields> findByID(String id) {
		Query query = em.createNamedQuery("Fields.findByID").setParameter("id", id);
		List<Fields> items = query.getResultList();
		return items;
	}

}
