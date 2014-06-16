package com.nlefler.glucloser.model.food;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.MealToFood;
import com.nlefler.glucloser.model.meal.MealUtil;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.place.PlaceUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;

import se.emilsjolander.sprinkles.Query;


public class FoodUtil {
	private static final String LOG_TAG = "Glucloser_Food_Util";


	/**
	 * Returns names for all foods in the database.
	 * 
	 * @note This method is synchronous. It should not
	 * be called on the main thread.
	 * 
	 * @return A List<String> of food names
	 */
	public static List<String> getAllFoodNames() {
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Food.class) +
                " ORDER BY " + Food.NAME_DB_COLUMN_KEY + " ASC";
		List<Food> sortedFoods = Query.many(Food.class, select).get().asList();

        List<String> sortedNames = new ArrayList<String>(sortedFoods.size());
        for (Food food : sortedFoods) {
            sortedNames.add(food.name);
        }

		return sortedNames;
	}

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
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Food.class) +
                " WHERE lower(" + Food.NAME_DB_COLUMN_KEY + ") LIKE lower(?%) ORDERED BY " +
                Food.NAME_DB_COLUMN_KEY + " ASC";

        List<Food> sortedFoods = Query.many(Food.class, select, start).get().asList();
		List<String> sortedNames = new ArrayList<String>(sortedFoods.size());

        for (Food food : sortedFoods) {
            sortedNames.add(food.name);
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
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Food.class) +
                " WHERE lower(" + Food.NAME_DB_COLUMN_KEY + ") LIKE lower(?) ORDERED BY " +
                Food.NAME_DB_COLUMN_KEY + " ASC";

		List<Food> foods = Query.many(Food.class, select, foodName).get().asList();

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
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Food.class) +
                "WHERE " + DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " = ?";

		Food food = Query.one(Food.class, select, id).get();

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
		return PlaceUtil.getPlaceById(MealUtil.getPlaceForMeal(FoodUtil.getMealForFood(food)).placeGlucloserId);
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
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Food.class) +
                " WHERE lower(" + Food.NAME_DB_COLUMN_KEY + ") LIKE lower(%?%) ORDER BY " +
                Food.NAME_DB_COLUMN_KEY + " ASC";

		List<Food> foods = Query.many(Food.class, select, name).get().asList();

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
			DatabaseUtil.PARSE_ID_COLUMN_NAME + " IN ( " +
					"SELECT " + MealToFood.MEAL_DB_COLUMN_KEY + 
					" FROM " + Tables.MEAL_TO_FOOD_DB_NAME + 
					" WHERE " + MealToFood.FOOD_DB_COLUMN_KEY + " IN ( " +
					"SELECT " + DatabaseUtil.PARSE_ID_COLUMN_NAME +
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

		return (float)average / (float)foods.size();

	}

	/**
	 * Save the food to the database.
	 * Updates the updatedAt time.
	 * 
	 * @param food The food to save
	 * @return The new id if the save was successful or -1
	 */
	public static boolean saveFood(Food food) {
		ContentValues values = new ContentValues();

		if (food.createdAt == null) {
			food.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (food.updatedAt == null) {
			food.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

        return food.save();
	}
}
