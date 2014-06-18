package com.nlefler.glucloser.model.food;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.TableCreationSQL;

/**
 * Created by nathan on 6/17/14.
 */
public class FoodCreationSQL extends TableCreationSQL {
    public static String creationSQL = "CREATE TABLE " + DatabaseUtil.tableNameForModel(Food.class) +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.PARSE_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.CREATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.UPDATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.DATA_VERSION_COLUMN_NAME + " INTEGER, " +
            Food.FOOD_DB_NAME + " TEXT, " +
            Food.CARBS_DB_COLUMN_KEY + " INTEGER, " +
            Food.DATE_EATEN_DB_COLUMN_NAME + " INTEGER, " +
            Food.CORRECTION_DB_COLUMN_KEY + " INTEGER, " +
            Food.MEAL_GLUCLOSER_ID_COLUMN_NAME + " TEXT);";
}
