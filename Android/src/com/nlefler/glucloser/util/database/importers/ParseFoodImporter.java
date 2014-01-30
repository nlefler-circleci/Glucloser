package com.nlefler.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.nlefler.glucloser.types.Food;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.Tables;

public class ParseFoodImporter extends SyncImporter {
	private static final String LOG_TAG = "Pump_Parse_Food_Importer";

	private static final String DATABASE_NAME = Tables.FOOD_DB_NAME;
	
	/**
	 * Parse Schema v1
	 * Columns:
	 * 	objectId => String
	 * 	place => Pointer (String)
	 * 	meal => Pointer (String)
	 * 	photo => File (byte[])
	 * 	name => String
	 * 	carbs => Number (Integer)
	 * 	tags => Relation (List<String>)
	 * 	live => Boolean
	 *  dateEaten => Date
	 * 	correction => Boolean
	 * 	createdAt => Date
	 * 	updatedAt => Date
	 *  dataVersion => Number
	 * 	ACL => Access Control List (Ignored)
	 */
	private static final String whereClause = getUpsertWhereClauseForTable(DATABASE_NAME);
	@Override
	public Date importRecords(List<Map<String, Object>> objects) {
		Log.i(LOG_TAG, "Starting import for " + DATABASE_NAME);
		
		if (objects.isEmpty()) {
			Log.i(LOG_TAG, "Asked to import 0 records");
			return null;
		}

		Date lastUpdate = null;

		Log.i(LOG_TAG, "Importing " + objects.size() + " records");
		ContentValues values = new ContentValues();
		
		for (Map<String, Object> food : objects) {
			values.clear();

			getCommonValuesForTableIntoValuesFromMap(DATABASE_NAME, values, food);

			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
                    Food.NAME_DB_COLUMN_KEY), (String)food.get(Food.NAME_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
					Food.CARBS_DB_COLUMN_KEY), (Integer)food.get(Food.CARBS_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
					Food.CORRECTION_DB_COLUMN_KEY), (Boolean)food.get(Food.CORRECTION_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
					Food.DATE_EATEN_DB_COLUMN_NAME), 
					DatabaseUtil.parseDateFormat.format(((Date)food.get(Food.DATE_EATEN_DB_COLUMN_NAME))));

			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, Food.IMAGE_DB_COLUMN_KEY),
					(byte[])food.get(Food.IMAGE_DB_COLUMN_KEY));

			
			lastUpdate = upsertRecord(values, whereClause, food, DATABASE_NAME,
					lastUpdate, LOG_TAG);
		}

		Log.i(LOG_TAG, "Finished import of records, date of last record is " + 
				(lastUpdate == null ? "null" : lastUpdate.toGMTString()));
		return lastUpdate;
	}

}
