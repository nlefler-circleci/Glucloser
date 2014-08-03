package com.nlefler.glucloser.model.sync;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.TableCreationSQL;

import java.util.Date;
import java.util.UUID;

/**
 * Created by nathan on 8/2/14.
 */
public class SyncUpEventCreationSQL extends TableCreationSQL {
    public static String creationSQL = "CREATE TABLE " + DatabaseUtil.tableNameForModel(SyncUpEvent.class) +
            " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.PARSE_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.CREATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.UPDATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.DATA_VERSION_COLUMN_NAME + " INTEGER, " +
            SyncUpEvent.BolusColumnName + " INTEGER, " +
            SyncUpEvent.FoodColumnName + " INTEGER, " +
            SyncUpEvent.MeterDataColumnName + " INTEGER, " +
            SyncUpEvent.MealColumnName + " INTEGER, " +
            SyncUpEvent.PlaceColumnName + " INTEGER);";

}
