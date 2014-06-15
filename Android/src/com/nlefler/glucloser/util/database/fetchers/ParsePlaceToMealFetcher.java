package com.nlefler.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.nlefler.glucloser.model.placetomeal.PlaceToMeal;
import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.parse.ParseObject;

public class ParsePlaceToMealFetcher extends SyncFetcher {
	private static final String LOG_TAG = "Pump_Parse_Place_To_Meal_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date lastSyncTime) {
		Log.i(LOG_TAG, "Starting fetch for table " + Tables.PLACE_TO_MEAL_DB_NAME + " from Parse");

		List<ParseObject> parseObjects = fetchParseObjectsForTableSinceDate(
				Tables.PLACE_TO_MEAL_DB_NAME, lastSyncTime, LOG_TAG);
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();


		Map<String, Object> record;
		for (ParseObject object : parseObjects) {
			record = new HashMap<String, Object>();

			getCommonValuesFromParseObjectIntoMap(object, record);

			record.put(PlaceToMeal.PLACE_DB_COLUMN_KEY,
					object.getParseObject(PlaceToMeal.PLACE_DB_COLUMN_KEY).getObjectId());
			record.put(PlaceToMeal.MEAL_DB_COLUMN_KEY, 
					object.getParseObject(PlaceToMeal.MEAL_DB_COLUMN_KEY).getObjectId());


			Log.v(LOG_TAG, "Got record " + record.toString());
			results.add(record);
		}

		return results;
	}

}
