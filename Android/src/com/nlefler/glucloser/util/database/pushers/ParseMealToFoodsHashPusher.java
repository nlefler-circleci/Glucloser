package com.nlefler.glucloser.util.database.pushers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.types.MealToFoodsHash;
import com.nlefler.glucloser.util.database.Tables;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;

public class ParseMealToFoodsHashPusher extends SyncPusher {
	private static final String LOG_TAG = "Pump_Parse_Meal_To_Foods_Hash_Pusher";

	@Override
	public Date pushRecords(List<Map<String, Object>> objects) {
		MealToFoodsHash mealToFoodsHash;
		ParseObject parseObject;
		Date lastSyncDate = null;
		SQLiteDatabase db = DatabaseUtil.instance().getWritableDatabase();
		ContentValues values = new ContentValues();
		long code = 0;

		for (Map<String, Object> record : objects) {
			mealToFoodsHash = MealToFoodsHash.fromMap(record);
			parseObject = mealToFoodsHash.toParseObject();
			try {
				// Save to Parse
				parseObject.save();
				String objId = mealToFoodsHash.id.equals(parseObject.getObjectId()) ? null : parseObject.getObjectId();
				
				values.clear();
				getCommonValuesIntoValuesForTable(values, Tables.MEAL_TO_FOODS_HASH_DB_NAME,
						objId, false);
				Log.v(LOG_TAG, "Uploading MealToFoodsHash to Parse: " + values);
				
				DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
				code = db.update(Tables.MEAL_TO_FOODS_HASH_DB_NAME, values,
						DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?", new String[] {mealToFoodsHash.id});
				if (code == -1) {
					Log.e(LOG_TAG, "Unable to update MealToFoodsHash entry with new object id");
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
		return super.internalGetRecordsSinceDate(Tables.MEAL_TO_FOODS_HASH_DB_NAME,
				MealToFoodsHash.COLUMN_TYPES, sinceDate);
	}
}
