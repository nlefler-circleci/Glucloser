package com.nlefler.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.nlefler.glucloser.model.placetomeal.PlaceToMeal;
import com.nlefler.glucloser.util.database.DatabaseUtil;

public class ParsePlaceToMealImporter extends SyncImporter {
	private static final String LOG_TAG = "Pump_Parse_Place_To_Meal_Importer";
	
	private static final String DATABASE_NAME = Tables.PLACE_TO_MEAL_DB_NAME;
	
	/**
	 * Parse Schema v1
	 * Columns:
	 * 	objectId => String
	 * 	place => Pointer (Place)
	 *  meal => Pointer (Meal)
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
					PlaceToMeal.PLACE_DB_COLUMN_KEY), (String)record.get(PlaceToMeal.PLACE_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, 
					PlaceToMeal.MEAL_DB_COLUMN_KEY), (String)record.get(PlaceToMeal.MEAL_DB_COLUMN_KEY));			
			
			lastUpdate = upsertRecord(values, whereClause, record, DATABASE_NAME,
					lastUpdate, LOG_TAG);
		}

		Log.i(LOG_TAG, "Finished import of records, date of last record is " + (lastUpdate == null ? "null" : lastUpdate.toGMTString()));
		return lastUpdate;
	}
}
