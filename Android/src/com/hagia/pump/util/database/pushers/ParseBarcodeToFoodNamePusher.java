package com.hagia.pump.util.database.pushers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hagia.pump.types.Barcode;
import com.hagia.pump.util.database.DatabaseUtil;
import com.hagia.pump.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;

public class ParseBarcodeToFoodNamePusher extends SyncPusher {
	private static final String LOG_TAG = "Pump_Parse_Barcode_To_Food_Name_Pusher";

	@Override
	public Date pushRecords(List<Map<String, Object>> objects) {
		Barcode barCode;
		ParseObject parseObject;
		Date lastSyncDate = null;
		SQLiteDatabase db = DatabaseUtil.instance().getWritableDatabase();
		ContentValues values = new ContentValues();
		long code = 0;

		for (Map<String, Object> record : objects) {
			barCode = Barcode.fromMap(record);
			Log.v(LOG_TAG, "Uploading BarcodeToFood with id " + barCode.id);
			parseObject = barCode.toParseObject();
			try {
				// Save to Parse
				parseObject.save();
				String objId = barCode.id.equals(parseObject.getObjectId()) ?
						null : parseObject.getObjectId();

				// Update Barcode
				values.clear();
				getCommonValuesIntoValuesForTable(values, Tables.BARCODE_TO_FOOD_NAME_DB_NAME,
						objId, false);
				
				DatabaseUtil.instance().getWritableDatabase().beginTransaction();
				code = db.update(Tables.BARCODE_TO_FOOD_NAME_DB_NAME, values,
						DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?",
						new String[] {barCode.id});
				if (code == -1) {
					Log.e(LOG_TAG, "Unable to update BarcodeToFood entry with new object id");
					// TODO clean up
					DatabaseUtil.instance().getWritableDatabase().endTransaction();
					continue;
				}
				
				DatabaseUtil.instance().getWritableDatabase().setTransactionSuccessful();
				DatabaseUtil.instance().getWritableDatabase().endTransaction();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lastSyncDate = parseObject.getUpdatedAt();
		}
		return lastSyncDate;
	}

	@Override
	public List<Map<String, Object>> getRecordsSinceDate(Date sinceDate) {
		return super.internalGetRecordsSinceDate(Tables.BARCODE_TO_FOOD_NAME_DB_NAME,
				Barcode.COLUMN_TYPES, sinceDate);
	}
}
