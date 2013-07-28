package com.hagia.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.hagia.glucloser.types.TagToPlace;
import com.hagia.glucloser.util.database.Tables;
import com.parse.ParseObject;

public class ParseTagToPlaceFetcher extends SyncFetcher {
	private static final String LOG_TAG = "Pump_Parse_Tag_To_Place_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date lastSyncTime) {
		Log.i(LOG_TAG, "Starting fetch for table " + Tables.TAG_TO_PLACE_DB_NAME + " from Parse");


		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		List<ParseObject> parseObjects =
				fetchParseObjectsForTableSinceDate(Tables.TAG_TO_PLACE_DB_NAME,
						lastSyncTime, LOG_TAG);

		Map<String, Object> record;
		for (ParseObject object : parseObjects) {
			record = new HashMap<String, Object>();

			getCommonValuesFromParseObjectIntoMap(object, record);

			record.put(TagToPlace.TAG_DB_COLUMN_KEY,
					object.getParseObject(TagToPlace.TAG_DB_COLUMN_KEY).getObjectId());
			record.put(TagToPlace.PLACE_DB_COLUMN_KEY, 
					object.getParseObject(TagToPlace.PLACE_DB_COLUMN_KEY).getObjectId());


			Log.v(LOG_TAG, "Got record " + record.toString());
			results.add(record);
		}


		return results;
	}
}
