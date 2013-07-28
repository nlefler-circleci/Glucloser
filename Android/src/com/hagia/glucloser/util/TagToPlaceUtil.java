package com.hagia.glucloser.util;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hagia.glucloser.types.TagToPlace;
import com.hagia.glucloser.util.database.Tables;
import com.hagia.glucloser.util.database.DatabaseUtil;

public class TagToPlaceUtil {
private static final String LOG_TAG = "Pump_Tag_To_Place_Util";
	
	public static boolean saveTagToPlace(TagToPlace tagToPlace) {
		ContentValues values = new ContentValues();

		if (tagToPlace.createdAt == null) {
			tagToPlace.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (tagToPlace.updatedAt == null) {
			tagToPlace.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_PLACE_DB_NAME,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME), tagToPlace.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_PLACE_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(tagToPlace.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_PLACE_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(tagToPlace.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_PLACE_DB_NAME,
				TagToPlace.TAG_DB_COLUMN_KEY), tagToPlace.tag.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_PLACE_DB_NAME,
				TagToPlace.PLACE_DB_COLUMN_KEY), tagToPlace.place.id);
		
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.TAG_TO_PLACE_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				tagToPlace.needsUpload);

		DatabaseUtil.instance().getWritableDatabase().beginTransactionNonExclusive();
		long code = DatabaseUtil.instance().getWritableDatabase().insertWithOnConflict(
				Tables.TAG_TO_PLACE_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		
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
}
