package com.hagia.glucloser.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hagia.glucloser.types.Food;
import com.hagia.glucloser.types.TagToFood;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.hagia.glucloser.util.database.Tables;

public class TagToFoodUtil {
private static final String LOG_TAG = "Pump_Tag_To_Food_Util";
	
	public static boolean saveTagToFood(TagToFood tagToFood) {
		ContentValues values = new ContentValues();

		if (tagToFood.createdAt == null) {
			tagToFood.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (tagToFood.updatedAt == null) {
			tagToFood.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_FOOD_DB_NAME,
                DatabaseUtil.OBJECT_ID_COLUMN_NAME), tagToFood.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_FOOD_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(tagToFood.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_FOOD_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(tagToFood.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_FOOD_DB_NAME,
				TagToFood.TAG_DB_COLUMN_KEY), tagToFood.tag.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_FOOD_DB_NAME,
				TagToFood.FOOD_DB_COLUMN_KEY), tagToFood.food.id);
		
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_FOOD_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				tagToFood.needsUpload);

		DatabaseUtil.instance().getWritableDatabase().beginTransaction();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.TAG_TO_FOOD_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (code == -1) {
			Log.i(LOG_TAG, "Error code recieved from insert: " + code);
			Log.i(LOG_TAG, "Values are: " + values);
			DatabaseUtil.instance().getWritableDatabase().endTransaction();
			return false;
		}

		DatabaseUtil.instance().getWritableDatabase().setTransactionSuccessful();
		DatabaseUtil.instance().getWritableDatabase().endTransaction();
		return true;
	}
	
	private static final String whereClauseForGetAllTagsForFood = 
			TagToFood.FOOD_DB_COLUMN_KEY + "=?";
	public static List<TagToFood> getAllTagToFoodsForFood(Food food) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.TAG_TO_FOOD_DB_NAME, null, whereClauseForGetAllTagsForFood, 
				new String[] {food.id}, null, null, null);
		List<TagToFood> tagToFoods = new ArrayList<TagToFood>();
		
		if (!cursor.moveToFirst()) {
			return tagToFoods;
		}
		
		Map<String, Object> record;
		while (!cursor.isAfterLast()) {
			record = DatabaseUtil.getRecordFromCursor(cursor, TagToFood.COLUMN_TYPES);
			tagToFoods.add(TagToFood.fromMap(record));
			cursor.moveToNext();
		}
		
		return tagToFoods;
	}
}
