package com.nlefler.glucloser.model.meterdata;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.TableCreationSQL;

/**
 * Created by nathan on 6/17/14.
 */
public class MeterDataCreationSQL extends TableCreationSQL {
    public static String creationSQL = "CREATE TABLE " +
            DatabaseUtil.tableNameForModel(MeterData.class) +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.PARSE_ID_COLUMN_NAME + " TEXT, " +
            DatabaseUtil.CREATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.UPDATED_AT_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + " INTEGER, " +
            DatabaseUtil.DATA_VERSION_COLUMN_NAME + " INTEGER, " +

            MeterData.DATE_DB_COLUMN_NAME_DB_COLUMN_NAME + " INTEGER," +
            MeterData.TIME_DB_COLUMN_NAME_DB_COLUMN_NAME + " INTEGER," +
            MeterData.TIMESTAMP_DB_COLUMN_NAME + " INTEGER," +
            MeterData.NEW_DEVICE_TIME_DB_COLUMN_NAME_DB_COLUMN_NAME + " INTEGER," +
            MeterData.BG_READING__MG_DL_COLUMN_NAME + " INTEGER," +
            MeterData.LINKED_BG_METER_ID_DB_COLUMN_NAME + " INTEGER," +
            MeterData.TEMP_BASAL_AMOUNT__U_H__DB_COLUMN_NAME + " REAL," +
            MeterData.TEMP_BASAL_TYPE_DB_COLUMN_NAME + " TEXT," +
            MeterData.TEMP_BASAL_DURATION__HH_MM_SS__DB_COLUMN_NAME + " INTEGER," +
            MeterData.BOLUS_TYPE_DB_COLUMN_NAME + " TEXT," +
            MeterData.BOLUS_VOLUME_SELECTED__U__DB_COLUMN_NAME + " REAL," +
            MeterData.BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME + " REAL," +
            MeterData.PROGRAMMED_BOLUS_DURATION__HH_MM_SS__DB_COLUMN_NAME + " INTEGER," +
            MeterData.PRIME_TYPE_DB_COLUMN_NAME + " TEXT," +
            MeterData.PRIME_VOLUME_DELIVERED__U__DB_COLUMN_NAME + " REAL," +
            MeterData.SUSPEND_DB_COLUMN_NAME + " INTEGER," +
            MeterData.REWIND_DB_COLUMN_NAME + " INTEGER," +
            MeterData.BWZ_ESTIMATE__U__DB_COLUMN_NAME + " REAL," +
            MeterData.BWZ_TARGET_HIGH_BG__MG_DL__DB_COLUMN_NAME + " INTEGER," +
            MeterData.BWZ_TARGET_LOW_BG__MG_DL__DB_COLUMN_NAME + " INTEGER," +
            MeterData.BWZ_CARB_RATIO__GRAMS__DB_COLUMN_NAME + " INTEGER," +
            MeterData.BWZ_INSULIN_SENSITIVITY__MG_DL__DB_COLUMN_NAME + " INTEGER," +
            MeterData.BWZ_CARB_INPUT__GRAMS__DB_COLUMN_NAME + " INTEGER," +
            MeterData.BWZ_BG_INPUT__MG_DL__DB_COLUMN_NAME + " INTEGER," +
            MeterData.BWZ_CORRECTION_ESTIMATE__U__DB_COLUMN_NAME + " REAL," +
            MeterData.BWZ_FOOD_ESTIMATE__U__DB_COLUMN_NAME + " REAL," +
            MeterData.BWZ_ACTIVE_INSULIN__U__DB_COLUMN_NAME + " REAL," +
            MeterData.ALARM_DB_COLUMN_NAME + " TEXT," +
            MeterData.SENSOR_CALIBRATION_BG__MG_DL__DB_COLUMN_NAME + " INTEGER," +
            MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME + " INTEGER," +
            MeterData.ISIG_VALUE_DB_COLUMN_NAME + " REAL," +
            MeterData.DAILY_INSULIN_TOTAL__U__DB_COLUMN_NAME + " REAL," +
            MeterData.RAW_TYPE_DB_COLUMN_NAME + " TEXT," +
            MeterData.RAW_VALUES_DB_COLUMN_NAME + " TEXT," +
            MeterData.RAW_ID_DB_COLUMN_NAME + " TEXT," +
            MeterData.RAW_UPLOAD_ID_DB_COLUMN_NAME + " TEXT," +
            MeterData.RAW_SEQ_NUM_DB_COLUMN_NAME + " TEXT," +
            MeterData.RAW_DEVICE_TYPE_DB_COLUMN_NAME + " TEXT);";

}
