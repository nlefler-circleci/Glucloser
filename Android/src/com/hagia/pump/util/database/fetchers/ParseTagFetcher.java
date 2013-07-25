package com.hagia.pump.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.hagia.pump.types.Tag;
import com.hagia.pump.util.database.Tables;
import com.parse.ParseObject;

public class ParseTagFetcher extends SyncFetcher {
	private static final String LOG_TAG = "Pump_Parse_Tag_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date sinceDate) {
		Log.i(LOG_TAG, "Starting fetch from " + Tables.TAG_DB_NAME + " from Parse");

		List<ParseObject> parseObjects = fetchParseObjectsForTableSinceDate(
				Tables.TAG_DB_NAME, sinceDate, LOG_TAG);
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

		Map<String, Object> record;
		for (ParseObject object : parseObjects) {
			record = new HashMap<String, Object>();
			
			getCommonValuesFromParseObjectIntoMap(object, record);

			record.put(Tag.NAME_DB_COLUMN_KEY, object.getString(Tag.NAME_DB_COLUMN_KEY));

			Log.v(LOG_TAG, "Got record " + record.toString());
			results.add(record);
		}

		return results;
	}

}
