package com.nlefler.glucloser.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.types.MealToFood;
import com.nlefler.glucloser.types.MealToFoodsHash;
import com.nlefler.glucloser.util.database.Tables;
import com.nlefler.glucloser.types.Meal;
import com.nlefler.glucloser.types.PlaceToMeal;
import com.nlefler.glucloser.util.database.DatabaseUtil;

public class MealUtil {
	private static final String LOG_TAG = "Pump_Meal_Util";

	// SELECT * FROM Food WHERE objectId IN (
	//	SELECT food FROM MealToFood WHERE meal = /meal.id/
	// )
	private static String whereClauseForGetFoodsForMeal = 
			MealToFood.MEAL_DB_COLUMN_KEY + " = ?";
	/**
	 * Get all foods for the given meal.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param meal
	 * @return A List<MealToFood> for the meal
	 */
	public static List<MealToFood> getFoodsForMeal(Meal meal) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.MEAL_TO_FOOD_DB_NAME, null,
				whereClauseForGetFoodsForMeal, 
				new String[] {meal.id}, null, null, null);
		List<MealToFood> foods = new ArrayList<MealToFood>();

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "No foods for meal with id " + meal.id + " in local db");
			return foods;
		}

		Map<String, Object> record = null;
		while (!cursor.isAfterLast()) {
			record = DatabaseUtil.getRecordFromCursor(cursor, MealToFood.COLUMN_TYPES);
			foods.add(MealToFood.fromMap(record));
			cursor.moveToNext();
		}

		return foods;
	}

	/**
	 * Returns recently eaten meals
	 * @param mealLimit Maximum number of meals to return
	 * @return A List<Meal> of recently eaten meals
	 */
	public static List<Meal> getRecentMeals(int mealLimit) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.MEAL_DB_NAME, null, null, null, null, null,
				Meal.DATE_EATEN_DB_COLUMN_NAME + " DESC", String.valueOf(mealLimit));
		List<Meal> meals = new ArrayList<Meal>();

		if (!cursor.moveToFirst()) {
			return meals;
		}

		while (!cursor.isAfterLast()) {
			meals.add(Meal.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Meal.COLUMN_TYPES)));
			cursor.moveToNext();
		}

		return meals;
	}

	private static String whereClauseForGetMealsFromDateToDate = 
			DatabaseUtil.getStrfStringForString(DatabaseUtil.CREATED_AT_COLUMN_NAME) + 
			" >= " + DatabaseUtil.getStrfStringForString("?") + " AND " +
			DatabaseUtil.getStrfStringForString(DatabaseUtil.CREATED_AT_COLUMN_NAME) + 
			" <= " + DatabaseUtil.getStrfStringForString("?");
	/**
	 * Get all meals eaten between the given dates, inclusive.
	 * 
	 * @note This method is synchronous. It should not be called on the
	 * main thread.
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return A List<Meal> of matching meals
	 */
	public static List<Meal> getMealsFromDateToDate(Date fromDate, Date toDate) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.MEAL_DB_NAME, null, whereClauseForGetMealsFromDateToDate,
				new String[] {DatabaseUtil.parseDateFormat.format(fromDate), 
						DatabaseUtil.parseDateFormat.format(toDate)}, null, null,
						DatabaseUtil.getStrfStringForString(DatabaseUtil.UPDATED_AT_COLUMN_NAME) +
				" DESC");
		List<Meal> meals = new ArrayList<Meal>();

		if (!cursor.moveToFirst()) {
			return meals;
		}

		while (!cursor.isAfterLast()) {
			meals.add(Meal.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Meal.COLUMN_TYPES)));
			cursor.moveToNext();
		}

		return meals;
	}

	/**
	 * Get a meal from the database with the given id.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @param id
	 * @return A @ref Meal representing the meal in the database, or
	 * null if no meal is found with the given id
	 */
	public static Meal getMealById(String id) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.MEAL_DB_NAME, null,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?", 
				new String[] {id}, null, null, null, "1");
		Meal meal = null;

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "No meals with id " + id + " in local db");
			return meal;
		}

		while (!cursor.isAfterLast()) {
			meal = Meal.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Meal.COLUMN_TYPES));
			cursor.moveToNext();
		}

		return meal;
	}

	// 	SELECT place FROM PlaceToMeal WHERE meal = /meal.id/
	private static String whereClauseForGetPlaceForMeal =
			PlaceToMeal.MEAL_DB_COLUMN_KEY + "=?";
	/**
	 * Get the place for the given meal.
	 * 
	 * @note This method is synchronous. It should not be called on the
	 * main thread.
	 * 
	 * @param meal
	 * @return A @ref PlaceToMeal
	 */
	public static PlaceToMeal getPlaceForMeal(Meal meal) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_TO_MEAL_DB_NAME, null,
				whereClauseForGetPlaceForMeal, 
				new String[] {meal.id}, null, null, null, "1");
		PlaceToMeal place = null;

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "No places for meal with id " + meal.id + " in local db");
			return place;
		}

		Map<String, Object> record = null;
		while (!cursor.isAfterLast()) {
			record = DatabaseUtil.getRecordFromCursor(cursor, PlaceToMeal.COLUMN_TYPES);
			cursor.moveToNext();
		}

		if (record != null) {
			place = PlaceToMeal.fromMap(record);
		}
		return place;
	}

	/**
	 * Generate the hash representing the foods in this meal.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param meal The meal whose foods should be hashed
	 * @return The hash, or null on failure
	 */
	public static String generateHashForFoods(Meal meal) {
		meal.linkFoods();
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			for (MealToFood mtf : meal.mealToFoods) {
				digest.update(mtf.food.name.trim().getBytes());
			}
			return new String(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the wrapper for a foods hash.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param meal
	 * @return The MealToFoodsHash for this meal and it's hash
	 */
	public static MealToFoodsHash getMealToFoodsHash(Meal meal) {
		MealToFoodsHash mtfh = new MealToFoodsHash();
		mtfh.meal = meal;
		mtfh.foodsHash = generateHashForFoods(meal);;

		return mtfh;
	}

	public static long saveMeal(Meal meal) {
		ContentValues values = new ContentValues();

		// Save meal
		if (meal.createdAt == null) {
			meal.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (meal.updatedAt == null) {
			meal.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.clear();
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_DB_NAME,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME), meal.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(meal.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(meal.updatedAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), meal.needsUpload);


		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_DB_NAME,
				Meal.DATE_EATEN_DB_COLUMN_NAME),DatabaseUtil.parseDateFormat.format(meal.dateEaten));

		DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.MEAL_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (code == -1) {
			Log.i(LOG_TAG, "Error code recieved from insert: " + code);
			Log.i(LOG_TAG, "Values are: " + values);
		} else {
			// Not responsible for saving MealToFoods or PlaceToMeals
			DatabaseUtil.instance().getWritableDatabase().setTransactionSuccessful();
		}
        DatabaseUtil.instance().getWritableDatabase().endTransaction();

		return code;		
	}
}
