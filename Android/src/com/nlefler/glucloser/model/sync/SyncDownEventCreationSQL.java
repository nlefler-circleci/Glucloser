package com.nlefler.glucloser.model.sync;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.TableCreationSQL;

import java.util.Date;
import java.util.UUID;

/**
 * Created by nathan on 8/2/14.
 */
public class SyncDownEventCreationSQL extends TableCreationSQL {
    public static String creationSQL = "CREATE TABLE " + DatabaseUtil.tableNameForModel(SyncDownEvent.class) +
            " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.PARSE_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.CREATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.UPDATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.DATA_VERSION_COLUMN_NAME + " INTEGER, " +
            SyncDownEvent.BolusColumnName + " INTEGER, " +
            SyncDownEvent.FoodColumnName + " INTEGER, " +
            SyncDownEvent.MeterDataColumnName + " INTEGER, " +
            SyncDownEvent.MealColumnName + " INTEGER, " +
            SyncDownEvent.PlaceColumnName + " INTEGER);";
}
