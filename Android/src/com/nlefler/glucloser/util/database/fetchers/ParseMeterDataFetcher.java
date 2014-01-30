package com.nlefler.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.nlefler.glucloser.util.database.Tables;
import com.nlefler.glucloser.types.MeterData;
import com.parse.ParseObject;

public class ParseMeterDataFetcher extends SyncFetcher {
	private static final String LOG_TAG = "Pump_Parse_Meter_Data_Fetcher";

	@Override
	public List<Map<String, Object>> fetchRecords(Date sinceDate) {
		Log.i(LOG_TAG, "Starting fetch for " + Tables.METER_DATA_DB_NAME + " from Parse");

        // TODO: Allow user to set this
        // Only sync last two weeks to speed up full sync time during development
        Date overrideDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 14);
        if (sinceDate == null || overrideDate.compareTo(sinceDate) > 0) {
            sinceDate = overrideDate;
        }

		List<ParseObject> parseObjects = fetchParseObjectsForTableSinceDate(
				Tables.METER_DATA_DB_NAME, sinceDate, LOG_TAG);
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();

		for (ParseObject object : parseObjects) {
			Map<String, Object> record = new HashMap<String, Object>();

			getCommonValuesFromParseObjectIntoMap(object, record);

			record.put(MeterData.INDEX_DB_COLUMN_NAME,									object.getNumber(MeterData.INDEX_DB_COLUMN_NAME));
			record.put(MeterData.DATE_DB_COLUMN_NAME_DB_COLUMN_NAME,					object.getDate(MeterData.DATE_DB_COLUMN_NAME_DB_COLUMN_NAME));
			record.put(MeterData.TIME_DB_COLUMN_NAME_DB_COLUMN_NAME,					object.getNumber(MeterData.TIME_DB_COLUMN_NAME_DB_COLUMN_NAME));
			record.put(MeterData.TIMESTAMP_DB_COLUMN_NAME,								object.getDate(MeterData.TIMESTAMP_DB_COLUMN_NAME));
			record.put(MeterData.NEW_DEVICE_TIME_DB_COLUMN_NAME_DB_COLUMN_NAME,			object.getString(MeterData.NEW_DEVICE_TIME_DB_COLUMN_NAME_DB_COLUMN_NAME));
			record.put(MeterData.BG_READING__MG_DL_COLUMN_NAME,							object.getNumber(MeterData.BG_READING__MG_DL_COLUMN_NAME));									
			record.put(MeterData.LINKED_BG_METER_ID_DB_COLUMN_NAME,						object.getString(MeterData.LINKED_BG_METER_ID_DB_COLUMN_NAME));
			record.put(MeterData.TEMP_BASAL_AMOUNT__U_H__DB_COLUMN_NAME,				object.getNumber(MeterData.TEMP_BASAL_AMOUNT__U_H__DB_COLUMN_NAME));
			record.put(MeterData.TEMP_BASAL_TYPE_DB_COLUMN_NAME,						object.getString(MeterData.TEMP_BASAL_TYPE_DB_COLUMN_NAME));
			record.put(MeterData.TEMP_BASAL_DURATION__HH_MM_SS__DB_COLUMN_NAME,			object.getString(MeterData.TEMP_BASAL_DURATION__HH_MM_SS__DB_COLUMN_NAME));
			record.put(MeterData.BOLUS_TYPE_DB_COLUMN_NAME,								object.getString(MeterData.BOLUS_TYPE_DB_COLUMN_NAME));
			record.put(MeterData.BOLUS_VOLUME_SELECTED__U__DB_COLUMN_NAME, 				object.getNumber(MeterData.BOLUS_VOLUME_SELECTED__U__DB_COLUMN_NAME));
			record.put(MeterData.BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME, 			object.getNumber(MeterData.BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME));
			record.put(MeterData.PROGRAMMED_BOLUS_DURATION__HH_MM_SS__DB_COLUMN_NAME,	object.getString(MeterData.PROGRAMMED_BOLUS_DURATION__HH_MM_SS__DB_COLUMN_NAME));
			record.put(MeterData.PRIME_TYPE_DB_COLUMN_NAME,								object.getString(MeterData.PRIME_TYPE_DB_COLUMN_NAME));
			record.put(MeterData.PRIME_VOLUME_DELIVERED__U__DB_COLUMN_NAME,				object.getNumber(MeterData.PRIME_VOLUME_DELIVERED__U__DB_COLUMN_NAME));
			record.put(MeterData.SUSPEND_DB_COLUMN_NAME,								object.getString(MeterData.SUSPEND_DB_COLUMN_NAME));
			record.put(MeterData.REWIND_DB_COLUMN_NAME,									object.getString(MeterData.REWIND_DB_COLUMN_NAME));
			record.put(MeterData.BWZ_ESTIMATE__U__DB_COLUMN_NAME,						object.getNumber(MeterData.BWZ_ESTIMATE__U__DB_COLUMN_NAME));
			record.put(MeterData.BWZ_TARGET_HIGH_BG__MG_DL__DB_COLUMN_NAME,				object.getNumber(MeterData.BWZ_TARGET_HIGH_BG__MG_DL__DB_COLUMN_NAME));
			record.put(MeterData.BWZ_TARGET_LOW_BG__MG_DL__DB_COLUMN_NAME,				object.getNumber(MeterData.BWZ_TARGET_LOW_BG__MG_DL__DB_COLUMN_NAME));
			record.put(MeterData.BWZ_CARB_RATIO__GRAMS__DB_COLUMN_NAME,					object.getNumber(MeterData.BWZ_CARB_RATIO__GRAMS__DB_COLUMN_NAME));
			record.put(MeterData.BWZ_INSULIN_SENSITIVITY__MG_DL__DB_COLUMN_NAME,		object.getNumber(MeterData.BWZ_INSULIN_SENSITIVITY__MG_DL__DB_COLUMN_NAME));
			record.put(MeterData.BWZ_CARB_INPUT__GRAMS__DB_COLUMN_NAME,					object.getNumber(MeterData.BWZ_CARB_INPUT__GRAMS__DB_COLUMN_NAME));
			record.put(MeterData.BWZ_BG_INPUT__MG_DL__DB_COLUMN_NAME,					object.getNumber(MeterData.BWZ_BG_INPUT__MG_DL__DB_COLUMN_NAME));
			record.put(MeterData.BWZ_CORRECTION_ESTIMATE__U__DB_COLUMN_NAME,			object.getNumber(MeterData.BWZ_CORRECTION_ESTIMATE__U__DB_COLUMN_NAME));
			record.put(MeterData.BWZ_FOOD_ESTIMATE__U__DB_COLUMN_NAME,					object.getNumber(MeterData.BWZ_FOOD_ESTIMATE__U__DB_COLUMN_NAME));
			record.put(MeterData.BWZ_ACTIVE_INSULIN__U__DB_COLUMN_NAME, 				object.getNumber(MeterData.BWZ_ACTIVE_INSULIN__U__DB_COLUMN_NAME));
			record.put(MeterData.ALARM_DB_COLUMN_NAME,									object.getString(MeterData.ALARM_DB_COLUMN_NAME));
			record.put(MeterData.SENSOR_CALIBRATION_BG__MG_DL__DB_COLUMN_NAME,			object.getNumber(MeterData.SENSOR_CALIBRATION_BG__MG_DL__DB_COLUMN_NAME));
			record.put(MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME,					object.getNumber(MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME));
			record.put(MeterData.ISIG_VALUE_DB_COLUMN_NAME,								object.getNumber(MeterData.ISIG_VALUE_DB_COLUMN_NAME));
			record.put(MeterData.DAILY_INSULIN_TOTAL__U__DB_COLUMN_NAME,				object.getNumber(MeterData.DAILY_INSULIN_TOTAL__U__DB_COLUMN_NAME));
			record.put(MeterData.RAW_TYPE_DB_COLUMN_NAME,								object.getString(MeterData.RAW_TYPE_DB_COLUMN_NAME));
			record.put(MeterData.RAW_VALUES_DB_COLUMN_NAME,								object.getString(MeterData.RAW_VALUES_DB_COLUMN_NAME));
			record.put(MeterData.RAW_ID_DB_COLUMN_NAME,									object.getNumber(MeterData.RAW_ID_DB_COLUMN_NAME));
			record.put(MeterData.RAW_UPLOAD_ID_DB_COLUMN_NAME,							object.getNumber(MeterData.RAW_UPLOAD_ID_DB_COLUMN_NAME));
			record.put(MeterData.RAW_SEQ_NUM_DB_COLUMN_NAME,							object.getNumber(MeterData.RAW_SEQ_NUM_DB_COLUMN_NAME));
			record.put(MeterData.RAW_DEVICE_TYPE_DB_COLUMN_NAME,						object.getString(MeterData.RAW_DEVICE_TYPE_DB_COLUMN_NAME));

			Log.v(LOG_TAG, "Got record " + record.toString());
			results.add(record);
		}

		return results;
	}

}
