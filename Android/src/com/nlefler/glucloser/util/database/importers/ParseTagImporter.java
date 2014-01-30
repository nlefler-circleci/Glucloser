package com.nlefler.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.nlefler.glucloser.types.Tag;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.Tables;

public class ParseTagImporter extends SyncImporter {
	private static final String LOG_TAG = "Pump_Parse_Tag_Importer";

	private static final String DATABASE_NAME = Tables.TAG_DB_NAME;
	
	/**
	 * Parse Schema v1
	 * Columns:
	 * 	objectId => String
	 * 	name => String
	 * 	foods => Relation (List<String>)
	 * 	places => Relation (List<String>)
	 * 	live => Boolean
	 * 	createdAt => Date
	 * 	updatedAt => Date
	 *  dataVersion => Number
	 * 	ACL => Access Control List (Ignored)
	 */
	private static final String whereClause = 
			getUpsertWhereClauseForTable(DATABASE_NAME);
	@Override
	public Date importRecords(List<Map<String, Object>> objects) {
		if (objects.isEmpty()) {
			return null;
		}

		Date lastUpdate = null;

		ContentValues values = new ContentValues();
		for (Map<String, Object> tag : objects) {
			values.clear();

			getCommonValuesForTableIntoValuesFromMap(DATABASE_NAME, values, tag);

			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
					Tag.NAME_DB_COLUMN_KEY), (String)tag.get(Tag.NAME_DB_COLUMN_KEY));
			
			lastUpdate = upsertRecord(values, whereClause, tag, DATABASE_NAME,
					lastUpdate, LOG_TAG);
		}
		
		Log.i(LOG_TAG, "Finished import of records, date of last record is " + (lastUpdate == null ? "null" : lastUpdate.toGMTString()));
		return lastUpdate;
	}

}
