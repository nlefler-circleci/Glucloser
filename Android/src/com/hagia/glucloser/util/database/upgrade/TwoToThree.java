package com.hagia.glucloser.util.database.upgrade;

import java.util.Stack;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hagia.glucloser.types.Place;
import com.hagia.glucloser.util.PlaceUtil;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.hagia.glucloser.util.database.Tables;

public class TwoToThree extends DatabaseUpgrader {

	@Override
	protected String[] getUpgradeCommands() {
		return new String[] {
				// Add ReadableAddress column to Place
				"ALTER TABLE " + Tables.PLACE_DB_NAME +
				" ADD COLUMN " + Place.READABLE_ADDRESS_COLUMN_KEY +
				" TEXT;",
		};
	}

	@Override
	public void updateData(SQLiteDatabase db) {
		// For each Place that has no address data
		// fetch it and update the record.
		
		Cursor cursor = db.query(Tables.PLACE_DB_NAME,
				new String[] {Place.READABLE_ADDRESS_COLUMN_KEY},
				Place.READABLE_ADDRESS_COLUMN_KEY + " IS NULL OR " +
						Place.READABLE_ADDRESS_COLUMN_KEY + "=''",
				null, null, null, null);
		if (!cursor.moveToFirst()) {
			return;
		}
		
		Stack<Place> places = new Stack<Place>();
		
		while (!cursor.isAfterLast()) {
			places.push(
					Place.fromMap(
							DatabaseUtil.getRecordFromCursor(cursor, Place.COLUMN_TYPES)));
		}
		cursor.close();
		
		while (!places.isEmpty()) {
			Place place = places.pop();
			PlaceUtil.updateReadableLocation(place);
			PlaceUtil.savePlaceIntoDatabaseWithTransaction(place, db, false);
		}
	}
}
