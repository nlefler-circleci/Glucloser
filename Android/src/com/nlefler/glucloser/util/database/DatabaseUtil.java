package com.nlefler.glucloser.util.database;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.NetworkSyncService;
import com.nlefler.glucloser.model.meterdata.MeterData;
import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.sync.SyncDownEvent;
import com.nlefler.glucloser.model.sync.SyncUpEvent;
import com.nlefler.glucloser.util.database.fetchers.ParseFoodFetcher;
import com.nlefler.glucloser.util.database.fetchers.ParseMeterDataFetcher;
import com.nlefler.glucloser.util.database.fetchers.ParsePlaceFetcher;
import com.nlefler.glucloser.util.database.fetchers.SyncFetcher;
import com.nlefler.glucloser.util.database.importers.ParseMealImporter;
import com.nlefler.glucloser.util.database.importers.SyncImporter;
import com.nlefler.glucloser.util.database.pushers.SyncPusher;
import com.nlefler.glucloser.util.database.fetchers.ParseMealFetcher;
import com.nlefler.glucloser.util.database.importers.ParseFoodImporter;
import com.nlefler.glucloser.util.database.importers.ParseMeterDataImporter;
import com.nlefler.glucloser.util.database.importers.ParsePlaceImporter;
import com.nlefler.glucloser.util.database.pushers.NoOpPusher;
import com.nlefler.glucloser.util.database.pushers.ParseFoodPusher;
import com.nlefler.glucloser.util.database.pushers.ParseMealPusher;
import com.nlefler.glucloser.util.database.pushers.ParsePlacePusher;
import com.nlefler.glucloser.util.database.upgrade.DatabaseUpgrader;
import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.nlefler.glucloser.util.database.upgrade.ZeroToOne;

import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.Sprinkles;
import se.emilsjolander.sprinkles.annotations.Table;


public class DatabaseUtil {
	private static final String LOG_TAG = "Glucloser_Database_Util";

    public static String DATABASE_NAME = "GLUCLOSER_DB";
	private static int DATABASE_VERSION = 1;
    private static Sprinkles sprinklesInstance = null;

    public static final String ID_COLUMN_NAME = "id";
    public static final String GLUCLOSER_ID_COLUMN_NAME = "glucloserId";
	public static final String PARSE_ID_COLUMN_NAME = "parseId";
	public static final String UPDATED_AT_COLUMN_NAME = "updatedAt";
	public static final String CREATED_AT_COLUMN_NAME = "createdAt";
	public static final String NEEDS_UPLOAD_COLUMN_NAME = "needsUpload";
	public static final String DATA_VERSION_COLUMN_NAME = "dataVersion";
	public static SimpleDateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss.SSS'Z'");

	private static DatabaseUtil instance;
	private static AtomicBoolean okToContinueSyncing = new AtomicBoolean(true);
	private static AtomicBoolean needsSync = new AtomicBoolean(false);

	private static DatabaseUpgrader[] dbUpgraders = new DatabaseUpgrader[] {
		new ZeroToOne()
	};

	private DatabaseUtil(Context context) {
        // TODO: Enable foreign key constraints and WAL
        sprinklesInstance = Sprinkles.init(context, DATABASE_NAME, DATABASE_VERSION);
        for (int i = 0; i < DATABASE_VERSION && i < dbUpgraders.length; i++) {
            sprinklesInstance.addMigration(dbUpgraders[i]);
        }
	}

	public static void initialize(Context context) {
		instance = new DatabaseUtil(context);
	}

	public static DatabaseUtil instance() {
		return instance;
	}

    public static String tableNameForModel(Class modelClass) {
        Annotation tableAnnotation = modelClass.getAnnotation(Table.class);
        if (tableAnnotation != null) {
            return tableAnnotation.toString();
        }
        return null;
    }

	public static void setNeedsSync() {
		needsSync.set(true);
	}
	
	public static void syncIfNeeded(Context context) {
		if (needsSync.get()) {
			instance().startNetworkSyncServiceUsingContext(context);
		}
	}
	// TODO return last sync times
	public void startNetworkSyncServiceUsingContext(Context context) {
		Intent syncIntent = new Intent(context, NetworkSyncService.class);
		context.startService(syncIntent);
	}

	public synchronized void syncWithNetwork() {
		Map<String, Date> ret[];

		okToContinueSyncing.set(true);

		Log.i(LOG_TAG, "Starting network sync");

		syncWithParse();

		Log.i(LOG_TAG, "Network sync complete");

		needsSync.set(false);
	}

	public static void stopSync() {
		okToContinueSyncing.set(false);
	}

