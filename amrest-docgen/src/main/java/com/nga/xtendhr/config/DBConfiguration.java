package com.nga.xtendhr.config;

public class DBConfiguration {
	public static final String SCHEMA_NAME = "AMREST_DOC_GENERATION";
	public static final String ARTIFACT_PATH = "com.amrest.docgeneration";

	public static final String TABLE_PATH = ".db::Table.";

	public static final String COUNTRIES = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_COUNTRIES\"";
	public static final String MAP_COUNTRY_COMPANY_GROUP = "\"" + ARTIFACT_PATH + TABLE_PATH
			+ "DGEN_MAP_COUNTRY_COMPANY_GROUP\"";
	public static final String GROUPS = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_GROUPS\"";
	public static final String TEMPLATES = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_TEMPLATES\"";
	public static final String MAP_GROUP_TEMPLATES = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_MAP_GROUP_TEMPLATES\"";
	public static final String TEMPLATE_CRITERIA_GENERATION = "\"" + ARTIFACT_PATH + TABLE_PATH
			+ "DGEN_TEMPLATE_CRITERIA_GENERATION\"";
	public static final String FIELDS = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGENC_FIELDS\"";
	public static final String ENTITIES = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGENC_ENTITIES\"";
	public static final String RULES = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGENC_RULES\"";
	public static final String MAP_RULE_FIELDS = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGENC_MAP_RULE_FIELDS\"";
	public static final String MAP_TEMPLATE_FIELDS = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_MAP_TEMPLATE_FILEDS\"";
	public static final String CODELIST = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_CODELIST\"";
	public static final String CODELIST_TEXT = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_CODELIST_TEXT\"";
	public static final String CONFIGURABLE_TABLES = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_CONFIGURABLE_TABLES\"";
	public static final String CONFIGURABLE_COLUMNS = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_CONFIGURABLE_COLUMNS\"";
	public static final String TEMPLATE_FIELD_TAG = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_TEMPALTE_FIELD_TAG\"";
	public static final String COUNTRY_SPECIFIC_FIELDS = "\"" + ARTIFACT_PATH + TABLE_PATH
			+ "DGEN_COUNTRY_SPECIFIC_FIELDS\"";
	public static final String SF_DATA_MAPPING = "\"" + ARTIFACT_PATH + TABLE_PATH + "DGEN_SF_DATA_MAPPING\"";
}
