package com.nlefler.glucloser.types;

import java.util.HashMap;
import java.util.Map;

import com.nlefler.glucloser.util.database.DatabaseUtil;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Table;

@Table(MeterData.METER_DATA_DB_NAME)
public class MeterData extends Model {

    public static final String METER_DATA_DB_NAME = "meter_data";

	public static final String INDEX_DB_COLUMN_NAME									= "Index"; 
	public static final String DATE_DB_COLUMN_NAME_DB_COLUMN_NAME					= "Date";
	public static final String TIME_DB_COLUMN_NAME_DB_COLUMN_NAME					= "Time";
	public static final String TIMESTAMP_DB_COLUMN_NAME								= "Timestamp";
	public static final String NEW_DEVICE_TIME_DB_COLUMN_NAME_DB_COLUMN_NAME		= "New_Device_Time";
	public static final String BG_READING__MG_DL_COLUMN_NAME						= "BG_Reading__mg_dL_";
	public static final String LINKED_BG_METER_ID_DB_COLUMN_NAME					= "Linked_BG_Meter_ID";
	public static final String TEMP_BASAL_AMOUNT__U_H__DB_COLUMN_NAME				= "Temp_Basal_Amount__U_h_";
	public static final String TEMP_BASAL_TYPE_DB_COLUMN_NAME						= "Temp_Basal_Type";
	public static final String TEMP_BASAL_DURATION__HH_MM_SS__DB_COLUMN_NAME		= "Temp_Basal_Duration__hh_mm_ss_";
	public static final String BOLUS_TYPE_DB_COLUMN_NAME							= "Bolus_Type";
	public static final String BOLUS_VOLUME_SELECTED__U__DB_COLUMN_NAME				= "Bolus_Volume_Selected__U_";
	public static final String BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME			= "Bolus_Volume_Delivered__U_";
	public static final String PROGRAMMED_BOLUS_DURATION__HH_MM_SS__DB_COLUMN_NAME	= "Programmed_Bolus_Duration__hh_mm_ss_";
	public static final String PRIME_TYPE_DB_COLUMN_NAME 							= "Prime_Type";
	public static final String PRIME_VOLUME_DELIVERED__U__DB_COLUMN_NAME			= "Prime_Volume_Delivered__U_";
	public static final String SUSPEND_DB_COLUMN_NAME								= "Suspend";
	public static final String REWIND_DB_COLUMN_NAME								= "Rewind";
	public static final String BWZ_ESTIMATE__U__DB_COLUMN_NAME						= "BWZ_Estimate__U_";
	public static final String BWZ_TARGET_HIGH_BG__MG_DL__DB_COLUMN_NAME			= "BWZ_Target_High_BG__mg_dL_";
	public static final String BWZ_TARGET_LOW_BG__MG_DL__DB_COLUMN_NAME				= "BWZ_Target_Low_BG__mg_dL_";
	public static final String BWZ_CARB_RATIO__GRAMS__DB_COLUMN_NAME				= "BWZ_Carb_Ratio__grams_";
	public static final String BWZ_INSULIN_SENSITIVITY__MG_DL__DB_COLUMN_NAME		= "BWZ_Insulin_Sensitivity__mg_dL_";
	public static final String BWZ_CARB_INPUT__GRAMS__DB_COLUMN_NAME				= "BWZ_Carb_Input__grams_";
	public static final String BWZ_BG_INPUT__MG_DL__DB_COLUMN_NAME					= "BWZ_BG_Input__mg_dL_";
	public static final String BWZ_CORRECTION_ESTIMATE__U__DB_COLUMN_NAME			= "BWZ_Correction_Estimate__U_";
	public static final String BWZ_FOOD_ESTIMATE__U__DB_COLUMN_NAME					= "BWZ_Food_Estimate__U_";
	public static final String BWZ_ACTIVE_INSULIN__U__DB_COLUMN_NAME				= "BWZ_Active_Insulin__U_";
	public static final String ALARM_DB_COLUMN_NAME									= "Alarm";
	public static final String SENSOR_CALIBRATION_BG__MG_DL__DB_COLUMN_NAME			= "Sensor_Calibration_BG__mg_dL_";
	public static final String SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME				= "Sensor_Glucose__mg_dL_";
	public static final String ISIG_VALUE_DB_COLUMN_NAME							= "ISIG_Value";
	public static final String DAILY_INSULIN_TOTAL__U__DB_COLUMN_NAME				= "Daily_Insulin_Total__U_";
	public static final String RAW_TYPE_DB_COLUMN_NAME								= "Raw_Type";
	public static final String RAW_VALUES_DB_COLUMN_NAME							= "Raw_Values";
	public static final String RAW_ID_DB_COLUMN_NAME								= "Raw_ID";
	public static final String RAW_UPLOAD_ID_DB_COLUMN_NAME							= "Raw_Upload_ID";
	public static final String RAW_SEQ_NUM_DB_COLUMN_NAME							= "Raw_Seq_Num";
	public static final String RAW_DEVICE_TYPE_DB_COLUMN_NAME						= "Raw_Device_Type";

	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(MeterData.TIMESTAMP_DB_COLUMN_NAME, String.class);
		put(MeterData.BG_READING__MG_DL_COLUMN_NAME, Integer.class);
		put(MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME, Integer.class);
		put(MeterData.BOLUS_TYPE_DB_COLUMN_NAME, String.class);
		put(MeterData.BWZ_ESTIMATE__U__DB_COLUMN_NAME, Double.class);
		put(MeterData.BWZ_CORRECTION_ESTIMATE__U__DB_COLUMN_NAME, Double.class);
		put(MeterData.BWZ_FOOD_ESTIMATE__U__DB_COLUMN_NAME, Double.class);
		put(MeterData.BWZ_ACTIVE_INSULIN__U__DB_COLUMN_NAME, Double.class);
		put(MeterData.BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME, Double.class);
		put(MeterData.PROGRAMMED_BOLUS_DURATION__HH_MM_SS__DB_COLUMN_NAME, String.class);
		put(DatabaseUtil.PARSE_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};
}
