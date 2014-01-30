package com.nlefler.glucloser.util.database.pushers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.types.Food;
import com.nlefler.glucloser.types.MealToFood;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;

public class ParseFoodPusher extends SyncPusher {
	private static final String LOG_TAG = "Pump_Parse_Food_Pusher";

	@Override
	public Date pushRecords(List<Map<String, Object>> objects) {
		Food food;
		ParseObject parseObject;
		Date lastSyncDate = null;
		SQLiteDatabase db = DatabaseUtil.instance().getWritableDatabase();
		ContentValues values = new ContentValues();
		long code = 0;

		for (Map<String, Object> record : objects) {
			food = Food.fromMap(record);
			Log.v(LOG_TAG, "Uploading food with id " + food.id);
			parseObject = food.toParseObject();
			try {
				// Save to Parse
				parseObject.save();
				String objId = food.id.equals(parseObject.getObjectId()) ?
						null : parseObject.getObjectId();

				// Update Food
				values.clear();
				getCommonValuesIntoValuesForTable(values, Tables.FOOD_DB_NAME,
						objId, false);
				
				DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
				code = db.update(Tables.FOOD_DB_NAME, values,
						DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?",
						new String[] {food.id});
				if (code == -1) {
					Log.e(LOG_TAG, "Unable to update Food entry with new object id");
					// TODO clean up
					continue;
				}
				
				// Update MealToFood
				values.clear();
				if (objId != null) {
					values.put(MealToFood.FOOD_DB_COLUMN_KEY, objId);
					values.put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, true);
					values.put(DatabaseUtil.UPDATED_AT_COLUMN_NAME,
							DatabaseUtil.parseDateFormat.format(new Date(System.currentTimeMillis())));
					
					code = db.update(Tables.MEAL_TO_FOOD_DB_NAME, values,
							MealToFood.FOOD_DB_COLUMN_KEY + "=?", new String[] {food.id});
					if (code == -1) {
						Log.e(LOG_TAG, "Unable to update MealToFood entry with new object id");
						// TODO clean up
						DatabaseUtil.instance().getWritableDatabase().endTransaction();
						continue;
					}
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
		return super.internalGetRecordsSinceDate(Tables.FOOD_DB_NAME, Food.COLUMN_TYPES, sinceDate);
	}

}
