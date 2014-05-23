package com.nlefler.glucloser.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.GlucloserActivity;
import com.nlefler.glucloser.types.Food;
import com.nlefler.glucloser.types.Meal;
import com.nlefler.glucloser.types.MealToFood;
import com.nlefler.glucloser.types.MealToFoodsHash;
import com.nlefler.glucloser.types.Place;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.Tables;


public class FoodUtil {
	private static final String LOG_TAG = "Pump_Food_Util";

	private static String[] columnsForGetAllFoodNames = new String[] {Food.NAME_DB_COLUMN_KEY};

	/**
	 * Returns names for all foods in the database.
	 * 
	 * @note This method is synchronous. It should not
	 * be called on the main thread.
	 * 
	 * @return A List<String> of food names
	 */
	public static List<String> getAllFoodNames() {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				true, Tables.FOOD_DB_NAME, columnsForGetAllFoodNames,
				null, null, Food.NAME_DB_COLUMN_KEY, 
				null, Food.NAME_DB_COLUMN_KEY + " ASC", null);

		List<String> sortedNames = new ArrayList<String>();

		if (!cursor.moveToFirst()) {
			return sortedNames;
		}

		Map<String, Object> map = null;
		while (!cursor.isAfterLast()) {
			map = DatabaseUtil.getRecordFromCursor(cursor, Food.COLUMN_TYPES);
			sortedNames.add((String)map.get(Food.NAME_DB_COLUMN_KEY));

			cursor.moveToNext();
		}

