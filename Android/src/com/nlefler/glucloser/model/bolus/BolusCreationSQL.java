package com.nlefler.glucloser.model.bolus;

import android.provider.ContactsContract;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.TableCreationSQL;

/**
 * Created by nathan on 6/17/14.
 */
public class BolusCreationSQL extends TableCreationSQL {
    public static String creationSQL = "CREATE TABLE " + DatabaseUtil.tableNameForModel(Bolus.class) +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.PARSE_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.CREATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.UPDATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.DATA_VERSION_COLUMN_NAME + " INTEGER);";
}
