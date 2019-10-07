package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.Text;

public interface TextService {
	public Text create(Text item);

	public Text update(Text item);

	public void delete(Text item);

	public List<Text> findByRefrencedIdLocale(String id, String locale);
}
