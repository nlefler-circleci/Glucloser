package com.nlefler.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.nlefler.glucloser.util.database.Tables;
import com.nlefler.glucloser.types.PlaceToFoodsHash;
import com.nlefler.glucloser.util.database.DatabaseUtil;

public class ParsePlaceToFoodsHashImporter extends SyncImporter {
private static final String LOG_TAG = "Pump_Parse_Place_To_Foods_Hash_Importer";
	
	private static final String DATABASE_NAME = Tables.PLACE_TO_FOODS_HASH_DB_NAME;
	
	/**
	 * Parse Schema v1
	 * Columns:
	 * 	objectId => String
	 * 	place => Pointer (Place)
	 *  foodsHash => String
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
		for (Map<String, Object> record : objects) {
			values.clear();

			getCommonValuesForTableIntoValuesFromMap(DATABASE_NAME, values, record);

			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
					PlaceToFoodsHash.PLACE_DB_COLUMN_KEY), 
					(String)record.get(PlaceToFoodsHash.PLACE_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, 
					PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY), 
					(String)record.get(PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY));			
			
			lastUpdate = upsertRecord(values, whereClause, record, DATABASE_NAME,
					lastUpdate, LOG_TAG);
		}

		Log.i(LOG_TAG, "Finished import of records, date of last record is " + 
		(lastUpdate == null ? "null" : lastUpdate.toGMTString()));
		return lastUpdate;
	}
}
