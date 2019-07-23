package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.ConfigurableColumns;

@Transactional
@Component
public class ConfigurableColumnsServiceImp implements ConfigurableColumnsService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public ConfigurableColumns create(ConfigurableColumns item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public ConfigurableColumns update(ConfigurableColumns item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(ConfigurableColumns item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ConfigurableColumns> findByTableID(String id) {
		Query query = em.createNamedQuery("ConfigurableColumns.findByTableID").setParameter("tableID", id);
		List<ConfigurableColumns> items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getColumnNamesByTableID(String id) {
		Query query = em.createNamedQuery("ConfigurableColumns.getColumnNamesByTableID").setParameter("tableID", id);
		List<String> items = query.getResultList();
		return items;
	}
}