	/**
	 * Syncs the local database with Parse.
	 * Should be run in a thread.
	 */
	private void syncWithParse() {
		Log.i(LOG_TAG, "Starting sync with Parse");

        SyncDownEvent lastDownSyncTimes = getLastSyncDownTime();
        SyncUpEvent lastUpSyncTimes = getLastUpSyncTime();

		syncHelper(Food.class,
				new ParseFoodFetcher(), new ParseFoodImporter(), new ParseFoodPusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		syncHelper(Meal.class,
				new ParseMealFetcher(), new ParseMealImporter(), new ParseMealPusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		syncHelper(Place.class,
                new ParsePlaceFetcher(), new ParsePlaceImporter(), new ParsePlacePusher(),
                lastDownSyncTimes, lastUpSyncTimes);

		// Doing this last because it's slow (42.5k records and counting)
		syncHelper(MeterData.class,
				new ParseMeterDataFetcher(), new ParseMeterDataImporter(), new NoOpPusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		Log.i(LOG_TAG, "Sync with Parse complete");

        updateLastSyncTimes(lastDownSyncTimes, lastUpSyncTimes);
	}

	private void syncHelper(Class modelClass, SyncFetcher fetcher,
			SyncImporter importer, SyncPusher pusher,
			SyncDownEvent downSyncTimes, SyncUpEvent upSyncTimes) {
		Date[] tableSyncTimes = doSync(
				fetcher,
                downSyncTimes.getTimeForModel(modelClass),
				importer,
				pusher,
                upSyncTimes.getTimeForModel(modelClass)
				);

        downSyncTimes.setTimeForModel(tableSyncTimes[0], modelClass);
        upSyncTimes.setTimeForModel(tableSyncTimes[1], modelClass);

		updateLastSyncTimes(downSyncTimes, upSyncTimes);

		Log.v(LOG_TAG, "Completed sync for table " + modelClass);
	}

	private SyncUpEvent getLastUpSyncTime() {
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(SyncUpEvent.class) +
                " ORDERED BY " + DatabaseUtil.CREATED_AT_COLUMN_NAME + " DESC";
        SyncUpEvent upEvent = Query.one(SyncUpEvent.class, select).get();

        return upEvent;
	}

    private SyncDownEvent getLastSyncDownTime() {
         String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(SyncDownEvent.class) +
                " ORDERED BY " + DatabaseUtil.CREATED_AT_COLUMN_NAME + " DESC";
        SyncDownEvent downEvent = Query.one(SyncDownEvent.class, select).get();

        return downEvent;
    }

	private void updateLastSyncTimes(SyncDownEvent lastDownSyncTimes, SyncUpEvent lastUpSyncTimes) {
		Log.v(LOG_TAG, "Updating last sync times with down: " + lastDownSyncTimes + " up: " + lastUpSyncTimes);

		if (lastDownSyncTimes == null || lastUpSyncTimes == null) {
			Log.w(LOG_TAG, "No sync times provided, finished");
			return;
		}

        lastDownSyncTimes.updateFieldsAndSave();
        lastUpSyncTimes.updateFieldsAndSave();

		Log.v(LOG_TAG, "Finished updating last sync times");
	}

	private Date[] doSync(SyncFetcher fetcher, Date lastDownSyncDate, SyncImporter importer,
			SyncPusher pusher, Date lastUpSyncDate) {
		Log.v(LOG_TAG, "Performing sync for table");

		// Syncing up first so we don't duplicate records we just downloaded
		lastUpSyncDate = doSyncUp(pusher, lastUpSyncDate);
		lastDownSyncDate = doSyncDown(fetcher, lastDownSyncDate, importer);

		Log.v(LOG_TAG, "Finished sync for table, last sync down date is " + 
				(lastDownSyncDate == null ? "null" : lastDownSyncDate.toGMTString()) +
				" last up sync date is " + (lastUpSyncDate == null ? "null" : lastUpSyncDate.toGMTString()));

		return new Date[] {lastDownSyncDate, lastUpSyncDate};
	}

	private Date doSyncDown(SyncFetcher fetcher, Date lastSyncDate, SyncImporter importer) {
		while (okToContinueSyncing.get() && fetcher.hasMoreRecords()) {
			Log.v(LOG_TAG, "Syncing records since " + lastSyncDate);

			List<Map<String, Object>> records = fetcher.fetchRecords(lastSyncDate);
			lastSyncDate = importer.importRecords(records);

			Log.v(LOG_TAG, "Did intermediate sync for table, last sync date is " + (lastSyncDate == null ? "null" : lastSyncDate.toGMTString()));
		}

		return lastSyncDate;
	}

	private Date doSyncUp(SyncPusher pusher, Date lastSyncDate) {
		Date newDate = pusher.doSyncSinceDate(lastSyncDate);
		return newDate;
	}
}
