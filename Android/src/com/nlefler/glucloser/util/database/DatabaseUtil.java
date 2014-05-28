package com.nlefler.glucloser.util.database;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.NetworkSyncService;
import com.nlefler.glucloser.util.database.fetchers.ParseFoodFetcher;
import com.nlefler.glucloser.util.database.fetchers.ParseMeterDataFetcher;
import com.nlefler.glucloser.util.database.fetchers.ParsePlaceFetcher;
import com.nlefler.glucloser.util.database.fetchers.SyncFetcher;
import com.nlefler.glucloser.util.database.importers.ParseMealImporter;
import com.nlefler.glucloser.util.database.importers.ParseMealToFoodImporter;
import com.nlefler.glucloser.util.database.importers.ParsePlaceToFoodsHashImporter;
import com.nlefler.glucloser.util.database.importers.ParsePlaceToMealImporter;
import com.nlefler.glucloser.util.database.importers.SyncImporter;
import com.nlefler.glucloser.util.database.pushers.ParseMealToFoodPusher;
import com.nlefler.glucloser.util.database.pushers.ParsePlaceToFoodsHashPusher;
import com.nlefler.glucloser.util.database.pushers.SyncPusher;
import com.nlefler.glucloser.util.database.fetchers.ParseMealFetcher;
import com.nlefler.glucloser.util.database.fetchers.ParseMealToFoodFetcher;
import com.nlefler.glucloser.util.database.fetchers.ParsePlaceToFoodsHashFetcher;
import com.nlefler.glucloser.util.database.fetchers.ParsePlaceToMealFetcher;
import com.nlefler.glucloser.util.database.importers.ParseFoodImporter;
import com.nlefler.glucloser.util.database.importers.ParseMeterDataImporter;
import com.nlefler.glucloser.util.database.importers.ParsePlaceImporter;
import com.nlefler.glucloser.util.database.pushers.NoOpPusher;
import com.nlefler.glucloser.util.database.pushers.ParseFoodPusher;
import com.nlefler.glucloser.util.database.pushers.ParseMealPusher;
import com.nlefler.glucloser.util.database.pushers.ParsePlacePusher;
import com.nlefler.glucloser.util.database.pushers.ParsePlaceToMealPusher;
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

    // Some columns used by Medtronic are reserved by parse.
    // Append a string to make them unique.
    // TODO: Refactor
	public static final String RESERVED_WORD_APPEND_TOKEN = "_GLUCLOSER_";
    // Mapping of DB name to (Network Column Name to Local Column Name)
	private static final Map<String, Map<String, String>> localKeyToNetworkKeyMap =
            new HashMap<String, Map<String, String>>() {{
		put(Tables.METER_DATA_DB_NAME, new HashMap<String, String>() {{
            put("Index" + RESERVED_WORD_APPEND_TOKEN, "Index"); }});
	}};
    // Reverse of localKeyToNetworkKeyMap
    // TODO: Use reverse of above map
	private static final Map<String, Map<String, String>> networkKeyToLocalKeyMap =
            new HashMap<String, Map<String, String>>() {{
		put(Tables.METER_DATA_DB_NAME, new HashMap<String, String>() {{
            put("Index", "Index" + RESERVED_WORD_APPEND_TOKEN); }});
	}};

    public static final String ID_COLUMN_NAME = "id";
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

	public synchronized Map<String, Date>[] syncWithNetwork() {
		Map<String, Date> ret[];

		okToContinueSyncing.set(true);

		Log.i(LOG_TAG, "Starting network sync");

		ret = syncWithParse();

		Log.i(LOG_TAG, "Network sync complete");

		needsSync.set(false);
		return ret;
	}

	public static void stopSync() {
		okToContinueSyncing.set(false);
	}

	/**
	 * Syncs the local database with Parse.
	 * Should be run in a thread.
	 */
	private Map<String, Date>[] syncWithParse() {
		Log.i(LOG_TAG, "Starting sync with Parse");

		Map<String, Date> lastSyncTimes[];
		try {
			lastSyncTimes = getLastSyncTimes();
		} catch (java.text.ParseException e) {
			Log.e(LOG_TAG, "Unable to get last sync times");
			e.printStackTrace();
			return new Map[] {new HashMap<String, Date>(), new HashMap<String, Date>()};
		}
		Map<String, Date> lastDownSyncTimes = lastSyncTimes[0];
		Map<String, Date> lastUpSyncTimes = lastSyncTimes[1];

		syncHelper(Tables.FOOD_DB_NAME,
				new ParseFoodFetcher(), new ParseFoodImporter(), new ParseFoodPusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		syncHelper(Tables.MEAL_DB_NAME,
				new ParseMealFetcher(), new ParseMealImporter(), new ParseMealPusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		syncHelper(Tables.PLACE_DB_NAME,
				new ParsePlaceFetcher(), new ParsePlaceImporter(), new ParsePlacePusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		syncHelper(Tables.MEAL_TO_FOOD_DB_NAME,
				new ParseMealToFoodFetcher(), new ParseMealToFoodImporter(), new ParseMealToFoodPusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		syncHelper(Tables.PLACE_TO_MEAL_DB_NAME,
				new ParsePlaceToMealFetcher(), new ParsePlaceToMealImporter(), new ParsePlaceToMealPusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		syncHelper(Tables.PLACE_TO_FOODS_HASH_DB_NAME,
				new ParsePlaceToFoodsHashFetcher(), new ParsePlaceToFoodsHashImporter(), new ParsePlaceToFoodsHashPusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		// Doing this last because it's slow (42.5k records and counting)
		syncHelper(Tables.METER_DATA_DB_NAME,
				new ParseMeterDataFetcher(), new ParseMeterDataImporter(), new NoOpPusher(),
				lastDownSyncTimes, lastUpSyncTimes);

		Log.i(LOG_TAG, "Sync with Parse complete");

		lastSyncTimes[0] = lastDownSyncTimes;
		lastSyncTimes[1] = lastUpSyncTimes;

		return lastSyncTimes;
	}

	private void syncHelper(String tableName, SyncFetcher fetcher,
			SyncImporter importer, SyncPusher pusher,
			Map<String, Date> downSyncTimes, Map<String, Date> upSyncTimes) {
		Date[] tableSyncTimes = doSync(
				fetcher,
				downSyncTimes.get(tableName),
				importer,
				pusher, 
				upSyncTimes.get(tableName),
				generatePartialSyncCallbackForTable(
						tableName, downSyncTimes, upSyncTimes)
				);

		downSyncTimes.put(tableName, tableSyncTimes[0]);
		upSyncTimes.put(tableName, tableSyncTimes[1]);

		updateLastSyncTimes(downSyncTimes, upSyncTimes);

		Log.v(LOG_TAG, "Completed sync for table " + tableName);
	}

	private Map<String, Date>[] getLastSyncTimes() throws java.text.ParseException {
		Log.v(LOG_TAG, "Getting last sync times");

		Map<String, Date> lastDown = new HashMap<String, Date>();
		Map<String, Date> lastUp = new HashMap<String, Date>();

		for (String tableName : Tables.syncingTableNames) {
			lastDown.put(tableName, null);
			lastUp.put(tableName, null);
		}

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor resultCursor = db.query(Tables.SYNC_INFO_DOWN_DB_NAME, 
				Tables.syncingTableNames, null, null, null, null, CREATED_AT_COLUMN_NAME + " DESC", "1");

		if (resultCursor.moveToFirst()) {
			Date syncTime = null;

			for (int i = 0; i < resultCursor.getColumnCount(); ++i) {
				String value = resultCursor.getString(i);
				if (value == null) {
					continue;
				}
				syncTime = parseDateFormat.parse(value);

				Log.v(LOG_TAG, "Got last sync time for " + resultCursor.getColumnName(i) + " " + syncTime.toGMTString());

				lastDown.put(resultCursor.getColumnName(i), syncTime);
			}
		}

		resultCursor.close();
		resultCursor = db.query(Tables.SYNC_INFO_UP_DB_NAME, 
				Tables.syncingTableNames, null, null, null, null, CREATED_AT_COLUMN_NAME + " DESC", "1");

		if (resultCursor.moveToFirst()) {
			Date syncTime = null;

			for (int i = 0; i < resultCursor.getColumnCount(); ++i) {
				String value = resultCursor.getString(i);
				if (value == null) {
					continue;
				}
				syncTime = parseDateFormat.parse(value);

				Log.v(LOG_TAG, "Got last sync time for " + resultCursor.getColumnName(i) + " " + syncTime.toGMTString());

				// TODO Why shouldn't all records marked as needUpload be uploaded?
				//lastUp.put(resultCursor.getColumnName(i), syncTime);
			}
		}

		Log.v(LOG_TAG, "Finished getting last sync times");

		return new Map[] {lastDown, lastUp};
	}

	private void updateLastSyncTimes(Map<String, Date> lastDownSyncTimes, 
			Map<String, Date> lastUpSyncTimes) {
		Log.v(LOG_TAG, "Updating last sync times with down: " + lastDownSyncTimes + " up: " + lastUpSyncTimes);

		if ((lastDownSyncTimes == null || lastDownSyncTimes.isEmpty()) &&
				(lastUpSyncTimes == null || lastUpSyncTimes.isEmpty())) {
			Log.w(LOG_TAG, "No sync times provided, finished");
			return;
		}

		ContentValues values = new ContentValues();
		for (Entry<String, Date> entry : lastDownSyncTimes.entrySet()) {
			if (entry.getValue() != null) {
				values.put(entry.getKey(), parseDateFormat.format(entry.getValue()));
			}
		}
		values.put(CREATED_AT_COLUMN_NAME, parseDateFormat.format(new Date()));

		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();
		long code = db.insert(Tables.SYNC_INFO_DOWN_DB_NAME, null, values);
		if (code == -1) {
			db.endTransaction();
			Log.e(LOG_TAG, "Error updating down sync times, code " + code);
			return;
		}

		values.clear();
		for (Entry<String, Date> entry : lastUpSyncTimes.entrySet()) {
			if (entry.getValue() != null) {
				values.put(entry.getKey(), parseDateFormat.format(entry.getValue()));
			}
		}
		values.put(CREATED_AT_COLUMN_NAME, parseDateFormat.format(new Date()));

		code = db.insert(Tables.SYNC_INFO_UP_DB_NAME, null, values);
		if (code == -1) {
			db.endTransaction();
			Log.e(LOG_TAG, "Error updating up sync times, code " + code);
			return;
		}

		db.setTransactionSuccessful();
		db.endTransaction();

		Log.v(LOG_TAG, "Finished updating last sync times");
	}

	private void updateLastUpSyncTimeForTable(String tableName, Date lastSyncTime,
			Map<String, Date> knownTimes) {
		updateLastSyncTimeForTableWithDirection(lastSyncTime, tableName, "up", knownTimes);
	}

	private void updateLastDownSyncTimeForTable(String tableName, Date lastSyncTime,
			Map<String, Date> knownTimes) {
		updateLastSyncTimeForTableWithDirection(lastSyncTime, tableName, "down", knownTimes);
	}

	private void updateLastSyncTimeForTableWithDirection(Date lastSyncTime, String tableName,
			String direction, Map<String, Date> lastKnownTimes) {
		if (!direction.equals("up") && ! direction.equals("down")) {
			Log.e(LOG_TAG, "Sync direction should be up or down, was " + direction);
			return;
		}
		String logDirection = direction.equals("up") ? "up" : "down";
		String dbName = direction.equals("up") ? Tables.SYNC_INFO_UP_DB_NAME : Tables.SYNC_INFO_DOWN_DB_NAME;

		Log.v(LOG_TAG, "Updating last " + logDirection + " sync time with " + lastSyncTime + " for " +
				tableName);

		if (lastSyncTime == null) {
			Log.w(LOG_TAG, "No sync times provided, finished");
			return;
		}

		ContentValues values = new ContentValues();
		// Fill in last known times for all columns
		for (Entry<String, Date> entry : lastKnownTimes.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			values.put(entry.getKey(), parseDateFormat.format(entry.getValue()));
		}
		// Overwrite the column we care about with the new time
		values.put(tableName, parseDateFormat.format(lastSyncTime));
		values.put(CREATED_AT_COLUMN_NAME, parseDateFormat.format(new Date()));

		try {
			this.getWritableDatabase().insertOrThrow(dbName, null, values);
		} catch (android.database.SQLException e) {
			Log.e(LOG_TAG, "Error updating last sync time. Code " + e.getLocalizedMessage());
		}

		Log.v(LOG_TAG, "Finished updating last sync " + logDirection + " time");
	}

	private Date[] doSync(SyncFetcher fetcher, Date lastDownSyncDate, SyncImporter importer,
			SyncPusher pusher, Date lastUpSyncDate, PartialSyncCallback onPartialSync) {
		Log.v(LOG_TAG, "Performing sync for table");

		// Syncing up first so we don't duplicate records we just downloaded
		lastUpSyncDate = doSyncUp(pusher, lastUpSyncDate, onPartialSync);
		lastDownSyncDate = doSyncDown(fetcher, lastDownSyncDate, importer, onPartialSync);

		Log.v(LOG_TAG, "Finished sync for table, last sync down date is " + 
				(lastDownSyncDate == null ? "null" : lastDownSyncDate.toGMTString()) +
				" last up sync date is " + (lastUpSyncDate == null ? "null" : lastUpSyncDate.toGMTString()));

		return new Date[] {lastDownSyncDate, lastUpSyncDate};
	}

	private Date doSyncDown(SyncFetcher fetcher, Date lastSyncDate, SyncImporter importer,
			PartialSyncCallback onPartialSync) {
		while (okToContinueSyncing.get() && fetcher.hasMoreRecords()) {
			Log.v(LOG_TAG, "Syncing records since " + lastSyncDate);

			List<Map<String, Object>> records = fetcher.fetchRecords(lastSyncDate);
			Date newLastSyncDate = importer.importRecords(records);
			if (newLastSyncDate != null) {
				// On the last fetch we'll get no data (this is because we can't know how many records remain in Parse)
				// Import will return null, wiping out our last sync date
				lastSyncDate = newLastSyncDate;

				Calendar zulu = Calendar.getInstance();
				zulu.setTime(newLastSyncDate);
				zulu.setTimeZone(TimeZone.getTimeZone("Etc/Zulu"));
				onPartialSync.onPartialDownSync(zulu.getTime());
			}
			Log.v(LOG_TAG, "Did intermediate sync for table, last sync date is " + (lastSyncDate == null ? "null" : lastSyncDate.toGMTString()));
		}

		return lastSyncDate;
	}

	private Date doSyncUp(SyncPusher pusher, Date lastSyncDate, PartialSyncCallback onPartialSync) {
		Date newDate = pusher.doSyncSinceDate(lastSyncDate);

		if (newDate != null) {
			Calendar zulu = Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu"));
			zulu.setTime(newDate);
			onPartialSync.onPartialUpSync(zulu.getTime());
		}
		return newDate;
	}

	/**
	 * Since meter data contains a key name 'Index', and this is a reserved word in SQLite,
	 * we need to append some token to get it into the database. This method returns the
	 * actual key for the modified local key.
	 * 
	 * e.g. 'Index' becomes 'IndexH0W81G', method will return 'Index' for 'IndexH0W81G'
	 * 
	 * See also localKeyForNetworkKey
	 * 
	 * @param localKey
	 * @return the original key if localKey is a modified key, otherwise localKey
	 */
	public static String networkKeyForLocalKey(String databaseName, String localKey) {
		Map<String, String> dbMap = localKeyToNetworkKeyMap.get(databaseName);
		boolean haveOriginalKey = dbMap != null && dbMap.containsKey(localKey);

		if (haveOriginalKey) {
			return dbMap.get(localKey);
		} else {
			return localKey;
		}
	}

	/**
	 * Since meter data contains a key name 'Index', and this is a reserved word in SQLite,
	 * we need to append some token to get it into the database. This method returns the
	 * modified local key for the network key.
	 * 
	 * e.g. 'Index' becomes 'IndexH0W81G'
	 * 
	 * See also networkKeyForLocalKey
	 * 
	 * @param networkKey
	 * @return the modified local key if networkKey needs to be modified, otherwise networkKey
	 */
	public static String localKeyForNetworkKey(String databaseName, String networkKey) {
		Map<String, String> dbMap = networkKeyToLocalKeyMap.get(databaseName);
		boolean haveOriginalKey = dbMap != null && dbMap.containsKey(networkKey);

		if (haveOriginalKey) {
			return dbMap.get(networkKey);
		} else {
			return networkKey;
		}
	}

	private PartialSyncCallback generatePartialSyncCallbackForTable(final String tableName,
			Map<String, Date> lastDown, Map<String, Date> lastUp) {
		return new PartialSyncCallback(lastDown, lastUp) {

			@Override
			public void onPartialUpSync(Date lastSyncDate) {
				updateLastUpSyncTimeForTable(tableName, lastSyncDate, lastUpTimes);
			}

			@Override
			public void onPartialDownSync(Date lastSyncDate) {
				updateLastDownSyncTimeForTable(tableName, lastSyncDate, lastDownTimes);
			}
		};
	}

	private abstract class PartialSyncCallback {
		protected Map<String, Date> lastDownTimes;
		protected Map<String, Date> lastUpTimes;

		public PartialSyncCallback(Map<String, Date> down, Map<String, Date> up) {
			lastDownTimes = down;
			lastUpTimes = up;
		}

		public abstract void onPartialUpSync(Date lastSyncDate);
		public abstract void onPartialDownSync(Date lastSyncDate);
	}
}
