package com.nlefler.glucloser.model.place;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.TableCreationSQL;

/**
 * Created by nathan on 6/17/14.
 */
public class PlaceCreationSQL extends TableCreationSQL {
    public static String creationSQL = "CREATE TABLE " + DatabaseUtil.tableNameForModel(Place.class) +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.PARSE_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.CREATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.UPDATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.DATA_VERSION_COLUMN_NAME + " INTEGER, " +
            Place.LATITUDE_DB_COLUMN_KEY + " REAL, " +
            Place.LONGITUDE_DB_COLUMN_KEY + " REAL, " +
            Place.FOURSQUARE_ID_COLUMN_KEY + " TEXT, " +
            Place.NAME_DB_COLUMN_KEY + " TEXT, " +
            Place.LAST_VISITED_COLUMN_KEY + " INTEGER, " +
            Place.READABLE_ADDRESS_COLUMN_KEY + " TEXT);";

}
