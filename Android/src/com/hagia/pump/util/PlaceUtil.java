package com.hagia.pump.util;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.hagia.pump.PumpActivity;
import com.hagia.pump.types.Food;
import com.hagia.pump.types.Meal;
import com.hagia.pump.types.Place;
import com.hagia.pump.types.PlaceToFoodsHash;
import com.hagia.pump.types.PlaceToMeal;
import com.hagia.pump.types.TagToPlace;
import com.hagia.pump.util.database.DatabaseUtil;
import com.hagia.pump.util.database.Tables;


public class PlaceUtil {
	private static final String LOG_TAG = "Pump_Place_Util";

	private static final double NEARBY_PLACES_DISTANCE_LIMIT = 100.0; // Distance in meters before a place is too far to be considered a 'nearby places'

	/**
	 * Get a place in the database with the provided id.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param id The id to search for
	 * @return A @ref Place representing the place in the database,
	 * or null if no place was found with the given id
	 */
	public static Place getPlaceById(String id) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_DB_NAME, null,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME + "=?", 
				new String[] {id}, null, null, null, "1");
		Place place = null;

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "No places with id " + id + " in local db");
			return place;
		}

		while (!cursor.isAfterLast()) {
			place = Place.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Place.COLUMN_TYPES));
			cursor.moveToNext();
		}

		return place;
	}

	/**
	 * Get all places in the database, sorted by their name ascending.
	 * 
	 * @note This method is synchronous. It should not be called on the
	 * main thread.
	 * 
	 * @return A List<Place> of places
	 */
	public static List<Place> getAllPlacesSortedByName() {
		return getAllPlacesSortedBy(Food.NAME_DB_COLUMN_KEY, true);
	}

	/**
	 * Get all places in the database, sorted by the provided column.
	 * 
	 * @note This method is synchronous. It should not be called on the
	 * main thread.
	 * 
	 * @param columnName The column to sort by
	 * @param asc True if the results should be sorted ascending
	 * @return A List<Place> of places
	 */
	public static List<Place> getAllPlacesSortedBy(String columnName, boolean asc) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_DB_NAME, null,
				null, null, null, null, columnName + " " + (asc ? "ASC" : "DESC"));
		List<Place> places = new ArrayList<Place>();

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "No places in local db");
			return places;
		}

		while (!cursor.isAfterLast()) {
			places.add(Place.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Place.COLUMN_TYPES)));
			cursor.moveToNext();
		}

		return places;
	}


	/**
	 * Gets recent meals for the provided place.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param mealLimit An upper bound on the number of meals to return,
	 * or 0 for all meals
	 * @param place
	 * @return A List<Meal> of recent meals
	 */
	public static List<Meal> getRecentMealsForPlace(int mealLimit, Place place) {
		Log.i(LOG_TAG, "Get recent meals");

		return _getAllMealsForPlace(place, mealLimit);

	}

	private static String whereClauseForGetPlacesNear = 
			Place.LATITUDE_DB_COLUMN_KEY + ">=? AND " +
					Place.LATITUDE_DB_COLUMN_KEY + "<=? AND " +
					Place.LONGITUDE_DB_COLUMN_KEY + ">=? AND " + 
					Place.LONGITUDE_DB_COLUMN_KEY + "<=?";
	private static double LATITUDE_DELTA = 0.018; // Approx. 2.001km
	private static double LONGITUDE_DELTA = 0.0230; // Approx. 2.0664km
	/**
	 * Get the places near the provided location. The definition of "near"
	 * is managed internally.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @param location The location to search for
	 * @return A List<Place> of nearby places.
	 */
	public static List<Place> getPlacesNear(final Location location) {
		List<Place> sortedList = new ArrayList<Place>();

		if (location == null) {
			return sortedList;
		}

		double maxDistance = 1;
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();

		Log.i(LOG_TAG, "Searching for places within " + maxDistance + "km of " +
				latitude + ", " + longitude);

		String minLat = String.valueOf(latitude - LATITUDE_DELTA);
		String maxLat = String.valueOf(latitude + LATITUDE_DELTA);
		String minLon = String.valueOf(longitude - LONGITUDE_DELTA);
		String maxLon = String.valueOf(longitude + LONGITUDE_DELTA);
		long dbStart;
		if (PumpActivity.LOG_LEVEL >= Log.VERBOSE) {
			dbStart = System.currentTimeMillis();
		}
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_DB_NAME,
				null, whereClauseForGetPlacesNear,
				new String[] {minLat, maxLat, minLon, maxLon},
				null, null, null);
		if (PumpActivity.LOG_LEVEL >= Log.VERBOSE) {
			Log.v(LOG_TAG, "GetPlacesNear db get took " + (System.currentTimeMillis() - dbStart));
		}
		if (!cursor.moveToFirst()) {
			return sortedList;
		}

		Map<String, Object> map;
		double distance = 0;
		Location recordLocation = new Location(LocationUtil.NO_PROVIDER);
		while (!cursor.isAfterLast()) {
			map = DatabaseUtil.getRecordFromCursor(cursor, Place.COLUMN_TYPES);
			cursor.moveToNext();

			recordLocation.setLatitude((Double)map.get(Place.LATITUDE_DB_COLUMN_KEY));
			recordLocation.setLongitude((Double)map.get(Place.LONGITUDE_DB_COLUMN_KEY));
			distance = location.distanceTo(recordLocation);

			if (distance > NEARBY_PLACES_DISTANCE_LIMIT) {
				continue;
			}
			Log.v(LOG_TAG, "Accepting " + map.get(Place.NAME_DB_COLUMN_KEY) + 
					" for nearby places query. It is " + 
					distance + " meters away.");
			sortedList.add(Place.fromMap(map));
		}

		Collections.sort(sortedList, new Comparator<Place>() {
			@Override
			public int compare(Place lhs, Place rhs) {
				return (int)(lhs.location.distanceTo(location)
						- rhs.location.distanceTo(location));
			}
		});

		return sortedList;
	}

	/**
	 * Get a list of places recently eaten at. The definition
	 * of 'recent' is managed internally.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @param limit An upper bound on the number of places to return
	 * @return A List<Place> of matching places, sorted by date eaten
	 * descending
	 */
	public static List<Place> getRecentPlaces(int limit) {
		Log.i("Place Util", "Get recent places");

		Calendar recentDate = Calendar.getInstance();
		recentDate.set(Calendar.HOUR, 0);
		recentDate.set(Calendar.MINUTE, 0);
		recentDate.set(Calendar.SECOND, 0);
		recentDate.set(Calendar.MILLISECOND, 0);
		recentDate.add(Calendar.HOUR, -72);

		Log.i("Place Util", "Asking for recent places as of " + DateFormat.getDateInstance().format(recentDate.getTime()));

		String boundDate = recentDate.getTime().toGMTString();

		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_DB_NAME, null,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME +
				" >= strftime('%Y-%m-%dT%H:%M:%fZ', ?)", 
				new String[] {boundDate}, null, null,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME + " DESC", String.valueOf(limit));
		List<Place> recentPlaces = new ArrayList<Place>();

		if (!cursor.moveToFirst()) {
			return recentPlaces;
		}

		while (!cursor.isAfterLast()) {
			Place place = Place.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Place.COLUMN_TYPES));

			recentPlaces.add(place);

			cursor.moveToNext();
		}

		Log.v("Place Util", "Found " + recentPlaces.size() + " recent dates");

		return recentPlaces;
	}

	/**
	 * Get the nearby places.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @return A List<Place> of places
	 */	
	public static List<Place> getNearbyPlaces() {
		return getPlacesNear(LocationUtil.getCurrentLocation());
	}

	/**
	 * Get the @ref Place with the provided name.
	 * 
	 * @note This method is synchronous. It should not be called
	 * on the main thread.
	 * 
	 * @param name The name of the place
	 * @return A @ref Place, or null if no place is found with the
	 * provided name
	 */
	public static Place getPlaceWithName(String name) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_DB_NAME, null,
				"lower(" + Place.NAME_DB_COLUMN_KEY + ") = lower(?)", new String[] {name}, 
				null, null, Place.NAME_DB_COLUMN_KEY + " DESC", null);
		Place place = null;

		if (!cursor.moveToFirst()) {
			return place;
		}

		while (!cursor.isAfterLast()) {
			place = Place.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Place.COLUMN_TYPES));

			cursor.moveToNext();
		}

		return place;
	}

	/**
	 * Get all places in the database whose name contains the provided
	 * string.
	 * 
	 * @note This method is synchronous. It should not be called on the
	 * main thread.
	 * 
	 * @param name The string to match place names against.
	 * @return A List<Place> of matching places
	 */	
	public static List<Place> getAllPlacesWithNameContaining(String name) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_DB_NAME, null,
				"lower(" + Place.NAME_DB_COLUMN_KEY + ") LIKE lower(?)", new String[] {"%" + name + "%"}, 
				null, null, Place.NAME_DB_COLUMN_KEY + " DESC", null);
		List<Place> matchingPlaces = new ArrayList<Place>();

		if (!cursor.moveToFirst()) {
			return matchingPlaces;
		}

		while (!cursor.isAfterLast()) {
			matchingPlaces.add(Place.fromMap(DatabaseUtil.getRecordFromCursor(cursor, Place.COLUMN_TYPES)));

			cursor.moveToNext();
		}

		return matchingPlaces;
	}

	// SELECT count(id) FROM PlaceToMeal WHERE place = ?
	private static final String selectClauseForGetNumberOfMealsForPlace =
			"SELECT count(id) FROM " + Tables.PLACE_TO_MEAL_DB_NAME;
	private static final String whereClauseForGetNumberOfMealsForPlace =
			" WHERE " + PlaceToMeal.PLACE_DB_COLUMN_KEY + "=?";

	/**
	 * Get the number of meals saved for the provided @ref Place.
	 * 
	 * @note This method is synchronous. It should not be called
	 * in the main thread.
	 * 
	 * @param place The place to get a meal count for
	 * @param callback The callback to call with the result
	 */
	public static int getNumberOfMealsForPlace(Place place) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().rawQuery(
				selectClauseForGetNumberOfMealsForPlace + whereClauseForGetNumberOfMealsForPlace,
				new String[] {place.id});

		int count = 0;

		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}

		return count;
	}

	/**
	 * Returns the most popular meals for the provided @ref Place.
	 * The meals are represented by a @ref List of @ref Food objects.
	 * 
	 * @param place The @ref Place to retrieve the most popular meals for
	 * @param mealLimit An upper bound on the number of meals to retrieve
	 * @return A list of lists of @ref Food objects.
	 */
	public static List<List<Food>> getMostPopularMealsForPlace(Place place,
			int mealLimit) {
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.PLACE_TO_FOODS_HASH_DB_NAME, 
				new String[] {PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY},
				PlaceToFoodsHash.PLACE_DB_COLUMN_KEY + "=?", new String[] {place.id}, 
				PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY, null,
				"COUNT(" + DatabaseUtil.OBJECT_ID_COLUMN_NAME + ") DESC",
				String.valueOf(mealLimit));
		List<List<Food>> meals = new ArrayList<List<Food>>();
		if (!cursor.moveToFirst()) {
			return meals;
		}

		Map<String, Object> record = new HashMap<String, Object>();
		while (!cursor.isAfterLast()) {
			DatabaseUtil.getRecordFromCursorIntoMap(cursor,
					PlaceToFoodsHash.COLUMN_TYPES, record);
			meals.add(FoodUtil.getFoodsForFoodHash(
					(String)record.get(PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY)));
			cursor.moveToNext();
		}

		return meals;
	}

	/**
	 * Retrieves all meals for a given place.
	 * The list will be ordered by date eaten descending.
	 * 
	 * @param place
	 * @return List<Meal> The list of meals
	 */
	public static List<Meal> getAllMealsForPlace(Place place) {
		return _getAllMealsForPlace(place, 0);
	}

	// SELECT * FROM Meal WHERE objectId IN (
	// 	SELECT meal FROM PlaceToMeal WHERE place = /place.id/
	// ) ORDER BY strftime('%Y-%m-%dT%H:%M:%fZ', updatedAt) DESC
	// LIMIT /mealLimit/
	private static final String whereClauseForGetAllMealsForPlace =
			DatabaseUtil.OBJECT_ID_COLUMN_NAME  + " IN " +
					"(SELECT " + PlaceToMeal.MEAL_DB_COLUMN_KEY + " FROM " +
					Tables.PLACE_TO_MEAL_DB_NAME + " WHERE " + 
					PlaceToMeal.PLACE_DB_COLUMN_KEY + "=?)";
	/**
	 * Retrieves a number of meals for a given place.
	 * The list will be ordered by date eaten descending.
	 * 
	 * @param 
	 * @param limit The number of meals to get. If all meals, use 0.
	 * @return List<Meal> The list of meals
	 */
	private static List<Meal> _getAllMealsForPlace(Place place, int limit) {
		String limitValue = limit > 0 ? String.valueOf(limit) : null;
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.MEAL_DB_NAME, null, whereClauseForGetAllMealsForPlace,
				new String[] {place.id}, null, null,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME + 
				" DESC", limitValue);
		List<Meal> meals = new ArrayList<Meal>();

		if (!cursor.moveToFirst()) {
			return meals;
		}

		while (!cursor.isAfterLast()) {
			meals.add(
					Meal.fromMap(
							DatabaseUtil.getRecordFromCursor(cursor, Meal.COLUMN_TYPES)));
			cursor.moveToNext();
		}

		return meals;
	}


	/**
	 * Saves the provided @ref Place using the default database using a transaction.
	 * 
	 * @param place The place to save
	 * @return The value of @ref savePlaceIntoDatabaseWithTransaction()
	 */
	public static long savePlace(Place place) {
		return savePlaceIntoDatabaseWithTransaction(place,
				DatabaseUtil.instance().getWritableDatabase(),
				true);
	}

	/**
	 * Inserts the provided @ref Place into the provided database.
	 * If a conflicting record exists in the database it is replaced.
	 * 
	 * @param place The place to insert
	 * @param db The database to insert into
	 * @param transaction Should the insert be executed in a transaction
	 * @return The new id if the insert was successful or -1
	 */
	public static long savePlaceIntoDatabaseWithTransaction(Place place,
			SQLiteDatabase db, boolean transaction) {
		ContentValues values = new ContentValues();

		if (place.createdAt == null) {
			place.createdAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}
		if (place.updatedAt == null) {
			place.updatedAt = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu")).getTime());
		}

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_DB_NAME,
				DatabaseUtil.OBJECT_ID_COLUMN_NAME), place.id);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_DB_NAME,
				DatabaseUtil.CREATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(place.createdAt));
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_DB_NAME,
				DatabaseUtil.UPDATED_AT_COLUMN_NAME), 
				DatabaseUtil.parseDateFormat.format(place.updatedAt));

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_DB_NAME,
				Place.NAME_DB_COLUMN_KEY), place.name);
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_DB_NAME,
				Place.LATITUDE_DB_COLUMN_KEY), place.location.getLatitude());
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_DB_NAME,
				Place.LONGITUDE_DB_COLUMN_KEY), place.location.getLongitude());
		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_DB_NAME,
				Place.READABLE_ADDRESS_COLUMN_KEY), place.readableAddress);

		values.put(DatabaseUtil.localKeyForNetworkKey(Tables.PLACE_DB_NAME,
				DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME), 
				place.needsUpload);

		if (transaction) db.beginTransaction();

		long code = db.insertWithOnConflict(
				Tables.PLACE_DB_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (code != -1) {
			for (TagToPlace tag : place.tagToPlaces) {
				if (!TagToPlaceUtil.saveTagToPlace(tag)) {
					if (transaction) db.endTransaction();

					return code;
				}
			}
			if (transaction) {
				db.setTransactionSuccessful();
				db.endTransaction();
			}
			return code;
		} else {
			Log.i(LOG_TAG, "Error code recieved from insert: " + code);
			Log.i(LOG_TAG, "Values are: " + values);
			if (transaction) db.endTransaction();

			return code;
		}
	}

	/**
	 * Retrieves any known addresses for the Place's location.
	 * If an address is found the Place's @ref Place.readableAddress
	 * is replaced with the first line of the retrieved address.
	 * 
	 * @note The @ref Place.needsUpload flag of the provided place
	 * is set to true.
	 * 
	 * @param place The place to update
	 */
	public static void updateReadableLocation(Place place) {
		List<Address> addresses = LocationUtil.getAddressFromLocation(place.location, 1);
		if (!addresses.isEmpty()) {
			// TODO drill up until we find a non-null address line value
			place.readableAddress = addresses.get(0).getAddressLine(0);
			place.needsUpload = true;
		}
	}
}
