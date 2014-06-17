package com.nlefler.glucloser.util;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.MealToFood;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;

public class MealToFoodUtil {
	private static final String LOG_TAG = "Glucloser_Meal_To_Food_Util";

	public static boolean saveMealToFood(MealToFood mealToFood) {
		ContentValues values = new ContentValues();

		if (mealToFood.createdAt == null) {
			mealToFood.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (mealToFood.updatedAt == null) {
			mealToFood.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		return mealToFood.save();
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
