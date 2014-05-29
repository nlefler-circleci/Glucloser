package com.nlefler.glucloser.model.meal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.util.Log;

import com.nlefler.glucloser.model.MealToFood;
import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.PlaceToMeal;
import com.nlefler.glucloser.util.database.DatabaseUtil;

import se.emilsjolander.sprinkles.Query;

public class MealUtil {
	private static final String LOG_TAG = "Glucloser_Meal_Util";

	/**
	 * Get all foods for the given meal.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param meal
	 * @return A List<Food> for the meal
	 */
	public static List<Food> getFoodsForMeal(Meal meal) {
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Food.class) +
                " WHERE " + DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " IN (" +
                " SELECT " + MealToFood.FOOD_DB_COLUMN_KEY + " WHERE " + MealToFood.MEAL_DB_COLUMN_KEY +
                " = ?)";
		List<Food> foods = Query.many(Food.class, select, meal.glucloserId).get().asList();

		return foods;
	}

	/**
	 * Returns recently eaten meals
	 * @param mealLimit Maximum number of meals to return
	 * @return A List<Meal> of recently eaten meals
	 */
	public static List<Meal> getRecentMeals(int mealLimit) {
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Meal.class) +
                "ORDER BY " + Meal.DATE_EATEN_DB_COLUMN_NAME + " DESC LIMIT ?";
		List<Meal> meals = Query.many(Meal.class, select, mealLimit).get().asList();

		return meals;
	}

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
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Meal.class) +
                " WHERE " + DatabaseUtil.CREATED_AT_COLUMN_NAME + " >= ?" +
                " AND " + DatabaseUtil.CREATED_AT_COLUMN_NAME + " <= ? ORDERED BY " +
                DatabaseUtil.UPDATED_AT_COLUMN_NAME + " DESC";
		List<Meal> meals = Query.many(Meal.class, select, fromDate, toDate).get().asList();

		return meals;
	}

	/**
	 * Get a meal from the database with the given id.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @param id A database, Parse, or Glucloser id
	 * @return A @ref Meal representing the meal in the database, or
	 * null if no meal is found with the given id
	 */
	public static Meal getMealById(String id) {
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Meal.class) +
                " WHERE " + DatabaseUtil.ID_COLUMN_NAME + " = ? OR " +
                DatabaseUtil.PARSE_ID_COLUMN_NAME + " = ? OR " +
                DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " = ?";
		Meal meal = Query.one(Meal.class, select, id).get();

		return meal;
	}

	/**
	 * Get the place for the given meal.
	 * 
	 * @note This method is synchronous. It should not be called on the
	 * main thread.
	 * 
	 * @param meal
	 * @return A @ref PlaceToMeal
	 */
	public static Place getPlaceForMeal(Meal meal) {
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(Place.class) +
                " WHERE " + DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " = (SELECT " +
                PlaceToMeal.PLACE_DB_COLUMN_KEY + " FROM " + DatabaseUtil.tableNameForModel(PlaceToMeal.class) +
                " WHERE " + PlaceToMeal.MEAL_DB_COLUMN_KEY + " = ?)";
        Place place = Query.one(Place.class, select, meal.glucloserId).get();

		return place;
	}

	public static boolean saveMeal(Meal meal) {
		// Save meal
		if (meal.createdAt == null) {
			meal.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (meal.updatedAt == null) {
			meal.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

        return meal.save();
	}
}
