package com.nlefler.glucloser.util;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.PlaceToMeal;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.nlefler.glucloser.model.meal.Meal;

public class PlaceToMealUtil {
	private static final String LOG_TAG = "Pump_Place_To_Meal_Util";

	public static long savePlaceToMeal(PlaceToMeal placeToMeal) {
		ContentValues values = new ContentValues();

		if (placeToMeal.createdAt == null) {
			placeToMeal.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (placeToMeal.updatedAt == null) {
			placeToMeal.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_MEAL_DB_NAME,
                DatabaseUtil.PARSE_ID_COLUMN_NAME), placeToMeal.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_MEAL_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(placeToMeal.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_MEAL_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(placeToMeal.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_MEAL_DB_NAME,
				PlaceToMeal.PLACE_DB_COLUMN_KEY), placeToMeal.place.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_MEAL_DB_NAME,
				PlaceToMeal.MEAL_DB_COLUMN_KEY), placeToMeal.meal.id);

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_TO_MEAL_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				placeToMeal.needsUpload);

		Log.v(LOG_TAG, "Saving place to meal with values " + values);

		DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.PLACE_TO_MEAL_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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

	private static final String whereClauseForGetPlaceToMealForPlace =
			PlaceToMeal.PLACE_DB_COLUMN_KEY + "=?";
	public static PlaceToMeal getPlaceToMealForPlace(Place place) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query
				(Tables.PLACE_TO_MEAL_DB_NAME, null, whereClauseForGetPlaceToMealForPlace,
						new String[] {place.id}, null, null, null);
		PlaceToMeal placeToMeal = null;

		if (cursor.moveToFirst()) {
			Map<String, Object> record = null;
			while (!cursor.isAfterLast()) {
				record = DatabaseUtil.getRecordFromCursor(cursor, PlaceToMeal.COLUMN_TYPES);
				cursor.moveToNext();
			}
			if (record != null) {
				placeToMeal = PlaceToMeal.fromMap(record);
			}
		}

		return placeToMeal;
	}

	private static final String whereClauseForGetPlaceToMealForMeal =
			PlaceToMeal.MEAL_DB_COLUMN_KEY + "=?";
	public static PlaceToMeal getPlaceToMealForMeal(Meal meal) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query
				(Tables.PLACE_TO_MEAL_DB_NAME, null, whereClauseForGetPlaceToMealForMeal,
						new String[] {meal.id}, null, null, null);
		PlaceToMeal placeToMeal = null;

		if (cursor.moveToFirst()) {
			Map<String, Object> record = null;
			while (!cursor.isAfterLast()) {
				record = DatabaseUtil.getRecordFromCursor(cursor, PlaceToMeal.COLUMN_TYPES);
				cursor.moveToNext();
			}
			if (record != null) {
				placeToMeal = PlaceToMeal.fromMap(record);
			}
		}

		return placeToMeal;
	}
}
