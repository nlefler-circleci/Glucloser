package com.nlefler.glucloser.model.placetomeal;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import android.util.Log;

import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.placetomeal.PlaceToMeal;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.model.meal.Meal;

import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.annotations.Table;

public class PlaceToMealUtil {
	private static final String LOG_TAG = "Pump_Place_To_Meal_Util";

	public static void savePlaceToMeal(PlaceToMeal placeToMeal) {
		placeToMeal.save();
	}

	public static PlaceToMeal getPlaceToMealForPlace(Place place) {
		String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(PlaceToMeal.class) +
			" WHERE " + PlaceToMeal.PLACE_DB_COLUMN_KEY + " = ?";
		PlaceToMeal placeToMeal = Query.one(PlaceToMeal.class, select, place.glucloserId).get();

		return placeToMeal;
	}

	public static PlaceToMeal getPlaceToMealForMeal(Meal meal) {
		String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(PlaceToMeal.class) +
			" WHERE " + PlaceToMeal.MEAL_DB_COLUMN_KEY + " = ?";
		PlaceToMeal placeToMeal = Query.one(PlaceToMeal.class, select, meal.glucloserId).get();

		return placeToMeal;
	}
}
