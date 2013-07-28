package com.hagia.glucloser.util.database;

import com.hagia.glucloser.types.Barcode;
import com.hagia.glucloser.types.Food;
import com.hagia.glucloser.types.MeterData;

public class Indexes {
	// Index names
	protected static final String INDEX_FOOD_NAME = "IdxFoodName";
	protected static final String INDEX_METER_DATA_TIMESTAMP = "IdxTimeStamp";
	protected static final String INDEX_BARCODE = "IdxBarCode";

	protected static final String[] indexNames = new String[] {
		INDEX_FOOD_NAME,
		INDEX_METER_DATA_TIMESTAMP,
		INDEX_BARCODE
	};
	
	// Index creation queries
	protected static final String INDEX_FOOD_NAME_CREATION_SQL = "CREATE INDEX IF NOT EXISTS " +
			INDEX_FOOD_NAME + " ON " + Tables.FOOD_DB_NAME + " (" + Food.NAME_DB_COLUMN_KEY + " ASC);";
	protected static final String INDEX_METER_DATA_TIMESTAMP_CREATION_SQL = "CREATE INDEX IF NOT EXISTS " +
			INDEX_METER_DATA_TIMESTAMP + " ON " + Tables.METER_DATA_DB_NAME + 
			" (" + MeterData.TIMESTAMP_DB_COLUMN_NAME + " ASC);";
	protected static final String INDEX_BARCODE_CREATION_SQL = "CREATE INDEX IF NOT EXISTS " +
			INDEX_BARCODE + " ON " + Tables.BARCODE_TO_FOOD_NAME_DB_NAME + " (" + Barcode.BARCODE_DB_COLUMN_KEY + ");";
	
	protected static final String[] indexCreationSQLs = new String[] {
		INDEX_FOOD_NAME_CREATION_SQL,
		INDEX_METER_DATA_TIMESTAMP_CREATION_SQL,
		INDEX_BARCODE_CREATION_SQL
	};
}
