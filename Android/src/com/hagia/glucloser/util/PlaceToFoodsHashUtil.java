package com.hagia.glucloser.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hagia.glucloser.types.PlaceToFoodsHash;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.hagia.glucloser.util.database.Tables;

public class PlaceToFoodsHashUtil {
	private static final String LOG_TAG = "Pump_Place_To_Foods_Hash_Util";
	
	// SELECT * FROM PlaceToFoodsHash WHERE foodsHash = ?
	private static String whereClauseForGetPlaceToFoodsHashes =
			PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY + "=?";
	public static List<PlaceToFoodsHash> getPlaceToFoodsHashes(String foodsHash) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_TO_FOODS_HASH_DB_NAME,
				null, whereClauseForGetPlaceToFoodsHashes, new String[] {foodsHash}, 
				null, null, null);
		List<PlaceToFoodsHash> results = new ArrayList<PlaceToFoodsHash>();
		
		if (!cursor.moveToFirst()) {
			return results;
		}
		
		while (!cursor.isAfterLast()) {
			results.add(PlaceToFoodsHash.fromMap(
					DatabaseUtil.getRecordFromCursor(cursor, PlaceToFoodsHash.COLUMN_TYPES)));
			cursor.moveToNext();
		}
		
		return results;
	}
	
	public static long savePlaceToFoodsHash(PlaceToFoodsHash ptfh) {
		ContentValues values = new ContentValues();

		if (ptfh.createdAt == null) {
			ptfh.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (ptfh.updatedAt == null) {
			ptfh.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_FOODS_HASH_DB_NAME,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME), ptfh.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_FOODS_HASH_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(ptfh.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_FOODS_HASH_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(ptfh.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_FOODS_HASH_DB_NAME,
				PlaceToFoodsHash.PLACE_DB_COLUMN_KEY), ptfh.place.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_FOODS_HASH_DB_NAME,
				PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY), ptfh.foodsHash);

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_FOODS_HASH_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				ptfh.needsUpload);
		
		Log.v(LOG_TAG, "Saving place to foods hash with values " + values);
		
		DatabaseUtil.instance().getWritableDatabase().beginTransaction();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.PLACE_TO_FOODS_HASH_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (code == -1) {
			Log.i(LOG_TAG, "Error code recieved from insert: " + code);
			Log.i(LOG_TAG, "Values are: " + values);
			DatabaseUtil.instance().getWritableDatabase().endTransaction();
		} else {
			DatabaseUtil.instance().getWritableDatabase().setTransactionSuccessful();
			DatabaseUtil.instance().getWritableDatabase().endTransaction();
		}

		return code;
	}
}
