package com.hagia.pump.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import android.database.Cursor;
import android.util.Log;

import com.hagia.pump.types.Bolus;
import com.hagia.pump.types.Meal;
import com.hagia.pump.types.MeterData;
import com.hagia.pump.util.database.DatabaseUtil;
import com.hagia.pump.util.database.Tables;


public class MeterDataUtil {
	private static final String LOG_TAG = "Pump_Blood_Sugar_Util";

	private static Map<Long, Integer> borderDatesForBloodSugarPagination = 
			new HashMap<Long, Integer>();

	public static class BloodSugarDataResults {
		public Map<Date, Integer> sensorData;
		public Map<Date, Integer> meterData;
		public long requestId;
		public boolean hasMoreResults;

		public BloodSugarDataResults(Map<Date, Integer> s, Map<Date, Integer> m,
				long r, boolean h) {
			sensorData = s;
			meterData = m;
			requestId = r;
			hasMoreResults = h;
		}
	}

	private static String[] columnsForGetBloodSugarDataFromDateToDate = new String[] {
		MeterData.TIMESTAMP_DB_COLUMN_NAME,
		MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME,
		MeterData.BG_READING__MG_DL_COLUMN_NAME
	};
	private static String whereClauseForGetBloodSugarDataFromDateToDate = 
			"(" + MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME + " != 0" +
					" OR " +
					MeterData.BG_READING__MG_DL_COLUMN_NAME + " != 0)" +
					" AND " +
					DatabaseUtil.getStrfStringForString(MeterData.TIMESTAMP_DB_COLUMN_NAME) + 
					" >= " + DatabaseUtil.getStrfStringForString("?") +
					" AND " +
					DatabaseUtil.getStrfStringForString(MeterData.TIMESTAMP_DB_COLUMN_NAME) +
					" <= " + DatabaseUtil.getStrfStringForString("?");
	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @param limit number of records to return, or -1 for all records
	 * @param requestId
	 * @param callback
	 */
	public static BloodSugarDataResults getBloodSugarDataFromDateToDate(
			Date fromDate, Date toDate, int limit, final long requestId) {

		// We don't know how many records the query will return.
		// It could be on the order of tens of thousands.
		// It's better due to that case to batch the results into
		// multiple calls.
		// 
		// The previous reason (which follows) is now invalid, although
		// some of the logic remains:
		//
		// Parse limits the results of a query to 1000 rows.
		// We have no way of knowing if there are more rows that we haven't yet
		// received. What we can do is keep the state for a query, and tell the
		// client that there are more rows. The client then can re-call us, and
		// we'll use the last date and get the next set of rows. We'll end up calling
		// the query one extra time (returning zero results).

		final int realLimit = limit > 0 ? limit : 1000;
		int skip = 0;
		// Check the pagination map
		if (borderDatesForBloodSugarPagination.containsKey(requestId)) {
			// Clients should handle duplicate records already
			// for cache, don't need to move our from date
			skip = borderDatesForBloodSugarPagination.get(requestId);
		}

		Log.i(LOG_TAG, "Date to Date " + fromDate + " to " + toDate);

		final String toDateString = DatabaseUtil.parseDateFormat.format(toDate);
		final String fromDateString = DatabaseUtil.parseDateFormat.format(fromDate);
		final int skipCopy = skip;

		Log.v(LOG_TAG, "Running query " +
				"select " + columnsForGetBloodSugarDataFromDateToDate +
				" from " + Tables.METER_DATA_DB_NAME + " where " +
				whereClauseForGetBloodSugarDataFromDateToDate +
				" limit " + skipCopy + "," + realLimit +
				" with args " + fromDateString + " to " + toDateString);
		Cursor cursor = DatabaseUtil.instance().getReadableDatabase()
				.query(Tables.METER_DATA_DB_NAME,
						columnsForGetBloodSugarDataFromDateToDate,
						whereClauseForGetBloodSugarDataFromDateToDate,
						new String[] {fromDateString, toDateString},
						null, null, null,
						skipCopy + "," + realLimit);

		Map<Date, Integer> sensorDataMap = new TreeMap<Date, Integer>();
		Map<Date, Integer> meterDataMap = new TreeMap<Date, Integer>();

		if (!cursor.moveToFirst()) {
			Log.i(LOG_TAG, "Got no results for blood sugar query, removing pagination date");

			borderDatesForBloodSugarPagination.remove(requestId);
			return new BloodSugarDataResults(sensorDataMap, meterDataMap, requestId, false);
		}

		Map<String, Object> record = null;
		while (!cursor.isAfterLast()) {
			record = DatabaseUtil.getRecordFromCursor(cursor, MeterData.COLUMN_TYPES);
			cursor.moveToNext();

			Date date;
			try {
				date = DatabaseUtil.parseDateFormat.parse(
						(String)record.get(MeterData.TIMESTAMP_DB_COLUMN_NAME));
			} catch (java.text.ParseException e) {
				Log.e(LOG_TAG, "Unable to parse record timestamp: " +
						(String)record.get(MeterData.TIMESTAMP_DB_COLUMN_NAME));
				e.printStackTrace();
				continue;
			}

			Integer value = (Integer)record.get(MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME);
			if (value != null && value != 0) {
				sensorDataMap.put(date, value);

			}
			value = (Integer)record.get(MeterData.BG_READING__MG_DL_COLUMN_NAME);
			if (value != null && value != 0) {
				meterDataMap.put(date, value);
			}
		}

		borderDatesForBloodSugarPagination.put(requestId, skipCopy + realLimit);

		Log.i(LOG_TAG, "Got " + sensorDataMap.keySet().size() + " sensor values and " +
				meterDataMap.keySet().size() + " meter values for date to date query (" +
				fromDateString + " to " + toDateString);
		return new BloodSugarDataResults(sensorDataMap, meterDataMap, requestId, true);
	}

