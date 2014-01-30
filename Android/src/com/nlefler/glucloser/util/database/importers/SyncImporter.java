package com.nlefler.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.nlefler.glucloser.util.database.DatabaseUtil;


public abstract class SyncImporter {
	private static final String LOG_TAG = "Pump_Sync_Importer";
	
	private DatabaseUtil dbUtil = DatabaseUtil.instance();

	/**
	 * Imports records from Parse to local db for a specific table
	 * @param List<ParseObject> objects
	 * @return Date the creation date of the last object synced
	 */
	public abstract Date importRecords(List<Map<String, Object>> objects);
	
	protected ContentValues getCommonValuesForTableIntoValuesFromMap(
			String tableName, ContentValues values, Map<String, Object> map) {
		values.put(DatabaseUtil.localKeyForNetworkKey(tableName, DatabaseUtil.OBJECT_ID_COLUMN_NAME),
				(String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME));
		values.put(DatabaseUtil.localKeyForNetworkKey(tableName, DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(((Date)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME))));
		values.put(DatabaseUtil.localKeyForNetworkKey(tableName, DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(((Date)map.get(DatabaseUtil.UPDATED_AT_COLUMN_NAME))));
		values.put(DatabaseUtil.localKeyForNetworkKey(tableName, DatabaseUtil.DATA_VERSION_COLUMN_NAME), 
				((Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME)));
		values.put(DatabaseUtil.localKeyForNetworkKey(tableName, DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				false);
		
		return values;
	}
	
	private static final String firstHalfOfWhereClause = 
			DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=? AND " +
					"?>coalesce((SELECT " +
					DatabaseUtil.DATA_VERSION_COLUMN_NAME + " FROM ";
	private static final String secondHalfOfWhereClause =
			" WHERE " +
					DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?), -1)";
	protected static String getUpsertWhereClauseForTable(String tableName) {
		return firstHalfOfWhereClause + tableName + secondHalfOfWhereClause;
	}
	
	protected String[] getArgsForUpsertWhereClauseFromMapForTable(Map<String, Object> map, String tableName) {
		String id = (String)map.get(DatabaseUtil.localKeyForNetworkKey(
				tableName, DatabaseUtil.OBJECT_ID_COLUMN_NAME));
		String version = String.valueOf((Integer)map.get(
				DatabaseUtil.localKeyForNetworkKey(tableName, DatabaseUtil.DATA_VERSION_COLUMN_NAME)));
		
		return new String[] {id, version, id};
	}
	
	protected Date upsertRecord(ContentValues values, String whereClause, 
			Map<String, Object> record, String tableName, Date lastSyncDate, String logTag) {
		
		long code = dbUtil.upsert(tableName, values,
				whereClause, getArgsForUpsertWhereClauseFromMapForTable(
						record, tableName));
		if (code != -1) {
			if (lastSyncDate == null) {
				Log.v(LOG_TAG, "Updating last sync time with " + (Date)record.get(DatabaseUtil.localKeyForNetworkKey(
						tableName, DatabaseUtil.UPDATED_AT_COLUMN_NAME)));
				return (Date)record.get(DatabaseUtil.localKeyForNetworkKey(
						tableName, DatabaseUtil.UPDATED_AT_COLUMN_NAME));
			} else if (((Date)record.get(DatabaseUtil.localKeyForNetworkKey(
					tableName, DatabaseUtil.UPDATED_AT_COLUMN_NAME))).compareTo(lastSyncDate) >= 0) {
				Log.v(LOG_TAG, "Updating last sync time with " + (Date)record.get(DatabaseUtil.localKeyForNetworkKey(
						tableName, DatabaseUtil.UPDATED_AT_COLUMN_NAME)));
				return (Date)record.get(DatabaseUtil.localKeyForNetworkKey(
						tableName, DatabaseUtil.UPDATED_AT_COLUMN_NAME));
			} else {
				Log.v(LOG_TAG, "Not updating last sync date, " + (Date)record.get(DatabaseUtil.localKeyForNetworkKey(
					tableName, DatabaseUtil.UPDATED_AT_COLUMN_NAME)) + " is before " + lastSyncDate);
			}
		} else {
			Log.w(logTag, "Error code recieved from insert: " + code);
			Log.w(logTag, "Values are: " + values);
		}
		
		return lastSyncDate;
	}
}
