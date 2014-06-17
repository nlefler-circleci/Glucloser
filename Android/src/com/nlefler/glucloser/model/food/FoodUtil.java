package com.nlefler.glucloser.model.food;

import java.util.ArrayList;
import java.util.List;

import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.meal.MealUtil;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.place.PlaceUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;

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
        // TODO: Unique on name
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

        // TODO: Unique on name
        List<Food> sortedFoods = Query.many(Food.class, select, start).get().asList();
		List<String> sortedNames = new ArrayList<String>(sortedFoods.size());

        for (Food food : sortedFoods) {
            sortedNames.add(food.name);
        }

		return sortedNames;

	}

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
        return MealUtil.getPlaceForMeal(FoodUtil.getMealForFood(food));
	}

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

    public static List<Food> getAllFoodsForMeal(Meal meal) {
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Food.class) +
                " WHERE " + Food.MEAL_GLUCLOSER_ID_COLUMN_NAME + " = ?";
        List<Food> foods = Query.many(Food.class, select, meal.glucloserId).get().asList();

        return foods;
    }

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
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Meal.class) +
                " WHERE " + DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " = ?";

		Meal meal = Query.one(Meal.class, select, food.mealGlucloserId).get();

		return meal;
	}

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
        List<Food> foods = getAllFoodsForName(foodName);
        List<Meal> meals = new ArrayList<Meal>(foods.size());

        for (Food food : foods) {
            meals.add(getMealForFood(food));
        }

		return meals;
	}

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
        List<Food> foods = getAllFoodsWithNameContaining(foodName);
        List<Meal> meals = new ArrayList<Meal>(foods.size());

        for (Food food : foods) {
            meals.add(getMealForFood(food));
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
        return food.updateFieldsAndSave();
	}
}
