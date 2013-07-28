package com.hagia.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.hagia.glucloser.util.database.Tables;
import com.hagia.glucloser.types.Meal;
import com.hagia.glucloser.util.database.DatabaseUtil;

public class ParseMealImporter extends SyncImporter {
	private static final String LOG_TAG = "Pump_Parse_Meal_Importer";

	private static final String DATABASE_NAME = Tables.MEAL_DB_NAME;
	
	/**
	 * Parse Schema v1
	 * Columns:
	 * 	objectId => String
	 * 	place => Pointer (String)
	 * 	foods => Relation (List<String>)
	 * 	live => Boolean
	 *  dateEaten => Date
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
		for (Map<String, Object> meal : objects) {
			values.clear();

			getCommonValuesForTableIntoValuesFromMap(DATABASE_NAME, values, meal);

			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, Meal.DATE_EATEN_DB_COLUMN_NAME), 
					DatabaseUtil.parseDateFormat.format(((Date)meal.get(Meal.DATE_EATEN_DB_COLUMN_NAME))));

			
			lastUpdate = upsertRecord(values, whereClause, meal, DATABASE_NAME,
					lastUpdate, LOG_TAG);
		}

		Log.i(LOG_TAG, "Finished import of records, date of last record is " + (lastUpdate == null ? "null" : lastUpdate.toGMTString()));
		return lastUpdate;
	}

}
