package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.ConfigurableTables;

@Transactional
@Component
public class ConfigurableTablesServiceImp implements ConfigurableTablesService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public ConfigurableTables create(ConfigurableTables item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public ConfigurableTables update(ConfigurableTables item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(ConfigurableTables item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ConfigurableTables> findAll() {
		Query query = em.createNamedQuery("ConfigurableTables.findAll");
		List<ConfigurableTables> items = query.getResultList();
		return items;
	}

	@Override
	public ConfigurableTables findById(String id) {
		ConfigurableTables item = em.find(ConfigurableTables.class, id);
		return item;
	}
}
