package com.hagia.glucloser.util.database;

import com.hagia.glucloser.types.Place;

public class Tables {
	// Table names
	public static final String FOOD_DB_NAME = "Food";
	public static final String MEAL_DB_NAME = "Meal";
	public static final String PLACE_DB_NAME = "Place";
	public static final String TAG_DB_NAME = "Tag";
	public static final String METER_DATA_DB_NAME = "MedtronicMinimedParadigmRevel755PumpData";

	public static final String MEAL_TO_FOOD_DB_NAME= "MealToFood";
	public static final String PLACE_TO_MEAL_DB_NAME = "PlaceToMeal";
	public static final String PLACE_TO_FOODS_HASH_DB_NAME = "PlaceToFoodsHash";
	public static final String MEAL_TO_FOODS_HASH_DB_NAME = "MealToFoodsHash";
	public static final String TAG_TO_FOOD_DB_NAME = "TagToFood";
	public static final String TAG_TO_PLACE_DB_NAME = "TagToPlace";
	public static final String BARCODE_TO_FOOD_NAME_DB_NAME = "BarCodeToFoodName";

    public static final String PLACE_TO_MEAL_DELETE_TRIGGER_NAME = "PlaceToMealDeleteTrigger";
    public static final String MEAL_DELETE_TRIGGER_NAME = "MealDeleteTrigger";
    public static final String MEAL_TO_FOOD_DELETE_TRIGGER_NAME = "MealToFoodDeleteTrigger";
    public static final String FOOD_DELETE_TRIGGER_NAME = "FoodDeleteTrigger";

	public static final String SYNC_INFO_DOWN_DB_NAME = "SyncInfoDown";
	public static final String SYNC_INFO_UP_DB_NAME = "SyncInfoUp";

	protected static String[] syncingTableNames = new String[] {
		FOOD_DB_NAME, 
		MEAL_DB_NAME, 
		PLACE_DB_NAME, 
		TAG_DB_NAME, 
		METER_DATA_DB_NAME, 
		MEAL_TO_FOOD_DB_NAME, 
		PLACE_TO_MEAL_DB_NAME,
		TAG_TO_FOOD_DB_NAME, 
		TAG_TO_PLACE_DB_NAME, 
		PLACE_TO_FOODS_HASH_DB_NAME,
		MEAL_TO_FOODS_HASH_DB_NAME,
		BARCODE_TO_FOOD_NAME_DB_NAME
	};

