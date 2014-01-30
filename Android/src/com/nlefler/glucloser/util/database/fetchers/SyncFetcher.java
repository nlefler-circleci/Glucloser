package com.nlefler.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQuery.CachePolicy;

public abstract class SyncFetcher {
	private static final String LOG_TAG = "Pump_Sync_Fetcher";
	// 1K is the highest limit supported by Parse but this uses a ton of memory
	// within the Parse SDK
	private static final int PARSE_FETCH_LIMIT = 1000;
	
	private boolean hasMoreRecords = true;
	
	public boolean hasMoreRecords() {
		return hasMoreRecords;
	}
	
	public abstract List<Map<String, Object>> fetchRecords(Date sinceDate);
	
	protected List<ParseObject> fetchParseObjectsForTableSinceDate(String tableName,
			Date sinceDate, String logTag) {
		
		ParseQuery syncQuery = new ParseQuery(tableName);
		syncQuery.setCachePolicy(CachePolicy.NETWORK_ONLY);
		
		if (sinceDate != null) {
			syncQuery.whereGreaterThan(DatabaseUtil.UPDATED_AT_COLUMN_NAME, sinceDate);
		}
		syncQuery.orderByAscending(DatabaseUtil.UPDATED_AT_COLUMN_NAME);
		syncQuery.setLimit(PARSE_FETCH_LIMIT);

		List<ParseObject> results = new ArrayList<ParseObject>();
		try {
			results = syncQuery.find();
			Log.i(LOG_TAG, "Got " + results.size() + " records from Parse");

			hasMoreRecords = !results.isEmpty();
		} catch (ParseException e) {
			Log.e(logTag, "Caught error while fetching records from Parse: " + e.getLocalizedMessage());
			e.printStackTrace();
			hasMoreRecords = false;
		}
		
		return results;
	}
	
	protected Map<String, Object> getCommonValuesFromParseObjectIntoMap(ParseObject parseObject,
			Map<String, Object> map) {
		map.put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, parseObject.getObjectId());
		map.put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, parseObject.getUpdatedAt());
		map.put(DatabaseUtil.CREATED_AT_COLUMN_NAME, parseObject.getCreatedAt());
		map.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, 
				parseObject.getInt(DatabaseUtil.DATA_VERSION_COLUMN_NAME));
		
		return map;
	}
}
