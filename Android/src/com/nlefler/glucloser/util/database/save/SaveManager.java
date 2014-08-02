package com.nlefler.glucloser.util.database.save;

import android.os.AsyncTask;

import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.food.FoodUtil;
import com.nlefler.glucloser.model.meal.MealUtil;
import com.nlefler.glucloser.model.place.PlaceUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
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

                place.needsUpload = true;
				if (PlaceUtil.savePlace(place)) {
					return null;
				}
				
				DatabaseUtil.setNeedsSync();
				
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
                MealUtil.saveMeal(meal);

				for (Food food : meal.getFoods()) {
					food.needsUpload = true;
                    FoodUtil.saveFood(food);
				}
				meal.getPlace().needsUpload = true;
				PlaceUtil.savePlace(meal.getPlace());

				DatabaseUtil.setNeedsSync();

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
				
				if (FoodUtil.saveFood(food)) {
					return null;
				}
				
				DatabaseUtil.setNeedsSync();
				
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
