package com.nlefler.glucloser.util.database.pushers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.types.Place;
import com.nlefler.glucloser.types.PlaceToFoodsHash;
import com.nlefler.glucloser.types.PlaceToMeal;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;

public class ParsePlacePusher extends SyncPusher {
	private static final String LOG_TAG = "Pump_Parse_Place_Pusher";

	@Override
	public Date pushRecords(List<Map<String, Object>> objects) {
		Place place;
		ParseObject parseObject;
		Date lastSyncDate = null;
		SQLiteDatabase db = DatabaseUtil.instance().getWritableDatabase();
		ContentValues values = new ContentValues();
		long code = 0;

		for (Map<String, Object> record : objects) {
			place = Place.fromMap(record);
			parseObject = place.toParseObject();
			try {
				// Save to Parse
				parseObject.save();
				String objId = place.id.equals(parseObject.getObjectId()) ? null : parseObject.getObjectId();

				values.clear();
				getCommonValuesIntoValuesForTable(values, Tables.PLACE_DB_NAME, objId, false);
				
				DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
				code = db.update(Tables.PLACE_DB_NAME, values,
						DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?", new String[] {place.id});
				if (code == -1) {
					Log.e(LOG_TAG, "Unable to update PlaceToMeal entry with new object id");
					// TODO clean up
					DatabaseUtil.instance().getWritableDatabase().endTransaction();
					continue;
				}
				
				// Update PlaceToMeal
				values.clear();
				if (objId != null) {
					values.put(PlaceToMeal.PLACE_DB_COLUMN_KEY, objId);
					values.put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, true);

					values.put(DatabaseUtil.UPDATED_AT_COLUMN_NAME,
							DatabaseUtil.parseDateFormat.format(new Date(System.currentTimeMillis())));
					code = db.update(Tables.PLACE_TO_MEAL_DB_NAME, values,
							PlaceToMeal.PLACE_DB_COLUMN_KEY + "=?", new String[] {place.id});
					if (code == -1) {
						Log.e(LOG_TAG, "Unable to update PlaceToMeal entry with new object id");
						// TODO clean up
						DatabaseUtil.instance().getWritableDatabase().endTransaction();
						continue;
					}
				}
				
				// Update PlaceToFoodsHash
				values.clear();
				if (objId != null) {
					values.put(PlaceToFoodsHash.PLACE_DB_COLUMN_KEY, objId);
					values.put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, true);

					values.put(DatabaseUtil.UPDATED_AT_COLUMN_NAME,
							DatabaseUtil.parseDateFormat.format(new Date(System.currentTimeMillis())));
					code = db.update(Tables.PLACE_TO_FOODS_HASH_DB_NAME, values,
							PlaceToFoodsHash.PLACE_DB_COLUMN_KEY + "=?", new String[] {place.id});
					if (code == -1) {
						Log.e(LOG_TAG, "Unable to update PlaceToFoodsHash entry with new object id");
						// TODO clean up
						DatabaseUtil.instance().getWritableDatabase().endTransaction();
						continue;
					}
				}
				DatabaseUtil.instance().getWritableDatabase().setTransactionSuccessful();
				DatabaseUtil.instance().getWritableDatabase().endTransaction();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lastSyncDate = parseObject.getUpdatedAt();
		}
		return lastSyncDate;
	}

	@Override
	public List<Map<String, Object>> getRecordsSinceDate(Date sinceDate) {
		return super.internalGetRecordsSinceDate(Tables.PLACE_DB_NAME,
				Place.COLUMN_TYPES, sinceDate);
	}
}
