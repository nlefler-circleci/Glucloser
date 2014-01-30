package com.nlefler.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.nlefler.glucloser.types.Food;
import com.nlefler.glucloser.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

public class ParseFoodFetcher extends SyncFetcher {
	private static final String LOG_TAG = "Pump_Parse_Food_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date lastSyncTime) {
		Log.i(LOG_TAG, "Starting fetch for table " + Tables.FOOD_DB_NAME + " from Parse");

		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		List<ParseObject> parseObjects = 
				fetchParseObjectsForTableSinceDate(Tables.FOOD_DB_NAME, lastSyncTime, LOG_TAG);
			
			Map<String, Object> record;
			for (ParseObject object : parseObjects) {
				record = new HashMap<String, Object>();

				getCommonValuesFromParseObjectIntoMap(object, record);
				
				record.put(Food.NAME_DB_COLUMN_KEY, object.getString(Food.NAME_DB_COLUMN_KEY));
				record.put(Food.CARBS_DB_COLUMN_KEY, object.getNumber(Food.CARBS_DB_COLUMN_KEY));
				record.put(Food.CORRECTION_DB_COLUMN_KEY, object.getBoolean(Food.CORRECTION_DB_COLUMN_KEY));
				record.put(Food.DATE_EATEN_DB_COLUMN_NAME, object.getDate(Food.DATE_EATEN_DB_COLUMN_NAME));

				if (object.containsKey(Food.IMAGE_DB_COLUMN_KEY)) {
					ParseFile photoFile = (ParseFile) object.get(Food.IMAGE_DB_COLUMN_KEY);
					try {
						record.put(Food.IMAGE_DB_COLUMN_KEY, photoFile.getData());
					} catch (ParseException e) {
						Log.e(LOG_TAG, "Unable to get photo from food");
						Log.e(LOG_TAG, e.getMessage());
						e.printStackTrace();
					}
				}
				
				Log.v(LOG_TAG, "Got record " + record.toString());
				results.add(record);
			}

		return results;
	}
}
