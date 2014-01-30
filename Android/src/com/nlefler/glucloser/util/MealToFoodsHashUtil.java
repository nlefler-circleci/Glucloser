package com.nlefler.glucloser.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.types.MealToFoodsHash;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.Tables;

public class MealToFoodsHashUtil {
	private static final String LOG_TAG = "Pump_Meal_To_Foods_Hash_Util";

	// SELECT * FROM MealToFoodsHash WHERE foodsHash = ?
	private static String whereClauseForGetMealToFoodsHashes =
			MealToFoodsHash.FOODS_HASH_DB_COLUMN_KEY + "=?";
	/**
	 * Get @ref MealToFoodHash for provided food hash.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param foodsHash
	 * @return A List<MealToFoodsHash>
	 */
	public static List<MealToFoodsHash> getMealToFoodsHashes(String foodsHash) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.MEAL_TO_FOODS_HASH_DB_NAME,
				null, whereClauseForGetMealToFoodsHashes, new String[] {foodsHash}, 
				null, null, null);
		List<MealToFoodsHash> results = new ArrayList<MealToFoodsHash>();

		if (!cursor.moveToFirst()) {
			return results;
		}

		while (!cursor.isAfterLast()) {
			results.add(MealToFoodsHash.fromMap(
					DatabaseUtil.getRecordFromCursor(cursor, MealToFoodsHash.COLUMN_TYPES)));
			cursor.moveToNext();
		}

		return results;
	}

	public static long saveMealToFoodsHash(MealToFoodsHash mtfh) {
		ContentValues values = new ContentValues();

		if (mtfh.createdAt == null) {
			mtfh.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (mtfh.updatedAt == null) {
			mtfh.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOODS_HASH_DB_NAME,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME), mtfh.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOODS_HASH_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(mtfh.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOODS_HASH_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(mtfh.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOODS_HASH_DB_NAME,
				MealToFoodsHash.MEAL_DB_COLUMN_KEY), mtfh.meal.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOODS_HASH_DB_NAME,
				MealToFoodsHash.FOODS_HASH_DB_COLUMN_KEY), mtfh.foodsHash);

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.MEAL_TO_FOODS_HASH_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				mtfh.needsUpload);

		Log.v(LOG_TAG, "Saving meal to foods hash with values " + values);

		DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.MEAL_TO_FOODS_HASH_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
