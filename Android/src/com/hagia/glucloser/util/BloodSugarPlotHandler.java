package com.hagia.glucloser.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.WebView;

public class BloodSugarPlotHandler {
	private static final String LOG_TAG = "Pump_BS_Plot_Handler";

	public static final int DATE_NONE = 0;
	public static final int DATE_DAY = 1;
	public static final int DATE_HOUR = 2;
	private static final int DATE_CAP = 3;

	private WebView plotView;
	private Map<Date, Integer> sensorData, meterData;
	private List<Date> bolusList;
	private int dateDisplayType = DATE_NONE;

	public BloodSugarPlotHandler(WebView wv) {
		plotView = wv;
		plotView.addJavascriptInterface(this, "Glucloser");
		sensorData = new TreeMap<Date, Integer>();
		meterData = new TreeMap<Date, Integer>();
		bolusList = new ArrayList<Date>();
	}

	public void logJSMessage(String level, String message) {
		if (level.equals("V")) {
			Log.v(LOG_TAG, message);
		} else if (level.equals("D")) {
			Log.d(LOG_TAG, message);
		} else if (level.equals("I")) {
			Log.i(LOG_TAG, message);
		} else if (level.equals("W")) {
			Log.w(LOG_TAG, message);
		} else if (level.equals("E")) {
			Log.e(LOG_TAG, message);
		} else {
			Log.e(LOG_TAG, "JS logged the following message with " +
					"invalid level: '" + level + "'");
			Log.e(LOG_TAG, message);
		}
	}

	public void setDisplayDateBy(int unit) {
		if (unit >= DATE_NONE && unit < DATE_CAP) {
			dateDisplayType = unit;
		}
	}

	public synchronized void updateData(Map<Date, Integer> sD,
			Map<Date, Integer> mD,
			List<Date> boluses) {
		if (sD != null) {
			sensorData.putAll(sD);
		}
		if (mD != null) {
			meterData.putAll(mD);
		}
		if (boluses != null) {
			bolusList.addAll(boluses);
		}
	}
	
	/**
	 * @brief Maps the provided data onto the time window displayed by this plot.
	 * 
	 * Mapping is done as follows:
	 * The date of the provided data points is discarded.
	 * If no data exists for the time of a data point the point is added.
	 * If data does exist for the time of a data point it is averaged with the new value.
	 * 
	 * @note This method is synchronous. It should not be called on 
	 * the main thread.
	 * 
	 * @param sD Sensor Data
	 * @param mD Meter Data
	 * @param boluses Boluses
	 */
	public synchronized void updateDataForAverage(Map<Date, Integer> sD,
			Map<Date, Integer> mD,
			List<Date> boluses) {
		if (sD != null) {
			Date date;
			Integer reading;
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu"));
			for (Entry<Date, Integer> entry : sD.entrySet()) {
				date = entry.getKey();
				reading = entry.getValue();
				
				cal.setTime(date);
			}
		}
		if (mD != null) {
			meterData.putAll(mD);
		}
		if (boluses != null) {
			bolusList.addAll(boluses);
		}
	}

	public synchronized void loadGraph() {
		if (sensorData.isEmpty() && meterData.isEmpty()) {
			return;
		}

		doWorkForPlotGraph();
	}

