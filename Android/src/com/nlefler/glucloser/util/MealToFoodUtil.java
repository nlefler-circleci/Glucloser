package com.nlefler.glucloser.util;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.types.Food;
import com.nlefler.glucloser.types.Meal;
import com.nlefler.glucloser.types.MealToFood;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;

public class MealToFoodUtil {
	private static final String LOG_TAG = "Pump_Meal_To_Food_Util";

	public static long saveMealToFood(MealToFood mealToFood) {
		ContentValues values = new ContentValues();

		if (mealToFood.createdAt == null) {
			mealToFood.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (mealToFood.updatedAt == null) {
			mealToFood.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOOD_DB_NAME,
				DatabaseUtil.PARSE_ID_COLUMN_NAME), mealToFood.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOOD_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(mealToFood.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOOD_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(mealToFood.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOOD_DB_NAME,
				MealToFood.MEAL_DB_COLUMN_KEY), mealToFood.meal.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOOD_DB_NAME,
				MealToFood.FOOD_DB_COLUMN_KEY), mealToFood.food.id);

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOOD_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				mealToFood.needsUpload);

		DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.MEAL_TO_FOOD_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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

	private static final String whereClauseForGetMealToFoodForFood =
			MealToFood.FOOD_DB_COLUMN_KEY + "=?";
	public static MealToFood getMealToFoodForFoodSync(Food food) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query
				(Tables.MEAL_TO_FOOD_DB_NAME, null, whereClauseForGetMealToFoodForFood,
						new String[] {food.id}, null, null, null);
		MealToFood mealToFood = null;

		if (cursor.moveToFirst()) {
			Map<String, Object> record = null;
			while (!cursor.isAfterLast()) {
				record = DatabaseUtil.getRecordFromCursor(cursor, MealToFood.COLUMN_TYPES);
				cursor.moveToNext();
			}
			if (record != null) {
				mealToFood = MealToFood.fromMap(record);
			}
		}

		return mealToFood;
	}

	private static final String whereClauseForGetMealToFoodForMeal =
			MealToFood.MEAL_DB_COLUMN_KEY + "=?";
	public static MealToFood getMealToFoodForMeal(Meal meal) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query
				(Tables.MEAL_TO_FOOD_DB_NAME, null, whereClauseForGetMealToFoodForMeal,
						new String[] {meal.id}, null, null, null);
		MealToFood mealToFood = null;

		if (cursor.moveToFirst()) {
			Map<String, Object> record = null;
			while (!cursor.isAfterLast()) {
				record = DatabaseUtil.getRecordFromCursor(cursor, MealToFood.COLUMN_TYPES);
				cursor.moveToNext();
			}
			if (record != null) {
				mealToFood = MealToFood.fromMap(record);
			}
		}

		return mealToFood;
	}
}
