package com.hagia.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.hagia.glucloser.types.Barcode;
import com.hagia.glucloser.util.database.Tables;
import com.parse.ParseObject;

public class ParseBarcodeToFoodNameFetcher extends SyncFetcher {
	private static final String LOG_TAG = "Pump_Parse_Barcode_To_Food_Name_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date lastSyncTime) {
		Log.i(LOG_TAG, "Starting fetch for table " + Tables.BARCODE_TO_FOOD_NAME_DB_NAME + " from Parse");

		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		List<ParseObject> parseObjects = 
				fetchParseObjectsForTableSinceDate(Tables.BARCODE_TO_FOOD_NAME_DB_NAME, lastSyncTime, LOG_TAG);
			
			Map<String, Object> record;
			for (ParseObject object : parseObjects) {
				record = new HashMap<String, Object>();

				getCommonValuesFromParseObjectIntoMap(object, record);
				
				record.put(Barcode.BARCODE_DB_COLUMN_KEY, object.getString(Barcode.BARCODE_DB_COLUMN_KEY));
				record.put(Barcode.FOOD_NAME_DB_COLUMN_KEY, object.getNumber(Barcode.FOOD_NAME_DB_COLUMN_KEY));
				
				Log.v(LOG_TAG, "Got record " + record.toString());
				results.add(record);
			}

		return results;
	}

}
