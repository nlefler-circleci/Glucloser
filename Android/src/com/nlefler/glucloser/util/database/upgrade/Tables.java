package com.nlefler.glucloser.util.database.upgrade;

import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.meterdata.MeterData;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.util.database.DatabaseUtil;

public class Tables {
	// Table names
//	public static final String FOOD_DB_NAME = "Food";
//	public static final String MEAL_DB_NAME = "Meal";
//	public static final String PLACE_DB_NAME = "Place";
//	public static final String TAG_DB_NAME = "Tag";
//	public static final String METER_DATA_DB_NAME = "MedtronicMinimedParadigmRevel755PumpData";
//
//	public static final String MEAL_TO_FOOD_DB_NAME= "MealToFood";
//	public static final String PLACE_TO_MEAL_DB_NAME = "PlaceToMeal";
//	public static final String PLACE_TO_FOODS_HASH_DB_NAME = "PlaceToFoodsHash";
//	public static final String MEAL_TO_FOODS_HASH_DB_NAME = "MealToFoodsHash";
//	public static final String TAG_TO_FOOD_DB_NAME = "TagToFood";
//	public static final String TAG_TO_PLACE_DB_NAME = "TagToPlace";
//	public static final String BARCODE_TO_FOOD_NAME_DB_NAME = "BarCodeToFoodName";
//
//    public static final String PLACE_TO_MEAL_DELETE_TRIGGER_NAME = "PlaceToMealDeleteTrigger";
//    public static final String MEAL_DELETE_TRIGGER_NAME = "MealDeleteTrigger";
//    public static final String MEAL_TO_FOOD_DELETE_TRIGGER_NAME = "MealToFoodDeleteTrigger";
//    public static final String FOOD_DELETE_TRIGGER_NAME = "FoodDeleteTrigger";
//
//	public static final String SYNC_INFO_DOWN_DB_NAME = "SyncInfoDown";
//	public static final String SYNC_INFO_UP_DB_NAME = "SyncInfoUp";

    protected static final String MEAL_DELETE_TRIGGER = "CREATE TRIGGER IF NOT EXISTS " +
            MEAL_DELETE_TRIGGER_NAME + " AFTER DELETE ON " +
            MEAL_TO_FOOD_DB_NAME + " FOR EACH ROW BEGIN DELETE FROM " + MEAL_DB_NAME + " WHERE " +
            "'" + DatabaseUtil.PARSE_ID_COLUMN_NAME + "'='OLD." + DatabaseUtil.PARSE_ID_COLUMN_NAME +
            "'; END;";

    protected static final String FOOD_DELETE_TRIGGER = "CREATE TRIGGER IF NOT EXISTS " +
            FOOD_DELETE_TRIGGER_NAME + " AFTER DELETE ON " +
            MEAL_TO_FOOD_DB_NAME + " FOR EACH ROW BEGIN DELETE FROM " + FOOD_DB_NAME + " WHERE " +
            "'" + DatabaseUtil.PARSE_ID_COLUMN_NAME + "'='OLD." + DatabaseUtil.PARSE_ID_COLUMN_NAME +
            "'; END;";

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
