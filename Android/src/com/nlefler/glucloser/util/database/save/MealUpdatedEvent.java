package com.nlefler.glucloser.util.database.save;

import com.nlefler.glucloser.model.meal.Meal;

/**
 * Created by nathan on 5/18/14.
 */
public class MealUpdatedEvent {
    private Meal _meal;

    public MealUpdatedEvent(Meal meal) {
        _meal = meal;
    }

    public Meal getMeal() {
        return _meal;
    }
}
