package com.nlefler.glucloser.model.meterdata;

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

import com.nlefler.glucloser.model.bolus.Bolus;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.meterdata.MeterData;
import com.nlefler.glucloser.model.meal.MealUtil;
import com.nlefler.glucloser.util.RequestIdUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;

import se.emilsjolander.sprinkles.Query;


public class MeterDataUtil {
	private static final String LOG_TAG = "Glucloser_Blood_Sugar_Util";

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

	private static String whereClauseForGetBloodSugarDataFromDateToDate = 
			"(" + MeterData.SENSOR_GLUCOSE__MG_DL__DB_COLUMN_NAME + " != 0" +
					" OR " +
					MeterData.BG_READING__MG_DL_COLUMN_NAME + " != 0)" +
					" AND " + MeterData.TIMESTAMP_DB_COLUMN_NAME +
					" >= ? AND " + MeterData.TIMESTAMP_DB_COLUMN_NAME +
					" <= ?";
	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @param limit number of records to return, or -1 for all records
	 * @param requestId
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

		String toDateString = DatabaseUtil.parseDateFormat.format(toDate);
		String fromDateString = DatabaseUtil.parseDateFormat.format(fromDate);

        String query = "SELECT * FROM " +
                DatabaseUtil.tableNameForModel(MeterData.class) + " WHERE " +
                whereClauseForGetBloodSugarDataFromDateToDate + " LIMIT " +
                skip + ", " + realLimit;
        List<MeterData> results = Query.many(MeterData.class, query, fromDate, toDate).get().asList();

		Map<Date, Integer> sensorDataMap = new TreeMap<Date, Integer>();
		Map<Date, Integer> meterDataMap = new TreeMap<Date, Integer>();

        if (results.isEmpty()) {
            Log.i(LOG_TAG, "Got no results for blood sugar query, removing pagination date");

            borderDatesForBloodSugarPagination.remove(requestId);
            return new BloodSugarDataResults(sensorDataMap, meterDataMap, requestId, false);
        }

		Map<String, Object> record = null;
		for (MeterData meterData : results) {
			Date date;
			try {
				date = DatabaseUtil.parseDateFormat.parse(meterData.TIMESTAMP_DB_COLUMN_NAME);
			} catch (java.text.ParseException e) {
				Log.e(LOG_TAG, "Unable to parse record timestamp: " +
                        meterData.TIMESTAMP_DB_COLUMN_NAME);
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

		borderDatesForBloodSugarPagination.put(requestId, skip + realLimit);

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

	private static String whereClauseForGetBolusDataForMeal = 
			MeterData.TIMESTAMP_DB_COLUMN_NAME + " >= ? AND " +
			MeterData.TIMESTAMP_DB_COLUMN_NAME + " <= ? AND " +
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

		String fromDateString = DatabaseUtil.parseDateFormat.format(minTime.getTime());
		String toDateString = DatabaseUtil.parseDateFormat.format(maxTime.getTime());
		Date mealDate = meal.getDateEaten();

		String selectQuery = "SELECT * FROM " + DatabaseUtil.tableNameForModel(MeterData.class) +
		" WHERE " + whereClauseForGetBolusDataForMeal;

		List<MeterData> results = Query.many(MeterData.class, selectQuery, fromDateString,
			toDateString).get().asList();

		if (results.isEmpty()) {
			return bolusMap.values();
		}

		Log.v(LOG_TAG, "Got " + results.size() + " possible boluses");	
		Map<String, Object> record;
		for (MeterData meterData : results) {
			bolusMap.put(meterData.timestamp, meterData.getBolus());
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
