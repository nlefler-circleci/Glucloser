package com.nlefler.glucloser.dataSource;

import com.nlefler.glucloser.models.Meal;
import com.nlefler.glucloser.models.MealParcelable;

import java.util.UUID;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealFactory {
    public static Meal Meal() {
        Meal meal = new Meal();
        meal.setMealId(UUID.randomUUID().toString());

        return meal;
    }

    public static MealParcelable ParcelableFromMeal(Meal meal) {
        MealParcelable parcelable = new MealParcelable();
        parcelable.setPlace(meal.getPlace());
        parcelable.setCarbs(meal.getCarbs());
        parcelable.setInsulin(meal.getInsulin());
        parcelable.setMealId(meal.getMealId());

        return parcelable;
    }

    public static Meal MealFromParcelable(MealParcelable parcelable) {
        Meal meal = new Meal();
        meal.setInsulin(parcelable.getInsulin());
        meal.setMealId(parcelable.getMealId());
        meal.setCarbs(parcelable.getCarbs());
        meal.setPlace(parcelable.getPlace());

        return meal;
    }
}
