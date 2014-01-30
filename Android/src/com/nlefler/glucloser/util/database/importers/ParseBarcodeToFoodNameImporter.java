package com.nlefler.glucloser.util.database.importers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.util.Log;

import com.nlefler.glucloser.types.Barcode;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.Tables;

public class ParseBarcodeToFoodNameImporter extends SyncImporter {
	private static final String LOG_TAG = "Pump_Parse_Barcode_To_Food_Name_Importer";

	private static final String DATABASE_NAME = Tables.BARCODE_TO_FOOD_NAME_DB_NAME;
	
	/**
	 * Parse Schema v1
	 * Columns:
	 * 	objectId => String
	 * 	barCode => String
	 * 	foodName => String
	 * 	createdAt => Date
	 * 	updatedAt => Date
	 *  dataVersion => Number
	 * 	ACL => Access Control List (Ignored)
	 */
	private static final String whereClause = getUpsertWhereClauseForTable(DATABASE_NAME);
	@Override
	public Date importRecords(List<Map<String, Object>> objects) {
		Log.i(LOG_TAG, "Starting import for " + DATABASE_NAME);
		
		if (objects.isEmpty()) {
			Log.i(LOG_TAG, "Asked to import 0 records");
			return null;
		}

		Date lastUpdate = null;

		Log.i(LOG_TAG, "Importing " + objects.size() + " records");
		ContentValues values = new ContentValues();
		
		for (Map<String, Object> barCode : objects) {
			values.clear();

			getCommonValuesForTableIntoValuesFromMap(DATABASE_NAME, values, barCode);

			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
                    Barcode.BARCODE_DB_COLUMN_KEY), (String)barCode.get(Barcode.BARCODE_DB_COLUMN_KEY));
			values.put(DatabaseUtil.localKeyForNetworkKey(DATABASE_NAME,
					Barcode.FOOD_NAME_DB_COLUMN_KEY), (String)barCode.get(Barcode.FOOD_NAME_DB_COLUMN_KEY));
			
			lastUpdate = upsertRecord(values, whereClause, barCode, DATABASE_NAME,
					lastUpdate, LOG_TAG);
		}

		Log.i(LOG_TAG, "Finished import of records, date of last record is " + 
				(lastUpdate == null ? "null" : lastUpdate.toGMTString()));
		return lastUpdate;
	}
}