		return sortedNames;
	}

	private static final String[] columnsForGetAllFoodNamesStartingWith = 
			new String[] {Food.NAME_DB_COLUMN_KEY};
	private static final String whereClauseForGetAllFoodNamesStartingWith =
			"lower(" + Food.NAME_DB_COLUMN_KEY + ") LIKE lower(?)";
	/**
	 * Returns names for all foods in the database that begin
	 * with the provided string.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param start The string to match at the beginning of the name
	 * @return A List<String> of food names.
	 */
	public static List<String> getAllFoodNamesStartingWith(String start) {
		Log.i(LOG_TAG, "Getting food names starting with " + start);


		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.FOOD_DB_NAME,
				columnsForGetAllFoodNamesStartingWith, 
				whereClauseForGetAllFoodNamesStartingWith,
				new String[] {start + "%"}, Food.NAME_DB_COLUMN_KEY, null, 
				Food.NAME_DB_COLUMN_KEY + " ASC");
		List<String> sortedNames = new ArrayList<String>();

		if (!cursor.moveToFirst()) {
			return sortedNames;
		}

		Map<String, Object> map;
		while (!cursor.isAfterLast()) {
			map = DatabaseUtil.getRecordFromCursor(cursor, Food.COLUMN_TYPES);
			cursor.moveToNext();

			sortedNames.add((String)map.get(Food.NAME_DB_COLUMN_KEY));
		}
		return sortedNames;

	}

	private static final String whereClauseForGetFoodForName = 
			"lower(" + Food.NAME_DB_COLUMN_KEY + ") LIKE lower(?)";

	/**
	 * Returns all food records whose name is equal to the provided
	 * name.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param foodName The name to match
	 * @return A List<Food> of matching records
	 */
	public static List<Food> getAllFoodsForName(String foodName) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.FOOD_DB_NAME,
				null, whereClauseForGetFoodForName,
				new String[] {"%" + foodName + "%"}, Food.NAME_DB_COLUMN_KEY,
				null, Food.NAME_DB_COLUMN_KEY + " ASC");
		List<Food> foods = new ArrayList<Food>();

		if (!cursor.moveToFirst()) {
			return foods;
		}

		while (!cursor.isAfterLast()) {
			foods.add(Food.fromMap(DatabaseUtil.getRecordFromCursor(cursor, 
					Food.COLUMN_TYPES)));
			cursor.moveToNext();
		}
		return foods;
	}

	/**
	 * Returns the food record for the provided id.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @param id The id to match
	 * @return A @ref Food for the id or null if no matching Food is found
	 */
	public static Food getFoodById(String id) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.FOOD_DB_NAME, null,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?", 
				new String[] {id}, null, null, null, "1");
		Food food = null;

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "No foods with id " + id + " in local db");
			return food;
		}

		while (!cursor.isAfterLast()) {
			food = Food.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Food.COLUMN_TYPES));
			cursor.moveToNext();
		}

		return food;
	}

	/**
	 * Returns the @ref Place for the provided @ref Food.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @param food The food to get a @ref Place for
	 * @return The @ref Place or null if no @ref Food or @ref Place is found
	 */
	public static Place getPlaceForFood(Food food) {
		return MealUtil.getPlaceForMeal(FoodUtil.getMealForFood(food)).place;
	}

	private static final String whereClauseForGetAllFoodsWithNameContaining = 
			"lower(" + Food.NAME_DB_COLUMN_KEY + ") LIKE lower(?)";
	/**
	 * Returns all foods in the database whose name contains the provided
	 * string.
	 * 
	 * @note This method is synchronous. It should not be called on the
	 * main thread.
	 * 
	 * @param name The name search for.
	 * @return A List<Food> with matching foods
	 */
	public static List<Food> getAllFoodsWithNameContaining(String name) {
		Log.i(LOG_TAG, "Getting food with names containing " + name);

		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.FOOD_DB_NAME,
				null, 
				whereClauseForGetAllFoodsWithNameContaining,
				new String[] {"%" + name + "%"}, Food.NAME_DB_COLUMN_KEY, null,
				Food.NAME_DB_COLUMN_KEY + " ASC");
		List<Food> foods = new ArrayList<Food>();

		if (!cursor.moveToFirst()) {
			return foods;
		}

		while (!cursor.isAfterLast()) {
			foods.add(Food.fromMap(DatabaseUtil.getRecordFromCursor(cursor,
					Food.COLUMN_TYPES)));
			cursor.moveToNext();
		}
		return foods;
	}

	// 	SELECT place FROM MealToFood WHERE food = /food.id/
	private static String[] columnsForGetMealForFood = 
			new String[] {MealToFood.MEAL_DB_COLUMN_KEY};
	private static String whereClauseForGetMealForFood =
			MealToFood.FOOD_DB_COLUMN_KEY + "=?";
	/**
	 * Returns the meal for which the provided food is a member.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param food The food whose meal we want
	 * @return The @ref Meal that owns the proivded @ref Food
	 */
	public static Meal getMealForFood(Food food) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_TO_MEAL_DB_NAME, columnsForGetMealForFood,
				whereClauseForGetMealForFood, 
				new String[] {food.id}, null, null, null, "1");
		Meal meal = null;

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "No meal for food with id " + food.id + " in local db");
			return meal;
		}

		Map<String, Object> record = null;
		while (!cursor.isAfterLast()) {
			record = DatabaseUtil.getRecordFromCursor(cursor, MealToFood.COLUMN_TYPES);
			cursor.moveToNext();
		}

		if (record != null) {
			meal = MealUtil.getMealById((String) record.get(MealToFood.MEAL_DB_COLUMN_KEY));
		}
		return meal;
	}

	// SELECT * FROM Meal WHERE objectId IN (
	// 	SELECT meal FROM MealToFood WHERE food IN (
	//		SELECT objectID FROM Food WHERE name = /foodName/
	//	)
	// )
	private static final String baseWhereClauseForGetAllMealsForFoodName = 
			DatabaseUtil.OBJECT_ID_COLUMN_NAME + " IN ( " +
					"SELECT " + MealToFood.MEAL_DB_COLUMN_KEY + 
					" FROM " + Tables.MEAL_TO_FOOD_DB_NAME + 
					" WHERE " + MealToFood.FOOD_DB_COLUMN_KEY + " IN ( " +
					"SELECT " + DatabaseUtil.OBJECT_ID_COLUMN_NAME + 
					" FROM "  + Tables.FOOD_DB_NAME + " WHERE ";
	private static final String whereClauseForGetAllMealsForFoodName = 
			baseWhereClauseForGetAllMealsForFoodName +
			Food.NAME_DB_COLUMN_KEY + " = ?) )";
	/**
	 * Get all meals that contain a food with the provided name.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param foodName The name of the food
	 * @return A List<Meal> of meals that have a food with a matching name
	 */
	public static List<Meal> getAllMealsForFoodName(String foodName) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.MEAL_DB_NAME, null,
				whereClauseForGetAllMealsForFoodName,
				new String[] {foodName}, null, null, null);
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

	// SELECT * FROM Meal WHERE objectId IN (
	// 	SELECT meal FROM MealToFood WHERE food IN (
	//		SELECT objectID FROM Food WHERE name = /foodName/
	//	)
	// )
	private static final String whereClauseForGetAllMealsForFoodNameContaining = 
			baseWhereClauseForGetAllMealsForFoodName +
			" lower(" + Food.NAME_DB_COLUMN_KEY + ") LIKE lower(?)) )";
	/**
	 * Get all the meals that contain a food whose name contains the provided
	 * string.
	 * 
	 * @note This method is synchronous. It should not be called on the main
	 * thread.
	 * 
	 * @param foodName The substring to search for
	 * @return A List<Meal> of meals that have a food with a matching name
	 */
	public static List<Meal> getAllMealsForFoodNameContaining(String foodName) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.MEAL_DB_NAME, null,
				whereClauseForGetAllMealsForFoodNameContaining,
				new String[] {"%" + foodName + "%"}, null, null, null);
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
	 * Gets the average of all carb estimates for food with the
	 * provided name.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param foodName The name of the food
	 * @return A float of the average carb estimate
	 */
	public static float getAverageCarbsForFoodNamed(String foodName) {
		List<Food> foods = getAllFoodsForName(foodName);

		float average = 0;

		for (Food food : foods) {
			average += food.carbs;
		}

		Log.i(LOG_TAG, "Running callback for average carbs for food " + foodName);
		return (float)average / (float)foods.size();

	}

	private static final String whereClauseForGetFoodsForFoodHash =
			DatabaseUtil.OBJECT_ID_COLUMN_NAME + " IN (" +
					"SELECT " + MealToFood.FOOD_DB_COLUMN_KEY + " FROM " +
					Tables.MEAL_TO_FOOD_DB_NAME + " WHERE " +
					MealToFood.MEAL_DB_COLUMN_KEY + " = (SELECT " +
					MealToFoodsHash.MEAL_DB_COLUMN_KEY + " FROM " +
					Tables.MEAL_TO_FOODS_HASH_DB_NAME + " WHERE " +
					MealToFoodsHash.FOODS_HASH_DB_COLUMN_KEY + " = ? LIMIT 1))";
	/**
	 * Gets the foods that match the provided hash.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param foodHash The hash representing a set of foods
	 * @return A List<Food> of the foods for the hash
	 */
	public static List<Food> getFoodsForFoodHash(String foodHash) {
		long start;
		if (GlucloserActivity.LOG_LEVEL >= Log.VERBOSE) {
			start = System.currentTimeMillis();
		}
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.FOOD_DB_NAME, null, whereClauseForGetFoodsForFoodHash,
				new String[] {foodHash}, null, null, null);
		List<Food> foods = new ArrayList<Food>();

		if (!cursor.moveToFirst()) {
			return foods;
		}

		while (!cursor.isAfterLast()) {
			foods.add(Food.fromMap(DatabaseUtil.getRecordFromCursor(
					cursor, Food.COLUMN_TYPES)));
			cursor.moveToNext();
		}

		if (GlucloserActivity.LOG_LEVEL >= Log.VERBOSE) {
			Log.v(LOG_TAG, "GetFoodsForFoodHash took " + (System.currentTimeMillis() - start));
		}
		return foods;
	}

	/**
	 * Save the food to the database.
	 * Updates the updatedAt time.
	 * 
	 * @param food The food to save
	 * @return The new id if the save was successful or -1
	 */
	public static long saveFood(Food food) {
		ContentValues values = new ContentValues();

		if (food.createdAt == null) {
			food.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (food.updatedAt == null) {
			food.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME), food.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(food.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(food.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
				Food.NAME_DB_COLUMN_KEY), food.name);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
				Food.CARBS_DB_COLUMN_KEY), food.carbs);

		byte[] image = food.getImage();
		if (image != null) {
			values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
					Food.IMAGE_DB_COLUMN_KEY), image);
		}
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
				Food.CORRECTION_DB_COLUMN_KEY), food.isCorrection);

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
				Food.DATE_EATEN_DB_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(food.dateEaten));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				food.needsUpload);

		DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.FOOD_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (code == -1) {
			Log.i(LOG_TAG, "Error code recieved from insert: " + code);
			Log.i(LOG_TAG, "Values are: " + values);
		} else {
			DatabaseUtil.instance().getWritableDatabase().setTransactionSuccessful();
		}
        DatabaseUtil.instance().getWritableDatabase().endTransaction();

		return code;
	}
}
