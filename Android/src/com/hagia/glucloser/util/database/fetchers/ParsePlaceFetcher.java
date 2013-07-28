package com.hagia.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.hagia.glucloser.types.Place;
import com.hagia.glucloser.util.database.Tables;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

public class ParsePlaceFetcher extends SyncFetcher {
	private static final String LOG_TAG = "Pump_Parse_Place_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date sinceDate) {
		Log.i(LOG_TAG, "Starting fetch for " + Tables.PLACE_DB_NAME + " from Parse");

		List<ParseObject> parseObjects = fetchParseObjectsForTableSinceDate(
				Tables.PLACE_DB_NAME, sinceDate, LOG_TAG);
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

		for (ParseObject object : parseObjects) {
			Map<String, Object> record = new HashMap<String, Object>();

			getCommonValuesFromParseObjectIntoMap(object, record);

			record.put(Place.NAME_DB_COLUMN_KEY, object.getString(Place.NAME_DB_COLUMN_KEY));
			record.put(Place.READABLE_ADDRESS_COLUMN_KEY, object.getString(Place.READABLE_ADDRESS_COLUMN_KEY));

			ParseGeoPoint location = (ParseGeoPoint) object.get(Place.LOCATION_DB_COLUMN_KEY);
			record.put(Place.LATITUDE_DB_COLUMN_KEY, location.getLatitude());
			record.put(Place.LONGITUDE_DB_COLUMN_KEY, location.getLongitude());

			Log.v(LOG_TAG, "Got record " + record.toString());
			results.add(record);
		}

		return results;
	}

}
