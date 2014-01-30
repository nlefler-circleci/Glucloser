package com.nlefler.glucloser.util;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.util.database.Tables;
import com.nlefler.glucloser.types.Barcode;
import com.nlefler.glucloser.util.database.DatabaseUtil;

public class BarcodeUtil {
	private static final String LOG_TAG = "Pump_Barcode_Util";

	private static final String whereClauseForBarcodeForBarcodeValue =
			Barcode.BARCODE_DB_COLUMN_KEY + " = ?";
	public static Barcode barCodeForBarcodeValue(String barcodeValue) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.BARCODE_TO_FOOD_NAME_DB_NAME,
				null, 
				whereClauseForBarcodeForBarcodeValue,
				new String[] {barcodeValue}, null, null,
				null, "1");

		Barcode barCode = null;

		if (!cursor.moveToFirst()) {
			return barCode;
		}

		while (!cursor.isAfterLast()) {
			barCode = Barcode.fromMap(DatabaseUtil.getRecordFromCursor(cursor,
					Barcode.COLUMN_TYPES));
			cursor.moveToNext();
		}
		return barCode;
	}

	private static final String whereClauseForBarcodeForFoodName =
			Barcode.FOOD_NAME_DB_COLUMN_KEY + " = ?";
	public static Barcode barCodeForFoodName(String foodName) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.BARCODE_TO_FOOD_NAME_DB_NAME,
				null, 
				whereClauseForBarcodeForFoodName,
				new String[] {foodName}, null, null,
				null, "1");

		Barcode barCode = null;

		if (!cursor.moveToFirst()) {
			return barCode;
		}

		while (!cursor.isAfterLast()) {
			barCode = Barcode.fromMap(DatabaseUtil.getRecordFromCursor(cursor,
					Barcode.COLUMN_TYPES));
			cursor.moveToNext();
		}
		return barCode;
	}

	/**
	 * Save the barcode to the database.
	 * Updates the updatedAt time.
	 * 
	 * @param barCode The barcode to save
	 * @return The new id if the save was successful or -1
	 */
	public static long saveBarcode(Barcode barCode) {
		ContentValues values = new ContentValues();

		if (barCode.createdAt == null) {
			barCode.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (barCode.updatedAt == null) {
			barCode.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.BARCODE_TO_FOOD_NAME_DB_NAME,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME), barCode.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.BARCODE_TO_FOOD_NAME_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(barCode.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.FOOD_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(barCode.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.BARCODE_TO_FOOD_NAME_DB_NAME,
				Barcode.BARCODE_DB_COLUMN_KEY), barCode.barCode);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.BARCODE_TO_FOOD_NAME_DB_NAME,
				Barcode.FOOD_NAME_DB_COLUMN_KEY), barCode.foodName);

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.BARCODE_TO_FOOD_NAME_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				barCode.needsUpload);

		DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.BARCODE_TO_FOOD_NAME_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (code == -1) {
			Log.i(LOG_TAG, "Error code recieved from insert: " + code);
			Log.i(LOG_TAG, "Values are: " + values);
			DatabaseUtil.instance().getWritableDatabase().endTransaction();
		} else {
			DatabaseUtil.instance().getWritableDatabase().setTransactionSuccessful();
			DatabaseUtil.instance().getWritableDatabase().endTransaction();
		}

		return code;
	}

}
