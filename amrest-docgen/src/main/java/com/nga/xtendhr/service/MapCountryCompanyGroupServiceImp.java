package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.MapCountryCompanyGroup;

@Transactional
@Component
public class MapCountryCompanyGroupServiceImp implements MapCountryCompanyGroupService {

	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public MapCountryCompanyGroup create(MapCountryCompanyGroup item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public MapCountryCompanyGroup update(MapCountryCompanyGroup item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(MapCountryCompanyGroup item) {
		em.remove(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapCountryCompanyGroup> findByCountryCompany(String countryID, String companyID, Boolean isManager) {
		Query query;
		List<MapCountryCompanyGroup> items;
		if (isManager) {
			query = em.createNamedQuery("MapCountryCompanyGroup.findByCountryCompany_Manager")
					.setParameter("countryID", countryID).setParameter("companyID", companyID)
					.setParameter("isMssRelevant", true);
			items = query.getResultList();
			return items;
		}
		query = em.createNamedQuery("MapCountryCompanyGroup.findByCountryCompany_Employee")
				.setParameter("countryID", countryID).setParameter("companyID", companyID)
				.setParameter("isEssRelevant", true);
		items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapCountryCompanyGroup> findByGroupCountryCompany(String groupID, String countryID, String companyID,
			Boolean isManager) {
		Query query;
		List<MapCountryCompanyGroup> items;
		if (isManager) {
			query = em.createNamedQuery("MapCountryCompanyGroup.findByGroupCountryCompany_Manager")
					.setParameter("groupID", groupID).setParameter("countryID", countryID)
					.setParameter("companyID", companyID).setParameter("isMssRelevant", true);
			items = query.getResultList();
			return items;
		}
		query = em.createNamedQuery("MapCountryCompanyGroup.findByGroupCountryCompany_Employee")
				.setParameter("groupID", groupID).setParameter("countryID", countryID)
				.setParameter("companyID", companyID).setParameter("isEssRelevant", true);
		items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapCountryCompanyGroup> findByCountryCompanyAdmin(String countryID, String companyID) {
		Query query;
		List<MapCountryCompanyGroup> items;
		query = em.createNamedQuery("MapCountryCompanyGroup.findByCountryCompany_Admin")
				.setParameter("countryID", countryID).setParameter("companyID", companyID);
		items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapCountryCompanyGroup> findByGroupCountryCompany(String groupID, String countryID, String companyID) {
		Query query;
		List<MapCountryCompanyGroup> items;
		query = em.createNamedQuery("MapCountryCompanyGroup.findByGroupCountryCompany_Admin")
				.setParameter("groupID", groupID).setParameter("countryID", countryID)
				.setParameter("companyID", companyID);
		items = query.getResultList();
		return items;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MapCountryCompanyGroup> findByCountry(String countryID) {
		Query query;
		List<MapCountryCompanyGroup> items;
		query = em.createNamedQuery("MapCountryCompanyGroup.findByCountry").setParameter("countryID", countryID);
		items = query.getResultList();
		return items;
	}

}