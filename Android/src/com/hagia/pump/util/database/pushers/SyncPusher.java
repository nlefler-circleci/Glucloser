package com.hagia.pump.util.database.pushers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hagia.pump.util.database.DatabaseUtil;

public abstract class SyncPusher {
	private static final String LOG_TAG = "Pump_Sync_Pusher";

	public abstract List<Map<String, Object>> getRecordsSinceDate(Date sinceDate);

	// WHERE strftime('%Y-%m-%dT%H:%M:%fZ', updatedAt) >= strftime('%Y-%m-%dT%H:%M:%fZ', ?)
	// AND needsUpload = 1
	private static final String datedWhereClauseForInternalGetRecordsSinceDate = 
			DatabaseUtil.getStrfStringForString(DatabaseUtil.UPDATED_AT_COLUMN_NAME) + 
			">=" + DatabaseUtil.getStrfStringForString("?") +  ") AND " +
			DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + "=1";
	// WHERE needsUpload = 1
	private static final String whereClauseForInternalGetRecordsSinceDate = 
			DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + "=1";
	// ORDER BY strftime('%Y-%m-%dT%H:%M:%fZ', updatedAt) ASC
	private static final String orderClauseForInternalGetRecordsSinceDate = 
			DatabaseUtil.getStrfStringForString(DatabaseUtil.UPDATED_AT_COLUMN_NAME) + " ASC";
	protected List<Map<String, Object>> internalGetRecordsSinceDate(String tableName,
			Map<String, Class> columnTypes, Date sinceDate) {
		String whereClause = null;
		String[] whereArgs = null;

		if (sinceDate != null) {
			whereClause = datedWhereClauseForInternalGetRecordsSinceDate;
			whereArgs = new String[] {DatabaseUtil.parseDateFormat.format(sinceDate)};
		} else {
			whereClause = whereClauseForInternalGetRecordsSinceDate;
		}
		
		Log.v(LOG_TAG, "Getting records for upload from " + tableName + " with where clause " +
				whereClause);
		
		SQLiteDatabase db = DatabaseUtil.instance().getReadableDatabase();
		Cursor cursor = db.query(tableName, null, whereClause,
				whereArgs, null, null, 
				orderClauseForInternalGetRecordsSinceDate);
		List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "No records found since date " + sinceDate);
			return records;
		}

		while (!cursor.isAfterLast()) {
			records.add(DatabaseUtil.getRecordFromCursor(cursor, columnTypes));
			cursor.moveToNext();
		}

		return records;
	}

	/*
	 * Specify null for no id.
	 * 
	 * NOTE: Sets needsUpload to false
	 */
	protected ContentValues getCommonValuesIntoValuesForTable(ContentValues values,
			String tableName, String withNewId, boolean needsUpload) {

		if (withNewId != null) {
			// replace temp id with parse id
			values.put(
					DatabaseUtil.networkKeyForLocalKey(tableName, DatabaseUtil.OBJECT_ID_COLUMN_NAME)
					, withNewId);
		}
		values.put(
				DatabaseUtil.networkKeyForLocalKey(tableName, DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME),
				needsUpload);
		values.put(DatabaseUtil.networkKeyForLocalKey(tableName, DatabaseUtil.UPDATED_AT_COLUMN_NAME),
				DatabaseUtil.parseDateFormat.format(new Date(System.currentTimeMillis())));

		return values;
	}

	/**
	 * Pushes records from local db to Parse for a specific table
	 * @param List<ParseObject> objects
	 * @return Date the updatedAt date of the last object synced
	 */
	public abstract Date pushRecords(List<Map<String, Object>> objects);

	public Date doSyncSinceDate(Date sinceDate) {
		return pushRecords(getRecordsSinceDate(sinceDate));
	}
}
