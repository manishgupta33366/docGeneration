package com.nga.xtendhr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nga.xtendhr.model.Text;

@Transactional
@Component
public class TextServiceImp implements TextService {

	@PersistenceContext
	EntityManager em;

	@Override
	public Text create(Text item) {
		em.persist(item);
		return item;
	}

	@Override
	public Text update(Text item) {
		em.merge(item);
		return item;
	}

	@Override
	public void delete(Text item) {
		em.remove(item);
	}

	@Override
	public List<Text> findByRefrencedIdLocale(String id, String locale) {
		Query query = em.createNamedQuery("Text.findByRefrencedIdLocale").setParameter("referencedID", id)
				.setParameter("locale", locale);
		@SuppressWarnings("unchecked")
		List<Text> items = query.getResultList();
		return items;
	}
}
