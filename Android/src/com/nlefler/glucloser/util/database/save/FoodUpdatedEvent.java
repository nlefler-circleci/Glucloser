package com.nlefler.glucloser.util.database.save;

import com.nlefler.glucloser.types.Food;

/**
 * Created by nathan on 5/18/14.
 */
public class FoodUpdatedEvent {
    private Food _food;

    public FoodUpdatedEvent(Food food) {
        _food = food;
    }

    public Food getFood() {
        return _food;
    }
}
