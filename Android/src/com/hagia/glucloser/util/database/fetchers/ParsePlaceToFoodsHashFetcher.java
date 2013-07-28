package com.hagia.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.hagia.glucloser.types.PlaceToFoodsHash;
import com.hagia.glucloser.util.database.Tables;
import com.parse.ParseObject;

public class ParsePlaceToFoodsHashFetcher extends SyncFetcher {
	private static final String LOG_TAG = "Pump_Parse_Place_To_Foods_Hash_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date lastSyncTime) {
		Log.v(LOG_TAG, "Starting fetch for table " + Tables.PLACE_TO_FOODS_HASH_DB_NAME +
				" from Parse");

		List<ParseObject> parseObjects = fetchParseObjectsForTableSinceDate(
				Tables.PLACE_TO_FOODS_HASH_DB_NAME, lastSyncTime, LOG_TAG);
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();


		Map<String, Object> record;
		for (ParseObject object : parseObjects) {
			record = new HashMap<String, Object>();

			getCommonValuesFromParseObjectIntoMap(object, record);

			record.put(PlaceToFoodsHash.PLACE_DB_COLUMN_KEY,
					object.getParseObject(PlaceToFoodsHash.PLACE_DB_COLUMN_KEY).getObjectId());
			record.put(PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY, 
					object.getString(PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY));

			Log.v(LOG_TAG, "Got record " + record.toString());
			results.add(record);
		}

		return results;
	}
}
