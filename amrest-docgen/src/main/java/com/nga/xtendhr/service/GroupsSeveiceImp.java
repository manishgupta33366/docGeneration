package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Groups;

@Transactional
@Component
public class GroupsSeveiceImp implements GroupsService {

	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public Groups create(Groups item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public Groups update(Groups item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(Groups item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Groups> findAll() {
		Query query = em.createNamedQuery("Groups.selectAll");
		List<Groups> items = query.getResultList();
		return items;
	}
}
