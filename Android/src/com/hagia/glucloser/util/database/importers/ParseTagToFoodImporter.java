package com.hagia.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.hagia.glucloser.types.TagToFood;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.hagia.glucloser.util.database.Tables;

public class ParseTagToFoodImporter extends SyncImporter {
	private static final String LOG_TAG = "Pump_Parse_Tag_To_Food_Importer";
	
	private static final String DATABASE_NAME = Tables.TAG_TO_FOOD_DB_NAME;
	
	/**
	 * Parse Schema v1
	 * Columns:
	 * 	objectId => String
	 * 	tag => Pointer (Tag)
	 *  food => Pointer (Food)
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
		
		DatabaseUtil dbUtil = DatabaseUtil.instance();
		Date lastUpdate = null;

		ContentValues values = new ContentValues();
		for (Map<String, Object> record : objects) {
			values.clear();

			getCommonValuesForTableIntoValuesFromMap(DATABASE_NAME, values, record);

			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
					TagToFood.TAG_DB_COLUMN_KEY), (String)record.get(TagToFood.TAG_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, 
					TagToFood.FOOD_DB_COLUMN_KEY), (String)record.get(TagToFood.FOOD_DB_COLUMN_KEY));
			

			lastUpdate = upsertRecord(values, whereClause, record, DATABASE_NAME,
					lastUpdate, LOG_TAG);
		}

		Log.i(LOG_TAG, "Finished import of records, date of last record is " + (lastUpdate == null ? "null" : lastUpdate.toGMTString()));
		return lastUpdate;
	}
}
