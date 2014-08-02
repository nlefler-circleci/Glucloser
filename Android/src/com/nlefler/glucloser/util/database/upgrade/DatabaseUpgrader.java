package com.nlefler.glucloser.util.database.upgrade;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import se.emilsjolander.sprinkles.Migration;

public abstract class DatabaseUpgrader extends Migration {
	private static final String LOG_TAG = "Glucloser_Database_Upgrader";
	
	/**
	 * @return An array of SQL commands to be run on upgrade
	 */
	protected String[] getUpgradeCommands() {
		return new String[] {};
	}

    @Override
    public void onPreMigrate() {

    }

    @Override
    public void doMigration(SQLiteDatabase db) {
		db.beginTransaction();

        boolean success = false;
		try {
			for (String command : getUpgradeCommands()) {
				db.execSQL(command);
			}
			db.setTransactionSuccessful();
            success = true;
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
		} finally {
			db.endTransaction();
		}

        if (success) {
            updateData(db);
        }
	}

    @Override
    public void onPostMigrate() {

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
