package com.nlefler.glucloser.util.database.upgrade;

import com.nlefler.glucloser.model.bolus.Bolus;
import com.nlefler.glucloser.model.bolus.BolusCreationSQL;
import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.food.FoodCreationSQL;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.meal.MealCreationSQL;
import com.nlefler.glucloser.model.meterdata.MeterData;
import com.nlefler.glucloser.model.meterdata.MeterDataCreationSQL;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.place.PlaceCreationSQL;
import com.nlefler.glucloser.model.sync.SyncDownEvent;
import com.nlefler.glucloser.model.sync.SyncDownEventCreationSQL;
import com.nlefler.glucloser.model.sync.SyncUpEvent;
import com.nlefler.glucloser.model.sync.SyncUpEventCreationSQL;


import java.util.Date;

import se.emilsjolander.sprinkles.annotations.Table;

public class ZeroToOne extends DatabaseUpgrader {
    @Override
    protected String[] getUpgradeCommands() {
        String[] tableCreationSQLs = new String[] {
                BolusCreationSQL.creationSQL,
                FoodCreationSQL.creationSQL,
                MealCreationSQL.creationSQL,
                MeterDataCreationSQL.creationSQL,
                PlaceCreationSQL.creationSQL,
                SyncDownEventCreationSQL.creationSQL,
                SyncUpEventCreationSQL.creationSQL,
        };

        return tableCreationSQLs;
    }
}
