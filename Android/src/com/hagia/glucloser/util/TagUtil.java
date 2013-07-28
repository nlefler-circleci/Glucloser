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

import com.hagia.glucloser.types.Tag;
import com.hagia.glucloser.types.TagToFood;
import com.hagia.glucloser.types.TagToPlace;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.hagia.glucloser.util.database.Tables;


public class TagUtil {
	private static final String LOG_TAG = "Pump_Tag_Util";

	private static String[] columnsForAllTagNames = 
			new String[] {Tag.NAME_DB_COLUMN_KEY};
	/**
	 * Get all tag names.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @return A List<String> of all tag names
	 */
	public static List<String> getAllTagNames() {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				true, Tables.TAG_DB_NAME, columnsForAllTagNames,
				null, null, Tag.NAME_DB_COLUMN_KEY, 
				null, Tag.NAME_DB_COLUMN_KEY + " ASC", null);

		List<String> names = new ArrayList<String>();
		if (!cursor.moveToFirst()) {
			return names;
		}

		int columnIndex = cursor.getColumnIndex(Tag.NAME_DB_COLUMN_KEY);
		while (!cursor.isAfterLast()) {
			names.add(cursor.getString(columnIndex));
			cursor.moveToNext();
		}

		return names;
	}

	/**
	 * Get all tag names that start with the provided string.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param start The name prefix
	 * @return A List<String> of tag names starting with the given prefix
	 */
	public static List<String> getAllTagNamesStartingWith(String start) {
		Log.i(LOG_TAG, "Getting tag names starting with " + start);


		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				true, Tables.TAG_DB_NAME, columnsForAllTagNames,
				"lower(" + Tag.NAME_DB_COLUMN_KEY + ") LIKE lower(?)", new String[] {start + "%"},
				Tag.NAME_DB_COLUMN_KEY, null, Tag.NAME_DB_COLUMN_KEY + " ASC", null);

		List<String> names = new ArrayList<String>();
		if (!cursor.moveToFirst()) {
			return names;
		}

		int columnIndex = cursor.getColumnIndex(Tag.NAME_DB_COLUMN_KEY);
		while (!cursor.isAfterLast()) {
			names.add(cursor.getString(columnIndex));
			cursor.moveToNext();
		}

		return names;
	}

	/**
	 * Get all tags with names that contain the provided string.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param name The substring to search for
	 * @return A List<String> of tags that match
	 */
	public static List<Tag> getAllTagsWithNameContaining(String name) {
		Log.i(LOG_TAG, "Getting tags with name containing " + name);

		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				true, Tables.TAG_DB_NAME, null,
				"lower(" + Tag.NAME_DB_COLUMN_KEY + ") LIKE lower(?)", new String[] {"%" + name + "%"}, 
				null, null, Tag.NAME_DB_COLUMN_KEY + " ASC", null);

		List<Tag> tags = new ArrayList<Tag>();

		if (!cursor.moveToFirst()) {
			return tags;
		}

		while (!cursor.isAfterLast()) {
			tags.add(Tag.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Tag.COLUMN_TYPES)));
			cursor.moveToNext();
		}

		return tags;
	}

	/**
	 * Get the tag from the database with the given id.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param id
	 * @return A @ref Tag representing the tag in the database,
	 * or null if no tag was found with the given id.
	 */
	public static Tag getTagById(String id) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.TAG_DB_NAME, null,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?", 
				new String[] {id}, null, null, null, "1");
		Tag tag = null;

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "No tags with id " + id + " in local db");
			return tag;
		}

		while (!cursor.isAfterLast()) {
			tag = Tag.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Tag.COLUMN_TYPES));
			cursor.moveToNext();
		}

		return tag;
	}

	public static boolean saveTag(Tag tag) {
		ContentValues values = new ContentValues();

		if (tag.createdAt == null) {
			tag.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (tag.updatedAt == null) {
			tag.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_DB_NAME,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME), tag.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(tag.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(tag.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_DB_NAME,
				Tag.NAME_DB_COLUMN_KEY), tag.name);

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				tag.needsUpload);

		DatabaseUtil.instance().getWritableDatabase().beginTransaction();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.TAG_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (code != -1) {

			// Link Foods
			for (TagToFood tagToFood : tag.foods) {
				if (!TagToFoodUtil.saveTagToFood(tagToFood)) {
					Log.e(LOG_TAG, "Unable to save tag to food");
					DatabaseUtil.instance().getWritableDatabase().endTransaction();
					return false;
				}
			}


			// Link Places
			for (TagToPlace tagToPlace : tag.places) {
				if (!TagToPlaceUtil.saveTagToPlace(tagToPlace)) {
					Log.e(LOG_TAG, "Unable to save tag to place");
					DatabaseUtil.instance().getWritableDatabase().endTransaction();
					return false;
				}
			}

			DatabaseUtil.instance().getWritableDatabase().setTransactionSuccessful();
			DatabaseUtil.instance().getWritableDatabase().endTransaction();
			return true;
		} else {
			Log.i(LOG_TAG, "Error code recieved from insert: " + code);
			Log.i(LOG_TAG, "Values are: " + values);
			DatabaseUtil.instance().getWritableDatabase().endTransaction();
			return false;
		}
	}

	/**
	 * Get the tag with the given name.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param name
	 * @return The @ref Tag, or null if no tag with the given name
	 * exists.
	 */
	public static Tag getTagForName(String name) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				true, Tables.TAG_DB_NAME, null,
				"lower(" + Tag.NAME_DB_COLUMN_KEY + ") = lower(?)", new String[] {name}, 
				Tag.NAME_DB_COLUMN_KEY,
				null, Tag.NAME_DB_COLUMN_KEY + " ASC", null);
		Tag tag = null;
		if (!cursor.moveToFirst()) {
			return tag;
		}

		Map<String, Object> record = null;
		while (!cursor.isAfterLast()) {
			record = DatabaseUtil.getRecordFromCursor(cursor, Tag.COLUMN_TYPES);
			cursor.moveToNext();
		}

		if (record != null) {
			tag = Tag.fromMap(record);
		}

		return tag;
	}
}
