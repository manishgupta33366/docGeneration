package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.CodelistText;

@Transactional
@Component
public class CodelistTextServiceImp implements CodelistTextService {
	@PersistenceContext
	EntityManager em;

	@Override
	@Transactional
	public CodelistText create(CodelistText item) {
		em.persist(item);
		return item;
	}

	@Override
	@Transactional
	public CodelistText update(CodelistText item) {
		em.merge(item);
		return item;
	}

	@Override
	@Transactional
	public void delete(CodelistText item) {
		em.remove(item);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CodelistText> findByCodelistLanguage(String codelistID, String language) {
		Query query;
		List<CodelistText> items;
		query = em.createNamedQuery("CodelistText.findByCodelist_Language").setParameter("codelistID", codelistID)
				.setParameter("language", language);
		items = query.getResultList();
		return items;
	}
}
