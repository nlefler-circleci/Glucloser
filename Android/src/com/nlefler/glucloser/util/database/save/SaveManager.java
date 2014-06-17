package com.nlefler.glucloser.util.database.save;

import android.os.AsyncTask;

import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.food.FoodUtil;
import com.nlefler.glucloser.model.meal.MealUtil;
import com.nlefler.glucloser.model.place.PlaceUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.squareup.otto.Bus;

public class SaveManager {
	private static final String LOG_TAG = "Glucloser_Save_Manager";
	
	private static SaveManager _instance;

    private static Bus _placeUpdatedBus;
    private static Bus _placeSavedBus;
    private static Bus _mealUpdatedBus;
    private static Bus _foodUpdatedBus;
	
	public SaveManager() {
        _placeUpdatedBus = new Bus();
        _placeSavedBus = new Bus();
        _mealUpdatedBus = new Bus();
        _foodUpdatedBus = new Bus();
	}
	
	public static synchronized void initialize() {
		if (_instance == null) {
            _instance = new SaveManager();
		}
	}

    public static Bus getPlaceUpdatedBus() {
        return _placeUpdatedBus;
    }

    public static Bus getPlaceSavedBus() {
        return _placeSavedBus;
    }

    public static Bus getMealUpdatedBus() {
        return _mealUpdatedBus;
    }

    public static Bus getFoodUpdatedBus() {
        return _foodUpdatedBus;
    }

	public static void savePlace(Place place) {
		(new AsyncTask<Place, Void, Place>() {

			@Override
			protected Place doInBackground(Place... params) {
				if (params.length == 0 || !(params[0] instanceof Place)) {
					return null;
				}
				Place place = params[0];
				
				long placeId = PlaceUtil.savePlace(place);
				if (placeId == -1) {
					return null;
				}
				
				DatabaseUtil.setNeedsSync();
				
				place.id = DatabaseUtil.instance().objectIdForRowId(Tables.PLACE_DB_NAME, placeId);
				
				return place;
			}
			
			@Override
			protected void onPostExecute(Place place) {
				if (place != null) {
                    _placeUpdatedBus.post(new PlaceUpdatedEvent(place));
				} else {
//					NotificationCenter.getInstance().postNotificationWithArguments(SAVE_FAILED_NOTIFICATION, place);
				}
			}

		}).execute(place);
	}
	
	public static void saveMeal(Meal meal) {
		(new AsyncTask<Meal, Void, Meal>() {

			@Override
			protected Meal doInBackground(Meal... params) {
				if (params.length == 0 || !(params[0] instanceof Meal)) {
					return null;
				}
				Meal meal = params[0];
				
				// Set needsUpload flags
				meal.needsUpload = true;
				for (MealToFood mealToFood : meal.mealToFoods) {
					mealToFood.needsUpload = true;
					mealToFood.food.needsUpload = true;
				}
				meal.placeToMeal.needsUpload = true;
				meal.placeToMeal.place.needsUpload = true;

				// Start writing
				DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();

				// PlaceToMeal
				long placeToMealId = PlaceToMealUtil.savePlaceToMeal(meal.placeToMeal);
				
				// Place
				long placeId = PlaceUtil.savePlace(meal.placeToMeal.place);

				// PlaceToFoodsHash
				PlaceToFoodsHash ptfh = new PlaceToFoodsHash();
				ptfh.place = meal.placeToMeal.place;
				ptfh.foodsHash = MealUtil.generateHashForFoods(meal);;
				ptfh.needsUpload = true;
				long placeToFoodsHashId = PlaceToFoodsHashUtil.savePlaceToFoodsHash(ptfh);

				// MealToFoods, Foods, and Barcodes
				long mealToFoodIds[] = new long[meal.mealToFoods.size()];
				long foodIds[] = new long[meal.mealToFoods.size()];
				boolean foodSaveFail = false;
				for (int i = 0; i < meal.mealToFoods.size(); ++i) {
					MealToFood mealToFood = meal.mealToFoods.get(i);
					
					mealToFoodIds[i] = MealToFoodUtil.saveMealToFood(mealToFood);
					foodIds[i] = FoodUtil.saveFood(mealToFood.food);

					if (mealToFoodIds[i] == -1 || foodIds[i] == -1) {
						foodSaveFail = true;
					}
				}
				
				// Meal
				long mealId = MealUtil.saveMeal(meal);

				// Transaction successful?
				if (placeToMealId == -1 || placeId == -1 || placeToFoodsHashId == -1 ||
						mealId == -1 ||	foodSaveFail) {
					DatabaseUtil.instance().getWritableDatabase().endTransaction();
					return null;
				}
				
				// Finish writing
				DatabaseUtil.instance().getWritableDatabase().setTransactionSuccessful();
				DatabaseUtil.instance().getWritableDatabase().endTransaction();
				DatabaseUtil.setNeedsSync();
				
				// Update objectIds for meal and all linked objects
				meal.id = DatabaseUtil.instance().objectIdForRowId(Tables.MEAL_DB_NAME, mealId);
				meal.placeToMeal.id = DatabaseUtil.instance().objectIdForRowId(Tables.PLACE_TO_MEAL_DB_NAME, placeToMealId);
				meal.placeToMeal.place.id = DatabaseUtil.instance().objectIdForRowId(Tables.PLACE_DB_NAME, placeId);
				for (int i = 0; i < mealToFoodIds.length; ++i) {
					MealToFood m2f = meal.mealToFoods.get(i);
					
					m2f.id = DatabaseUtil.instance().objectIdForRowId(Tables.MEAL_TO_FOOD_DB_NAME, mealToFoodIds[i]);
					m2f.food.id = DatabaseUtil.instance().objectIdForRowId(Tables.FOOD_DB_NAME, foodIds[i]);
				}
				
				
				return meal;
			}
			
			@Override
			protected void onPostExecute(Meal meal) {
				if (meal != null) {
                    _mealUpdatedBus.post(new MealUpdatedEvent(meal));
				} else {
//					NotificationCenter.getInstance().postNotificationWithArguments(SAVE_FAILED_NOTIFICATION, meal);
				}
			}

		}).execute(meal);
	}
	
	public static void saveFood(Food food) {
		(new AsyncTask<Food, Void, Food>() {

			@Override
			protected Food doInBackground(Food... params) {
				if (params.length == 0 || !(params[0] instanceof Food)) {
					return null;
				}
				Food food = params[0];
				
				long foodId = FoodUtil.saveFood(food);
				if (foodId == -1) {
					return null;
				}
				
				DatabaseUtil.setNeedsSync();
				
				food.id = DatabaseUtil.instance().objectIdForRowId(Tables.FOOD_DB_NAME, foodId);
				
				return food;
			}
			
			@Override
			protected void onPostExecute(Food food) {
				if (food != null) {
                    _foodUpdatedBus.post(new FoodUpdatedEvent(food));
				} else {
//					NotificationCenter.getInstance().postNotificationWithArguments(SAVE_FAILED_NOTIFICATION, food);
				}
			}

		}).execute(food);
	}
}
