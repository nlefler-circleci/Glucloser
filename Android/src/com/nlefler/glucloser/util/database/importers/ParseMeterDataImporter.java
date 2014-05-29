package com.nlefler.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.nlefler.glucloser.model.meterdata.MeterData;

public class ParseMeterDataImporter extends SyncImporter {
	private static final String LOG_TAG = "Pump_Parse_Meter_Data_Importer";

	private static final String DATABASE_NAME = Tables.METER_DATA_DB_NAME;
	
	/**
	 * Parse Schema v1
	 * Columns:
	 * 	objectId => String
	 * 	createdAt => Date
	 * 	updatedAt => Date
	 * 	Index									INT
	 *  Date									DATE
	 *  Time									INT
	 *	Timestamp								DATE
	 *  New_Device_Time							TEXT
	 *  BG_Reading__mg_dL_						INT
	 *  Linked_BG_Meter_ID						TEXT
	 *  Temp_Basal_Amount__U_h_					FLOAT
	 *  Temp_Basal_Type							TEXT
	 *  Temp_Basal_Duration__hh_mm_ss_			TEXT
	 *  Bolus_Type								TEXT
	 *  Bolus_Volume_Selected__U_				FLOAT
	 *  Bolus_Volume_Delivered__U_				FLOAT
	 *  Programmed_Bolus_Duration__hh_mm_ss_	TEXT
	 *  Prime_Type								TEXT
	 *  Prime_Volume_Delivered__U_				FLOAT
	 *  Suspend									TEXT
	 *  Rewind									TEXT
	 *  BWZ_Estimate__U_						FLOAT
	 *  BWZ_Target_High_BG__mg_dL_				INT
	 *  BWZ_Target_Low_BG__mg_dL_				INT
	 *  BWZ_Carb_Ratio__grams_					FLOAT
	 *  BWZ_Insulin_Sensitivity__mg_dL_			INT
	 *  BWZ_Carb_Input__grams_					FLOAT
	 *  BWZ_BG_Input__mg_dL_					INT
	 *  BWZ_Correction_Estimate__U_				FLOAT
	 *  BWZ_Food_Estimate__U_					FLOAT
	 *  BWZ_Active_Insulin__U_					FLOAT
	 *  Alarm									TEXT
	 *  Sensor_Calibration_BG__mg_dL_			INT
	 *  Sensor_Glucose__mg_dL_					INT
	 *  ISIG_Value								FLOAT
	 *  Daily_Insulin_Total__U_					FLOAT
	 *  Raw_Type								TEXT
	 *  Raw_Values								TEXT
	 *  Raw_ID									FLOAT
	 *  Raw_Upload_ID							FLOAT
	 *  Raw_Seq_Num								FLOAT
	 *  Raw_Device_Type							TEXT
	 * 	ACL => Access Control List (Ignored)
	 *  dataVersion => Number
	 */
	private static final String whereClause = 
			getUpsertWhereClauseForTable(DATABASE_NAME);
	@Override
	public Date importRecords(List<Map<String, Object>> objects) {
		if (objects.isEmpty()) {
			return null;
		}

		Date lastUpdate = null;

		ContentValues values = new ContentValues();
		for (Map<String, Object> record : objects) {
			values.clear();

			getCommonValuesForTableIntoValuesFromMap(DATABASE_NAME, values, record);

			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.INDEX_DB_COLUMN_NAME),									(Integer)   record.get(MeterData.INDEX_DB_COLUMN_NAME));
			Date date = (Date)  record.get(MeterData.DATE_DB_COLUMN_NAME_DB_COLUMN_NAME);
			if (date != null) {
				values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.DATE_DB_COLUMN_NAME_DB_COLUMN_NAME),					DatabaseUtil.parseDateFormat.format(date));
			}
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.TIME_DB_COLUMN_NAME_DB_COLUMN_NAME),					(Integer)  record.get(MeterData.TIME_DB_COLUMN_NAME_DB_COLUMN_NAME));
			Date timestamp = (Date)  record.get(MeterData.TIMESTAMP_DB_COLUMN_NAME);
			if (timestamp != null) {
				values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.TIMESTAMP_DB_COLUMN_NAME),								DatabaseUtil.parseDateFormat.format(timestamp));
			}
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.NEW_DEVICE_TIME_DB_COLUMN_NAME_DB_COLUMN_NAME),			(String)  record.get(MeterData.NEW_DEVICE_TIME_DB_COLUMN_NAME_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BG_READING__MG_DL_COLUMN_NAME),									(Integer)   record.get(MeterData.BG_READING__MG_DL_COLUMN_NAME));									
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.LINKED_BG_METER_ID_DB_COLUMN_NAME),						(String)  record.get(MeterData.LINKED_BG_METER_ID_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.TEMP_BASAL_AMOUNT__U_H__DB_COLUMN_NAME),				(Double) record.get(MeterData.TEMP_BASAL_AMOUNT__U_H__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.TEMP_BASAL_TYPE_DB_COLUMN_NAME),						(String)  record.get(MeterData.TEMP_BASAL_TYPE_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.TEMP_BASAL_DURATION__HH_MM_SS__DB_COLUMN_NAME),			(String)  record.get(MeterData.TEMP_BASAL_DURATION__HH_MM_SS__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BOLUS_TYPE_DB_COLUMN_NAME),								(String)  record.get(MeterData.BOLUS_TYPE_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BOLUS_VOLUME_SELECTED__U__DB_COLUMN_NAME), 				(Double) record.get(MeterData.BOLUS_VOLUME_SELECTED__U__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME), 			(Double) record.get(MeterData.BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.PROGRAMMED_BOLUS_DURATION__HH_MM_SS__DB_COLUMN_NAME),	(String)  record.get(MeterData.PROGRAMMED_BOLUS_DURATION__HH_MM_SS__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.PRIME_TYPE_DB_COLUMN_NAME),								(String)  record.get(MeterData.PRIME_TYPE_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.PRIME_VOLUME_DELIVERED__U__DB_COLUMN_NAME),				(Double) record.get(MeterData.PRIME_VOLUME_DELIVERED__U__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.SUSPEND_DB_COLUMN_NAME),								(String)  record.get(MeterData.SUSPEND_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.REWIND_DB_COLUMN_NAME),									(String)  record.get(MeterData.REWIND_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_ESTIMATE__U__DB_COLUMN_NAME),						(Double) record.get(MeterData.BWZ_ESTIMATE__U__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_TARGET_HIGH_BG__MG_DL__DB_COLUMN_NAME),				(Integer)   record.get(MeterData.BWZ_TARGET_HIGH_BG__MG_DL__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_TARGET_LOW_BG__MG_DL__DB_COLUMN_NAME),				(Integer)   record.get(MeterData.BWZ_TARGET_LOW_BG__MG_DL__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_CARB_RATIO__GRAMS__DB_COLUMN_NAME),					(Double) record.get(MeterData.BWZ_CARB_RATIO__GRAMS__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_INSULIN_SENSITIVITY__MG_DL__DB_COLUMN_NAME),		(Integer)   record.get(MeterData.BWZ_INSULIN_SENSITIVITY__MG_DL__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_CARB_INPUT__GRAMS__DB_COLUMN_NAME),					(Double) record.get(MeterData.BWZ_CARB_INPUT__GRAMS__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_BG_INPUT__MG_DL__DB_COLUMN_NAME),					(Integer)   record.get(MeterData.BWZ_BG_INPUT__MG_DL__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_CORRECTION_ESTIMATE__U__DB_COLUMN_NAME),			(Double) record.get(MeterData.BWZ_CORRECTION_ESTIMATE__U__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_FOOD_ESTIMATE__U__DB_COLUMN_NAME),					(Double) record.get(MeterData.BWZ_FOOD_ESTIMATE__U__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.BWZ_ACTIVE_INSULIN__U__DB_COLUMN_NAME), 				(Double) record.get(MeterData.BWZ_ACTIVE_INSULIN__U__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.ALARM_DB_COLUMN_NAME),									(String)  record.get(MeterData.ALARM_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.SENSOR_CALIBRATION_BG__MG_DL__DB_COLUMN_NAME),			(Integer)   record.get(MeterData.SENSOR_CALIBRATION_BG__MG_DL__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME),					(Integer)   record.get(MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.ISIG_VALUE_DB_COLUMN_NAME),								(Double) record.get(MeterData.ISIG_VALUE_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.DAILY_INSULIN_TOTAL__U__DB_COLUMN_NAME),				(Double) record.get(MeterData.DAILY_INSULIN_TOTAL__U__DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.RAW_TYPE_DB_COLUMN_NAME),								(String)  record.get(MeterData.RAW_TYPE_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.RAW_VALUES_DB_COLUMN_NAME),								(String)  record.get(MeterData.RAW_VALUES_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.RAW_ID_DB_COLUMN_NAME),									(Double) record.get(MeterData.RAW_ID_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.RAW_UPLOAD_ID_DB_COLUMN_NAME),							(Double) record.get(MeterData.RAW_UPLOAD_ID_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.RAW_SEQ_NUM_DB_COLUMN_NAME),							(Double) record.get(MeterData.RAW_SEQ_NUM_DB_COLUMN_NAME));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME, MeterData.RAW_DEVICE_TYPE_DB_COLUMN_NAME),						(String)  record.get(MeterData.RAW_DEVICE_TYPE_DB_COLUMN_NAME));


			lastUpdate = upsertRecord(values, whereClause, record, DATABASE_NAME,
					lastUpdate, LOG_TAG);
		}

		Log.i(LOG_TAG, "Finished import of records, date of last record is " + (lastUpdate == null ? "null" : lastUpdate.toGMTString()));
		return lastUpdate;
	}

}
