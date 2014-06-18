package com.nlefler.glucloser.util.database.upgrade;

import com.nlefler.glucloser.model.bolus.BolusCreationSQL;
import com.nlefler.glucloser.model.food.FoodCreationSQL;
import com.nlefler.glucloser.model.meal.MealCreationSQL;
import com.nlefler.glucloser.model.meterdata.MeterDataCreationSQL;
import com.nlefler.glucloser.model.place.PlaceCreationSQL;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.sprinkles.annotations.Table;

public class ZeroToOne extends DatabaseUpgrader {
    @Override
    protected String[] getUpgradeCommands() {
        List<String> commands = new ArrayList<String>();
        String[] tableCreationSQLs = new String[] {
                BolusCreationSQL.creationSQL,
                FoodCreationSQL.creationSQL,
                MealCreationSQL.creationSQL,
                MeterDataCreationSQL.creationSQL,
                PlaceCreationSQL.creationSQL
        };
		for (String sql : tableCreationSQLs) {
            commands.add(sql);
		}

        return commands.toArray(new String[0]);
    }
}
