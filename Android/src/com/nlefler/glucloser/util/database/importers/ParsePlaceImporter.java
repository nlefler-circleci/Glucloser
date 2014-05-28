package com.nlefler.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.nlefler.glucloser.types.Place;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;

public class ParsePlaceImporter extends SyncImporter {
	private static final String LOG_TAG = "Pump_Parse_Place_Importer";
	
	private static final String DATABASE_NAME = Tables.PLACE_DB_NAME;
	
	/**
	 * Parse Schema v1
	 * Columns:
	 * 	objectId => String
	 * 	name => String
	 * 	tags => Relation (List<String>)
	 * 	live => Boolean
	 * 	location => ParseGeoPoint (Separated into latitude (Double) and longitude (Double))
	 * 	createdAt => Date
	 * 	updatedAt => Date
	 *  dataVersion => Number
	 * 	ACL => Access Control List (Ignored)
	 */
	private static final String whereClause = 
			getUpsertWhereClauseForTable(DATABASE_NAME);
	@Override
	public Date importRecords(List<Map<String, Object>> objects) {
		if (objects.isEmpty()) {
			return null;
		}

		Date lastUpdate = null;

		ContentValues values = new ContentValues();
		for (Map<String, Object> place : objects) {
			values.clear();

			getCommonValuesForTableIntoValuesFromMap(DATABASE_NAME, values, place);

			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
                    Place.NAME_DB_COLUMN_KEY), (String)place.get(Place.NAME_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
					Place.LATITUDE_DB_COLUMN_KEY), (Double)place.get(Place.LATITUDE_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
					Place.LONGITUDE_DB_COLUMN_KEY), (Double)place.get(Place.LONGITUDE_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, 
					Place.READABLE_ADDRESS_COLUMN_KEY), (String)place.get(Place.READABLE_ADDRESS_COLUMN_KEY));
			
			lastUpdate = upsertRecord(values, whereClause, place, DATABASE_NAME,
					lastUpdate, LOG_TAG);
		}

		Log.i(LOG_TAG, "Finished import of records, date of last record is " + (lastUpdate == null ? "null" : lastUpdate.toGMTString()));
		return lastUpdate;
	}

}
