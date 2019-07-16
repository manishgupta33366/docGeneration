package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.CodelistText;

public interface CodelistTextService {
	public CodelistText create(CodelistText item);

	public CodelistText update(CodelistText item);

	public void delete(CodelistText item);

	public List<CodelistText> findByCodelistLanguage(String codeListID, String language);
}
