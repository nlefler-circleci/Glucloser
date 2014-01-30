package com.nlefler.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.nlefler.glucloser.types.Meal;
import com.nlefler.glucloser.util.database.Tables;
import com.parse.ParseObject;

public class ParseMealFetcher extends SyncFetcher {
	private final static String LOG_TAG = "Pump_Parse_Meal_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date sinceDate) {
		Log.i(LOG_TAG, "Starting fetch of " + Tables.MEAL_DB_NAME + " from Parse");

		List<ParseObject> parseObjects = fetchParseObjectsForTableSinceDate(
				Tables.MEAL_DB_NAME, sinceDate, LOG_TAG);
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

		for (ParseObject object : parseObjects) {
			Map<String, Object> record = new HashMap<String, Object>();

			getCommonValuesFromParseObjectIntoMap(object, record);

			record.put(Meal.DATE_EATEN_DB_COLUMN_NAME, object.getDate(Meal.DATE_EATEN_DB_COLUMN_NAME));

			Log.v(LOG_TAG, "Got record " + record.toString());
			results.add(record);
		}

		return results;
	}

}