	public static BloodSugarDataResults getBloodSugarDataForHoursFromDate(Date date, int hours, 
			long requestId) {
		Calendar endDate = Calendar.getInstance();

		endDate.setTime(date);
		endDate.add(Calendar.HOUR, hours);

		return getBloodSugarDataFromDateToDate(date, endDate.getTime(), -1, requestId);
	}

	/**
	 * Gets blood sugar data for all meals in the date range.
	 * Data is included only for the range of 30 minutes to 2 hours
	 * after each meal.
	 * 
	 * @note This method is batched. Clients should check the
	 * @ref BloodSugarDataResults.hasMoreResults field to
	 * determine if more data is available.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @param fromDate
	 * @param toDate
	 * @param limit number of records to return, or -1 for all records
	 * @param requestId
	 * @return A @ref BloodSugarDataResults of the retrieved data
	 */
	public static BloodSugarDataResults getPostMealBloodSugarDataFromDateToDate(
			Date fromDate, Date toDate, int limit, final long requestId) {
		Map<Date, Integer> sensorData = new TreeMap<Date, Integer>();
		Map<Date, Integer> meterData = new TreeMap<Date, Integer>();

		List<Meal> meals = MealUtil.getMealsFromDateToDate(fromDate, toDate);

		final TimeZone zulu = TimeZone.getTimeZone("Etc/Zulu");

		BloodSugarDataResults result;
		for (Meal meal : meals) {
			Calendar sensorDelay = Calendar.getInstance(zulu);
			sensorDelay.setTime(meal.dateEaten);
			sensorDelay.add(Calendar.MINUTE, 30);

			result = getBloodSugarDataForHoursFromDate(sensorDelay.getTime(), 2, 
					RequestIdUtil.getNewId());
			sensorData.putAll(result.sensorData);
			meterData.putAll(result.meterData);
		}

		return new BloodSugarDataResults(sensorData, meterData, requestId, false);
	}

	private static String[] columnsForGetBolusDataForMeal = new String[] {
		MeterData.TIMESTAMP_DB_COLUMN_NAME,
		MeterData.BOLUS_TYPE_DB_COLUMN_NAME,
		MeterData.BWZ_ACTIVE_INSULIN__U__DB_COLUMN_NAME,
		MeterData.BWZ_CORRECTION_ESTIMATE__U__DB_COLUMN_NAME,
		MeterData.BWZ_FOOD_ESTIMATE__U__DB_COLUMN_NAME,
		MeterData.BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME
	};
	private static String whereClauseForGetBolusDataForMeal = 
			DatabaseUtil.getStrfStringForString(MeterData.TIMESTAMP_DB_COLUMN_NAME) + 
			" >= " + DatabaseUtil.getStrfStringForString("?") + " AND " +
			DatabaseUtil.getStrfStringForString(MeterData.TIMESTAMP_DB_COLUMN_NAME) + 
			" <= " + DatabaseUtil.getStrfStringForString("?") + " AND " +
			MeterData.BOLUS_TYPE_DB_COLUMN_NAME + " NOT NULL AND " +
			MeterData.BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME + " NOT NULl";

