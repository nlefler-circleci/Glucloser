package com.hagia.pump.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.hagia.pump.types.MealToFood;
import com.hagia.pump.util.database.Tables;
import com.parse.ParseObject;

public class ParseMealToFoodFetcher extends SyncFetcher {
	private static final String LOG_TAG = "Pump_Parse_Meal_To_Food_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date lastSyncTime) {
		Log.i(LOG_TAG, "Starting fetch for table " + Tables.MEAL_TO_FOOD_DB_NAME + " from Parse");

		List<ParseObject> parseObjects = fetchParseObjectsForTableSinceDate(
				Tables.MEAL_TO_FOOD_DB_NAME, lastSyncTime, LOG_TAG);
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

		Map<String, Object> record;
		for (ParseObject object : parseObjects) {
			record = new HashMap<String, Object>();

			getCommonValuesFromParseObjectIntoMap(object, record);

			record.put(MealToFood.MEAL_DB_COLUMN_KEY, 
					object.getParseObject(MealToFood.MEAL_DB_COLUMN_KEY).getObjectId());
			record.put(MealToFood.FOOD_DB_COLUMN_KEY, 
					object.getParseObject(MealToFood.FOOD_DB_COLUMN_KEY).getObjectId());

			Log.v(LOG_TAG, "Got record " + record.toString());
			results.add(record);
		}

		return results;
	}

}
