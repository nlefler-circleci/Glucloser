package com.nlefler.glucloser.util.database.upgrade;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class DatabaseUpgrader {
	private static final String LOG_TAG = "Pump_Database_Upgrader";
	
	/**
	 * @return An array of SQL commands to be run on upgrade
	 */
	protected String[] getUpgradeCommands() {
		return new String[] {};
	}

	/**
	 * Runs the SQL commands returned from @ref getUpgradeCommands().
	 * @param db The database to upgrade
	 * @return success
	 */
	public boolean upgrade(SQLiteDatabase db) {
		db.beginTransaction();

		try {
			for (String command : getUpgradeCommands()) {
				db.execSQL(command);
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
			return false;
		} finally {
			db.endTransaction();
		}

		return true;
	}
	
	/**
	 * Run after @ref upgrade(). Subclasses may override this
	 * to perform work to update the data contained by the database
	 * 
	 * @note This method will be run on the main thread. Implementations
	 * should tread lightly.
	 * 
	 * @param db The database to update
	 */
	public void updateData(SQLiteDatabase db) {
	}
}