	/**
	 * Get bolus data for the given meal.
	 * 
	 * TODO: Document algorithm for finding boluses.
	 * 
	 * @note This method is synchronous. It should not be called on the
	 * main thread.
	 * 
	 * @param meal
	 * @param carbTotal
	 * @return A Collectin<Bolus> of all possible boluses for the meal
	 */
	public static Collection<Bolus> getBolusDataForMeal(Meal meal, int carbTotal) {
		List<Bolus> mealBoluses = new ArrayList<Bolus>();
		SortedMap<Date, Bolus> bolusMap = new TreeMap<Date, Bolus>();

		// Short circuit if carbTotal is zero
		if (carbTotal == 0) {
			return mealBoluses;
		}

		Calendar minTime = Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu"));
		Calendar maxTime = Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu"));

		minTime.setTime(meal.getDateEaten());
		maxTime.setTime(meal.getDateEaten());

		minTime.add(Calendar.MINUTE, -45);
		maxTime.add(Calendar.MINUTE, 45);

		Log.i(LOG_TAG, "Querying for boluses between " + minTime.getTime() + " to " + maxTime.getTime());

		final String fromDateString = DatabaseUtil.parseDateFormat.format(minTime.getTime());
		final String toDateString = DatabaseUtil.parseDateFormat.format(maxTime.getTime());
		final Date mealDate = meal.getDateEaten();

		Cursor cursor = DatabaseUtil.instance().getReadableDatabase().query(
				Tables.METER_DATA_DB_NAME, columnsForGetBolusDataForMeal,
				whereClauseForGetBolusDataForMeal,
				new String[] {fromDateString, toDateString},
				null, null, null);

		if (!cursor.moveToFirst()) {
			return bolusMap.values();
		}

		Log.v(LOG_TAG, "Got " + cursor.getCount() + " possible boluses");	
		Map<String, Object> record;
		while (!cursor.isAfterLast()) {
			record = DatabaseUtil.getRecordFromCursor(cursor, MeterData.COLUMN_TYPES);
			cursor.moveToNext();

			Date date;
			try {
				date = DatabaseUtil.parseDateFormat.parse((String)record.get(MeterData.TIMESTAMP_DB_COLUMN_NAME));
			} catch (java.text.ParseException e) {
				Log.e(LOG_TAG, "Unable to parse meter data timestamp: " +
						(String)record.get(MeterData.TIMESTAMP_DB_COLUMN_NAME));
				e.printStackTrace();
				continue;
			}

			Object value = null;
			String type = "Unknown bolus type";
			double units = 0;
			long length = 0;

			value = record.get(MeterData.BOLUS_TYPE_DB_COLUMN_NAME);
			if (value != null) {
				type = (String)value;
			}

			value = record.get(MeterData.BOLUS_VOLUME_DELIVERED__U__DB_COLUMN_NAME);
			if (value != null) {
				units = (Double)value;
			}

			value = record.get(MeterData.PROGRAMMED_BOLUS_DURATION__HH_MM_SS__DB_COLUMN_NAME);
			if (value != null) {
				length = ((Double)value).longValue();
			}

			bolusMap.put(date, new Bolus(type, units, length, date));
		}

		// Possible that multiple boluses are found within this window
		// need to reduce to correct meal.
		// Start with the closest bolus by date.
		Date closestDate = null;
		long mealDateValue = mealDate.getTime();
		long currentDiff = Long.MAX_VALUE;
		for (Date bolusDate : bolusMap.keySet()) {
			boolean update = false;

			if (closestDate == null) {
				update = true;
			} else {
				long diff = Math.abs(bolusDate.getTime() - mealDateValue);

				if (diff < currentDiff) {
					update = true;
				} else {
					// Because this is a SortedMap we can break here.
					// The rest of the dates will be further than the current.
					break;
				}
			}

			if (update) {
				closestDate = bolusDate;
				currentDiff = Math.abs(bolusDate.getTime() - mealDateValue);
			}
		}

		if (closestDate != null) {
			mealBoluses.add(bolusMap.get(closestDate));
		}
		// TODO: Continue searching if the closest bolus doesn't match
		// what we would expect based on the total carbs for the meal.
		// Requires knowing insulin/carb ratios.

		return mealBoluses;
	}

	public static float toHbA1C(float average) {
		// eAG(mg/dl) = 28.7 × A1C − 46.7

		return (float)((average + 46.7) / 28.7);
	}

	public static double to15Anhydroglucitol(double average) {
		// http://en.wikipedia.org/wiki/1,5-Anhydroglucitol
		// Average is post-meal max glucose
		// exponential fit of table in wiki article gives 
		// 1,5-An = 1077.41 * e ^ (-0.0253483 * average)
		return 1077.41 * Math.exp(-0.0253438 * average);
	}
}
