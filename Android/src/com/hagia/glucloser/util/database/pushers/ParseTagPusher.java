package com.hagia.glucloser.util.database.pushers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hagia.glucloser.types.Tag;
import com.hagia.glucloser.types.TagToFood;
import com.hagia.glucloser.types.TagToPlace;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.hagia.glucloser.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;

public class ParseTagPusher extends SyncPusher {
	private static final String LOG_TAG = "Pump_Parse_Tag_Pusher";

	@Override
	public Date pushRecords(List<Map<String, Object>> objects) {
		Tag tag;
		ParseObject parseObject;
		Date lastSyncDate = null;
		SQLiteDatabase db = DatabaseUtil.instance().getWritableDatabase();
		ContentValues values = new ContentValues();
		long code = 0;

		for (Map<String, Object> record : objects) {
			tag = Tag.fromMap(record);
			parseObject = tag.toParseObject();
			try {
				// Save to Parse
				parseObject.save();
				String objId = tag.id.equals(parseObject.getObjectId()) ? null : parseObject.getObjectId();
				
				// Update Tag
				values.clear();
				getCommonValuesIntoValuesForTable(values, Tables.TAG_DB_NAME, objId, false);
				
				DatabaseUtil.instance().getWritableDatabase().beginTransaction();
				code = db.update(Tables.TAG_DB_NAME, values,
						DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?", new String[] {tag.id});
				if (code == -1) {
					Log.e(LOG_TAG, "Unable to update Tag entry with new object id");
					// TODO clean up
					DatabaseUtil.instance().getWritableDatabase().endTransaction();
					continue;
				}
				
				// Update TagToFood
				values.clear();
				if (objId != null) {
					// replace temp id with parse id
					values.put(TagToFood.TAG_DB_COLUMN_KEY, objId);
					values.put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, true);
					values.put(DatabaseUtil.UPDATED_AT_COLUMN_NAME,
							DatabaseUtil.parseDateFormat.format(new Date(System.currentTimeMillis())));
					code = db.update(Tables.TAG_TO_FOOD_DB_NAME, values,
							TagToFood.TAG_DB_COLUMN_KEY + "=?", new String[] {tag.id});
					if (code == -1) {
						Log.e(LOG_TAG, "Unable to update TagToFood entry with new object id");
						// TODO clean up
						DatabaseUtil.instance().getWritableDatabase().endTransaction();
						continue;
					}
				}
				

				// Update TagToPlace
				values.clear();
				if (objId != null) {
					values.put(TagToPlace.TAG_DB_COLUMN_KEY, objId);
					values.put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, true);
					values.put(DatabaseUtil.UPDATED_AT_COLUMN_NAME,
							DatabaseUtil.parseDateFormat.format(new Date(System.currentTimeMillis())));
					code = db.update(Tables.TAG_TO_PLACE_DB_NAME, values,
							TagToPlace.TAG_DB_COLUMN_KEY + "=?", new String[] {tag.id});
					if (code == -1) {
						Log.e(LOG_TAG, "Unable to update TagToPlace entry with new object id");
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
		return super.internalGetRecordsSinceDate(Tables.TAG_DB_NAME,
				Tag.COLUMN_TYPES, sinceDate);
	}
}