	// Table creation queries
	// GeoPoints as separate lat and lon floats
	protected static final String FOOD_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " + FOOD_DB_NAME + " (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, photo BLOB," +
			" name TEXT, carbs INTEGER, dateEaten TEXT, " +
			"tags TEXT, live BOOLEAN, correction BOOLEAN, createdAt TEXT, updatedAt TEXT, "+
			" needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER  DEFAULT 1);";
	protected static final String MEAL_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " + MEAL_DB_NAME + " (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, " +
			"live BOOLEAN,  dateEaten TEXT, createdAt TEXT, updatedAt TEXT, " +
			" needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER  DEFAULT 1);";
	protected static final String METER_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " + METER_DATA_DB_NAME + " (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, createdAt TEXT, updatedAt TEXT, " +
			"Index" + DatabaseUtil.RESERVED_WORD_APPEND_TOKEN + " INTEGER, Date TEXT, Time TEXT, Timestamp TEXT, New_Device_Time TEXT, " +
			"BG_Reading__mg_dL_ INTEGER, Linked_BG_Meter_ID TEXT, Temp_Basal_Amount__U_h_ FLOAT, " + 
			"Temp_Basal_Type TEXT, Temp_Basal_Duration__hh_mm_ss_ TEXT, Bolus_Type TEXT, " +
			"Bolus_Volume_Selected__U_ FLOAT, Bolus_Volume_Delivered__U_ FLOAT, " +
			"Programmed_Bolus_Duration__hh_mm_ss_ TEXT, Prime_Type TEXT, " +
			"Prime_Volume_Delivered__U_ FLOAT, Suspend TEXT, Rewind TEXT, BWZ_Estimate__U_ FLOAT, " +
			"BWZ_Target_High_BG__mg_dL_ INTEGER, BWZ_Target_Low_BG__mg_dL_ INTEGER, BWZ_Carb_Ratio__grams_ FLOAT, " +
			"BWZ_Insulin_Sensitivity__mg_dL_ INTEGER, BWZ_Carb_Input__grams_ FLOAT, BWZ_BG_Input__mg_dL_ INTEGER, " +
			"BWZ_Correction_Estimate__U_ FLOAT, BWZ_Food_Estimate__U_ FLOAT, BWZ_Active_Insulin__U_ FLOAT, " +
			"Alarm TEXT, Sensor_Calibration_BG__mg_dL_ INTEGER, Sensor_Glucose__mg_dL_ INTEGER, ISIG_Value FLOAT, " +
			"Daily_Insulin_Total__U_ FLOAT, Raw_Type TEXT, Raw_Values TEXT, Raw_ID FLOAT, Raw_Upload_ID FLOAT, " +
			"Raw_Seq_Num FLOAT, Raw_Device_Type TEXT, needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER  DEFAULT 1);";
	protected static final String PLACE_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " +  PLACE_DB_NAME + " (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, location_latitude FLOAT, location_longitude FLOAT, name TEXT, " +
			"tags TEXT, live BOOLEAN, createdAt TEXT, updatedAt TEXT, needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER  DEFAULT 1);";
	protected static final String TAG_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " + TAG_DB_NAME + " (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, name TEXT, " +
			"live BOOLEAN, createdAt TEXT, updatedAt TEXT, needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER  DEFAULT 1);";

	protected static final String MEAL_TO_FOOD_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " + MEAL_TO_FOOD_DB_NAME + " (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, meal TEXT, food TEXT UNIQUE, " +
			" createdAt TEXT, updatedAt TEXT, needsUpload BOOLEAN  DEFAULT 0, dataVersion INTEGER DEFAULT 0);";
	protected static final String PLACE_TO_MEAL_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " + PLACE_TO_MEAL_DB_NAME + " (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, place TEXT, meal TEXT UNIQUE, " +
			" createdAt TEXT, updatedAt TEXT, needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER  DEFAULT 1);";
	protected static final String TAG_TO_FOOD_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " + TAG_TO_FOOD_DB_NAME + " (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, tag TEXT, food TEXT UNIQUE, " +
			" createdAt TEXT, updatedAt TEXT, needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER  DEFAULT 1);";
	protected static final String TAG_TO_PLACE_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " + TAG_TO_PLACE_DB_NAME + " (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, tag TEXT, place TEXT, " +
			" createdAt TEXT, updatedAt TEXT, needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER  DEFAULT 1);";
	protected static final String PLACE_TO_FOODS_HASH_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " +
			PLACE_TO_FOODS_HASH_DB_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, " +
			" updatedAt TEXT, needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER DEFAULT 1, " +
			" createdAt TEXT, place TEXT, foodsHash TEXT);";
	protected static final String MEAL_TO_FOODS_HASH_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " +
			MEAL_TO_FOODS_HASH_DB_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, " +
			" updatedAt TEXT, needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER DEFAULT 1, " +
			" createdAt TEXT, meal TEXT, foodsHash TEXT);";
	protected static final String BARCODE_TO_FOOD_CREATION_SQL = "CREATE TABLE IF NOT EXISTS " +
			BARCODE_TO_FOOD_NAME_DB_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, objectId TEXT UNIQUE, " +
			" updatedAt TEXT, needsUpload BOOLEAN DEFAULT 0, dataVersion INTEGER DEFAULT 1, " +
			" createdAt TEXT, barCode TEXT UNIQUE, foodName TEXT UNIQUE);";

    protected static final String PLACE_TO_MEAL_DELETE_TRIGGER = "CREATE TRIGGER IF NOT EXISTS " +
            DatabaseUtil.DATABASE_NAME + "." + PLACE_TO_MEAL_DELETE_TRIGGER_NAME + " AFTER DELETE ON " +
            PLACE_DB_NAME + " FOR EACH ROW BEGIN (DELETE FROM " + PLACE_TO_MEAL_DB_NAME + " WHERE " +
            "'" + DatabaseUtil.OBJECT_ID_COLUMN_NAME + "'='OLD." + DatabaseUtil.OBJECT_ID_COLUMN_NAME +
            "'); END;";
    protected static final String MEAL_DELETE_TRIGGER = "CREATE TRIGGER IF NOT EXISTS " +
            DatabaseUtil.DATABASE_NAME + "." + MEAL_DELETE_TRIGGER_NAME + " AFTER DELETE ON " +
            MEAL_TO_FOOD_DB_NAME + " FOR EACH ROW BEGIN (DELETE FROM " + MEAL_DB_NAME + " WHERE " +
            "'" + DatabaseUtil.OBJECT_ID_COLUMN_NAME + "'='OLD." + DatabaseUtil.OBJECT_ID_COLUMN_NAME +
            "'); END;";
    protected static final String MEAL_TO_FOOD_DELETE_TRIGGER = "CREATE TRIGGER IF NOT EXISTS " +
        DatabaseUtil.DATABASE_NAME + "." + MEAL_TO_FOOD_DELETE_TRIGGER_NAME + " AFTER DELETE ON " +
        MEAL_DB_NAME + " FOR EACH ROW BEGIN (DELETE FROM " + MEAL_TO_FOOD_DB_NAME + " WHERE " +
        "'" + DatabaseUtil.OBJECT_ID_COLUMN_NAME + "'='OLD." + DatabaseUtil.OBJECT_ID_COLUMN_NAME +
        "'); END;";
    protected static final String FOOD_DELETE_TRIGGER = "CREATE TRIGGER IF NOT EXISTS " +
        DatabaseUtil.DATABASE_NAME + "." + FOOD_DELETE_TRIGGER_NAME + " AFTER DELETE ON " +
        MEAL_TO_FOOD_DB_NAME + " FOR EACH ROW BEGIN (DELETE FROM " + FOOD_DB_NAME + " WHERE " +
        "'" + DatabaseUtil.OBJECT_ID_COLUMN_NAME + "'='OLD." + DatabaseUtil.OBJECT_ID_COLUMN_NAME +
        "'); END;";

	private static String syncInfoDownBuild = "CREATE TABLE IF NOT EXISTS " + SYNC_INFO_DOWN_DB_NAME +
			" (ID INTEGER PRIMARY KEY AUTOINCREMENT, ";
	static {
		for (String tableName : syncingTableNames) {
			syncInfoDownBuild += tableName + " TEXT, ";
		}
		syncInfoDownBuild += "createdAt TEXT UNIQUE);";
	}
	protected static final String SYNC_INFO_DOWN_CREATION_SQL = syncInfoDownBuild;
	
	private static String syncInfoUpBuild = "CREATE TABLE IF NOT EXISTS " + SYNC_INFO_UP_DB_NAME + 
			" (ID INTEGER PRIMARY KEY AUTOINCREMENT, ";
	static {
		for (String tableName : syncingTableNames) {
			syncInfoUpBuild += tableName + " TEXT, ";
		}
		syncInfoUpBuild += "createdAt TEXT UNIQUE);";
	}
	protected static final String SYNC_INFO_UP_CREATION_SQL = syncInfoUpBuild;
	
	protected static String[] tableCreationSQLs = new String[] {
		FOOD_CREATION_SQL,
		MEAL_CREATION_SQL,
		METER_CREATION_SQL,
		PLACE_CREATION_SQL,
		TAG_CREATION_SQL,
		MEAL_TO_FOOD_CREATION_SQL,
		PLACE_TO_MEAL_CREATION_SQL,
		TAG_TO_FOOD_CREATION_SQL,
		TAG_TO_PLACE_CREATION_SQL,
		PLACE_TO_FOODS_HASH_CREATION_SQL,
		MEAL_TO_FOODS_HASH_CREATION_SQL,
		BARCODE_TO_FOOD_CREATION_SQL,
		SYNC_INFO_DOWN_CREATION_SQL,
		SYNC_INFO_UP_CREATION_SQL
	};

    protected static String[] triggerCreationSQLs = new String[] {
            PLACE_TO_MEAL_DELETE_TRIGGER,
            MEAL_TO_FOOD_DELETE_TRIGGER,
            MEAL_DELETE_TRIGGER,
            FOOD_DELETE_TRIGGER
    };
}
