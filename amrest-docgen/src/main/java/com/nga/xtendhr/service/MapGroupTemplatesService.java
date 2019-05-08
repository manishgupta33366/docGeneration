package com.nga.xtendhr.service;

import java.util.List;

import com.nga.xtendhr.model.MapGroupTemplates;

public interface MapGroupTemplatesService {

	public MapGroupTemplates create(MapGroupTemplates item);

	public MapGroupTemplates update(MapGroupTemplates item);

	public void delete(MapGroupTemplates item);

	public List<MapGroupTemplates> findByGroupID(String groupID);
}