	private JSONObject getPointOptionsJSON(boolean showPoint) {
		JSONObject pointOption = new JSONObject();
		try {
			pointOption.put("show", showPoint);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return pointOption;		
	}

	private JSONObject getLineOptionsJSON(boolean showLine, boolean fillUnder) {
		JSONObject lineOption = new JSONObject();
		try {
			lineOption.put("show", showLine);
			lineOption.put("fill", fillUnder);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return lineOption;
	}

	private JSONObject getXAxisOptions(Map<Integer, Date> ticks) {
		JSONObject axisOption = new JSONObject();
		JSONArray values = new JSONArray();

		if (dateDisplayType != DATE_NONE) {
			for (Entry<Integer, Date> e : ticks.entrySet()) {
				JSONArray temp = new JSONArray();

				// TODO i must be the actual idx value in range of x axis
				// e.g. if one data point per hour, ticks at 0, 24, 48, etc.
				temp.put(e.getKey());
				temp.put(DateFormat.format("MM/dd", e.getValue().getTime()));
				values.put(temp);
			}

			try {
				axisOption.put("ticks", values);
				axisOption.put("show", true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return axisOption;
	}

	private JSONObject getYAxisOptions(int min, int max) {
		JSONObject axisOption = new JSONObject();

		try {
			axisOption.put("ticks", 4);
			axisOption.put("min", min);
			axisOption.put("max", max);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return axisOption;		
	}

	private JSONObject getLegendOptions(int numDataSeries) {
		JSONObject legendOptions = new JSONObject();

		try {
			legendOptions.put("show" , true);
			legendOptions.put("container", "#legendContainer");
			legendOptions.put("noColumns", numDataSeries);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return legendOptions;
	}

	private void doWorkForPlotGraph() {
		List<Date> sensorKeys = new ArrayList<Date>(sensorData.keySet().size());
		sensorKeys.addAll(sensorData.keySet());
		Collections.sort(sensorKeys);

		List<Date> meterKeys = new ArrayList<Date>(meterData.keySet().size());
		meterKeys.addAll(meterData.keySet());
		Collections.sort(meterKeys);

		// Get ticks for y axis
		int yMin = 0, yMax = 0;

		int sensorValuesMin = 0;
		int sensorValuesMax = 0;
		int meterValuesMin = 0;
		int meterValuesMax = 0;
		if (!sensorData.isEmpty()) {
			sensorValuesMin = Collections.min(sensorData.values()) - 10;
			sensorValuesMax = Collections.max(sensorData.values()) + 10;
		}
		if (!meterData.isEmpty()) {
			meterValuesMin = Collections.min(meterData.values()) - 10;
			meterValuesMax = Collections.max(meterData.values()) + 10;
		}

		yMin = Math.min(sensorValuesMin, meterValuesMin);
		yMax = Math.max(sensorValuesMax, meterValuesMax);

		// Get ticks for x axis
		List<Date> dates = new ArrayList<Date>(
				sensorKeys.size() + meterKeys.size() + bolusList.size());
		dates.addAll(sensorKeys);
		dates.addAll(meterKeys);
		dates.addAll(bolusList);
		Collections.sort(dates);

		Map<Integer, Date> dateTicks = new TreeMap<Integer, Date>();
		Map<Date, Integer> xAxisPosition = new HashMap<Date, Integer>();

		if (dateDisplayType != DATE_NONE && dates.size() > 0) {
			int field = Calendar.HOUR;
			int value = 0;
			if (dateDisplayType == DATE_DAY) {
				field = Calendar.HOUR;
				value = 24;
			} else if (dateDisplayType == DATE_HOUR) {
				field = Calendar.HOUR;
				value = 1;
			}

			Calendar toTZ = Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu"));

			Date currentDate = dates.get(0);

			toTZ.setTime(currentDate);
			toTZ.roll(Calendar.HOUR, 0);
			toTZ.roll(Calendar.MINUTE, 0);
			toTZ.roll(Calendar.SECOND, 0);
			toTZ.roll(Calendar.MILLISECOND, 0);

			dateTicks.put(0, toTZ.getTime());
			xAxisPosition.put(currentDate, 0);

			toTZ.add(field, value);
			Date nextDate = toTZ.getTime();

			int size = dates.size();
			for (int i = 1; i < size; i ++) {
				Date date = dates.get(i);
				xAxisPosition.put(date, i);

				if (date.compareTo(nextDate) == -1) {
					continue;
				}

				dateTicks.put(i, toTZ.getTime());

				toTZ.add(field, value);
				nextDate = toTZ.getTime();
			}
		}


		JSONArray sensorDataPoints = new JSONArray();
		for (int idx = 0; idx < sensorKeys.size(); ++idx) {
			try {
				JSONArray tuple = new JSONArray();
				Integer position = xAxisPosition.get(sensorKeys.get(idx));
				tuple.put(0, position == null ? idx : position);
				tuple.put(1, sensorData.get(sensorKeys.get(idx)));
				sensorDataPoints.put(idx, tuple);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}


		JSONArray meterDataPoints = new JSONArray();
		for (int idx = 0; idx < meterKeys.size(); ++idx) {
			try {
				JSONArray tuple = new JSONArray();
				Integer position = xAxisPosition.get(meterKeys.get(idx));
				tuple.put(0, position == null ? idx : position);
				tuple.put(1, meterData.get(meterKeys.get(idx)));
				meterDataPoints.put(idx, tuple);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		JSONArray bolusDataPoints = new JSONArray();
		for (int idx = 0; idx < bolusList.size(); ++idx) {
			try {
				JSONArray tuple = new JSONArray();
				Integer position = xAxisPosition.get(bolusList.get(idx));
				tuple.put(0, position == null ? idx : position);
				tuple.put(1, yMin);
				bolusDataPoints.put(idx, tuple);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		JSONArray paramsArray = new JSONArray();
		try {
			if (sensorDataPoints.length() > 0) {
				JSONObject sensorSeries = new JSONObject();
				sensorSeries.put("data", sensorDataPoints);
				sensorSeries.put("lines", getLineOptionsJSON(true, true));
				sensorSeries.put("points", getPointOptionsJSON(false));
				sensorSeries.put("label", "Sensor");
				paramsArray.put(sensorSeries);
			}

			if (meterDataPoints.length() > 0) {
				JSONObject meterSeries = new JSONObject();
				meterSeries.put("data", meterDataPoints);
				meterSeries.put("lines", getLineOptionsJSON(false, false));
				meterSeries.put("points", getPointOptionsJSON(true));
				meterSeries.put("label", "Meter");
				paramsArray.put(meterSeries);
			}

			if (bolusDataPoints.length() > 0) {
				JSONObject bolusSeries = new JSONObject();
				bolusSeries.put("data", bolusDataPoints);
				bolusSeries.put("lines", getLineOptionsJSON(false, false));
				bolusSeries.put("points", getPointOptionsJSON(true));
				bolusSeries.put("label", "Bolus");
				paramsArray.put(bolusSeries);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		} 

		String jsString = "javascript:showGraph(" 
				+ paramsArray.toString() + ",";

		JSONObject optionsObject = new JSONObject();
		try {
			optionsObject.put("xaxis", getXAxisOptions(dateTicks));
			optionsObject.put("yaxis", getYAxisOptions(yMin, yMax));
			optionsObject.put("legend", getLegendOptions(paramsArray.length()));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		jsString += optionsObject.toString() + ")";

		Log.v(LOG_TAG, "Running js " + jsString);
		plotView.loadUrl(jsString);
	}
	
}