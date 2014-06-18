package com.nlefler.glucloser.model.meal;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.TableCreationSQL;

/**
 * Created by nathan on 6/17/14.
 */
public class MealCreationSQL extends TableCreationSQL {
    public static String creationSQL = "CREATE TABLE " + DatabaseUtil.tableNameForModel(Meal.class) +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.PARSE_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.CREATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.UPDATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.DATA_VERSION_COLUMN_NAME + " INTEGER, " +
            Meal.DATE_EATEN_DB_COLUMN_NAME + " INTEGER, " +
            Meal.PLACE_GLUCLOSER_ID_COLUMN_NAME + " TEXT);";
}
