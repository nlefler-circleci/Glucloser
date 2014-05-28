package com.nlefler.glucloser.util.database.pushers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.types.PlaceToMeal;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;

public class ParsePlaceToMealPusher extends SyncPusher {
	private static final String LOG_TAG = "Pump_Parse_Place_To_Meal_Pusher";

	@Override
	public Date pushRecords(List<Map<String, Object>> objects) {
		PlaceToMeal placeToMeal;
		ParseObject parseObject;
		Date lastSyncDate = null;
		SQLiteDatabase db = DatabaseUtil.instance().getWritableDatabase();
		ContentValues values = new ContentValues();
		long code = 0;

		for (Map<String, Object> record : objects) {
			placeToMeal = PlaceToMeal.fromMap(record);
			parseObject = placeToMeal.toParseObject();
			try {
				// Save to Parse
				parseObject.save();
				String objId = placeToMeal.id.equals(parseObject.getObjectId()) ? null : parseObject.getObjectId();
				
				values.clear();
				getCommonValuesIntoValuesForTable(values, Tables.PLACE_TO_MEAL_DB_NAME, objId, false);
				Log.v(LOG_TAG, "Uploading PlaceToMeal to Parse: " + values);
				
				DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
				code = db.update(Tables.PLACE_TO_MEAL_DB_NAME, values,
						DatabaseUtil.PARSE_ID_COLUMN_NAME + "=?", new String[] {placeToMeal.id});
				if (code == -1) {
					Log.e(LOG_TAG, "Unable to update PlaceToMeal entry with new object id");
					// TODO clean up
					DatabaseUtil.instance().getWritableDatabase().endTransaction();
					continue;
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
		return super.internalGetRecordsSinceDate(Tables.PLACE_TO_MEAL_DB_NAME,
				PlaceToMeal.COLUMN_TYPES, sinceDate);
	}
}
