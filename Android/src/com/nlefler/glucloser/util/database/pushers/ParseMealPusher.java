package com.nlefler.glucloser.util.database.pushers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.types.Meal;
import com.nlefler.glucloser.types.MealToFood;
import com.nlefler.glucloser.types.MealToFoodsHash;
import com.nlefler.glucloser.types.PlaceToMeal;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;

public class ParseMealPusher extends SyncPusher {
	private static final String LOG_TAG = "Pump_Parse_Meal_Pusher";

	@Override
	public Date pushRecords(List<Map<String, Object>> objects) {
		Meal meal;
		ParseObject parseObject;
		Date lastSyncDate = null;
		SQLiteDatabase db = DatabaseUtil.instance().getWritableDatabase();
		ContentValues values = new ContentValues();
		long code = 0;

		for (Map<String, Object> record : objects) {
			meal = Meal.fromMap(record);
			parseObject = meal.toParseObject();
			try {
				// Save to Parse
				parseObject.save();
				String objId = meal.id.equals(parseObject.getObjectId()) ? null : parseObject.getObjectId();
				
				values.clear();
				getCommonValuesIntoValuesForTable(values, Tables.MEAL_DB_NAME, objId, false);
				
				DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
				code = db.update(Tables.MEAL_DB_NAME, values,
						DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?", new String[] {meal.id});
				if (code == -1) {
					Log.e(LOG_TAG, "Unable to update Meal entry with new object id");
					// TODO clean up
					DatabaseUtil.instance().getWritableDatabase().endTransaction();
					continue;
				}


				// Update MealToFood
				values.clear();
				if (objId != null) {
					// replace temp id with parse id
					values.put(MealToFood.MEAL_DB_COLUMN_KEY, objId);
					values.put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, true);
					values.put(DatabaseUtil.UPDATED_AT_COLUMN_NAME,
							DatabaseUtil.parseDateFormat.format(new Date(System.currentTimeMillis())));
					code = db.update(Tables.MEAL_TO_FOOD_DB_NAME, values,
							MealToFood.MEAL_DB_COLUMN_KEY + "=?", new String[] {meal.id});
					if (code == -1) {
						Log.e(LOG_TAG, "Unable to update MealToFood entry with new object id");
						// TODO clean up
						DatabaseUtil.instance().getWritableDatabase().endTransaction();
						continue;
					}
				}
				
				
				// Update PlaceToMeal
				values.clear();
				if (objId != null) {
					// replace temp id with parse id
					values.put(PlaceToMeal.MEAL_DB_COLUMN_KEY, objId);
					values.put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, true);

					values.put(DatabaseUtil.UPDATED_AT_COLUMN_NAME,
							DatabaseUtil.parseDateFormat.format(new Date(System.currentTimeMillis())));
					code = db.update(Tables.PLACE_TO_MEAL_DB_NAME, values,
							PlaceToMeal.MEAL_DB_COLUMN_KEY + "=?", new String[] {meal.id});
					if (code == -1) {
						Log.e(LOG_TAG, "Unable to update PlaceToMeal entry with new object id");
						// TODO clean up
						DatabaseUtil.instance().getWritableDatabase().endTransaction();
						continue;
					}
				}
				
				// Update MealToFoodsHash
				values.clear();
				if (objId != null) {
					// replace temp id with parse id
					values.put(MealToFoodsHash.MEAL_DB_COLUMN_KEY, objId);
					values.put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, true);

					values.put(DatabaseUtil.UPDATED_AT_COLUMN_NAME,
							DatabaseUtil.parseDateFormat.format(new Date(System.currentTimeMillis())));
					code = db.update(Tables.MEAL_TO_FOODS_HASH_DB_NAME, values,
							MealToFoodsHash.MEAL_DB_COLUMN_KEY + "=?", new String[] {meal.id});
					if (code == -1) {
						Log.e(LOG_TAG, "Unable to update MealToFoodsHash entry with new object id");
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
		return super.internalGetRecordsSinceDate(Tables.MEAL_DB_NAME, Meal.COLUMN_TYPES, sinceDate);
	}
}
