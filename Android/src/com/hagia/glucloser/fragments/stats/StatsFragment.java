package com.hagia.glucloser.fragments.stats;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.hagia.glucloser.util.BloodSugarPlotHandler;
import com.hagia.glucloser.util.ListUtil;
import com.hagia.glucloser.util.MeterDataUtil;
import com.hagia.glucloser.util.RequestIdUtil;
import com.hagia.glucloser.R;

public class StatsFragment extends Fragment {
	private static final String LOG_TAG = "Pump_Stats_Activity";

	private Handler handler;

	private TextView plotLabel;
	private TextView hba1cLabel;
	private TextView hba1cText;
	private TextView anhydroLabel;
	private TextView anhydroText;
	private TextView averageLabel;
	private TextView averageText;
	private WebView plotView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.stats_view, container, false);

		setupMemberVars(view);
		setupViews();

		Calendar today = Calendar.getInstance();

		Calendar week = Calendar.getInstance();
		week.add(Calendar.HOUR, -1 * 24 * 7);

		Log.i(LOG_TAG, "Getting blood sugars for past week: " + today.getTime().toLocaleString() + " to " + week.getTime().toLocaleString());
		final BloodSugarPlotHandler graphHandler = new BloodSugarPlotHandler(
				plotView);
		graphHandler.setDisplayDateBy(BloodSugarPlotHandler.DATE_DAY);
		plotView.getSettings().setJavaScriptEnabled(true);

		plotView.addJavascriptInterface(graphHandler, "Android");
		plotView.loadUrl("file:///android_asset/blood_sugar_plots/stats_graph.html");

		getBloodSugarDataForPlot(week.getTime(), today.getTime(), graphHandler);

		Calendar months = Calendar.getInstance();
		months.add(Calendar.HOUR, -1 * 24 * 7 * 4 * 3);

		getBloodSugarDataForHbA1C(months.getTime(), today.getTime());
		getBloodSugarDataFor15Anhydroglucitol(months.getTime(), today.getTime());

		return view;
	}

	private void setupMemberVars(View parentView) {
		handler = new Handler();

		plotLabel = (TextView)parentView.findViewById(R.id.stats_view_plot_label);
		plotView = (WebView)parentView.findViewById(R.id.stasts_view_blood_sugar_plot);
		hba1cLabel = (TextView)parentView.findViewById(R.id.stats_view_hba1c_label);
		hba1cText = (TextView)parentView.findViewById(R.id.stats_view_hba1c);
		anhydroLabel = (TextView)parentView.findViewById(R.id.stats_view_15anhydro_label);
		anhydroText = (TextView)parentView.findViewById(R.id.stats_view_15anhydro);
		averageLabel = (TextView)parentView.findViewById(R.id.stats_view_average_label);
		averageText = (TextView)parentView.findViewById(R.id.stats_view_average_blood_sugar);
	}

	private void setupViews() {
		plotLabel.setText("Recent Blood Sugar - 1 Week");
		hba1cLabel.setText("HbA1C - 3 Months");
		anhydroLabel.setText("1,5-Anhydroglucitol - 3 Months");
		averageLabel.setText("Average Blood Sugar - 3 Months");
	}

	private void getBloodSugarDataForPlot(final Date fromDate, final Date toDate,
			final BloodSugarPlotHandler graphHandler) {
		new AsyncTask<Date, Void, Void>() {

			@Override
			protected Void doInBackground(Date... params) {
				MeterDataUtil.BloodSugarDataResults results = new MeterDataUtil.BloodSugarDataResults(null, null, 0, true);
				while (results.hasMoreResults) {
					results = MeterDataUtil.getBloodSugarDataFromDateToDate(
							params[0], params[1], -1, RequestIdUtil.getNewId());
					graphHandler.updateData(results.sensorData, results.meterData, null);
					graphHandler.loadGraph();
				}
				return null;
			}

		}.execute(fromDate, toDate);
	}

	private void getBloodSugarDataForHbA1C(final Date fromDate, final Date toDate) {
		new AsyncTask<Date, Map<Date, Integer>, Void>() {
			float sum = 0;
			int count = 0;

			@Override
			protected Void doInBackground(Date... params) {
				MeterDataUtil.BloodSugarDataResults results = new MeterDataUtil.BloodSugarDataResults(null, null,
						RequestIdUtil.getNewId(), true);
				while (results.hasMoreResults) {
					results = MeterDataUtil.getBloodSugarDataFromDateToDate(
							params[0], params[1], -1, results.requestId);
					publishProgress(results.sensorData, results.meterData);
				}

				return null;
			}

			@Override
			protected void onProgressUpdate(Map<Date, Integer>... data) {
				sum += ListUtil.sum(data[0].values());
				count += data[0].values().size();


				float average  = count == 0 ? 0 : sum / count;
				String hba1c = String.valueOf(MeterDataUtil.toHbA1C(average));


				hba1cText.setText(hba1c + "%");
				averageText.setText(String.valueOf(average) + " mg/dL");
			}

		}.execute(fromDate, toDate);	
	}

	private void getBloodSugarDataFor15Anhydroglucitol(final Date fromDate, final Date toDate) {
		new AsyncTask<Date, Map<Date, Integer>, Void>() {
			float sum = 0;
			int count = 0;

			@Override
			protected Void doInBackground(Date... params) {
				MeterDataUtil.BloodSugarDataResults results = new MeterDataUtil.BloodSugarDataResults(null, null,
						RequestIdUtil.getNewId(), true);
				while (results.hasMoreResults) {
					results = MeterDataUtil.getPostMealBloodSugarDataFromDateToDate(
							params[0], params[1], -1, results.requestId);
					publishProgress(results.sensorData, results.meterData);
				}

				return null;
			}

			@Override
			protected void onProgressUpdate(Map<Date, Integer>... data) {
				sum += (int)(ListUtil.sum(data[0].values()) + ListUtil.sum(data[1].values()));
				count += data[0].size() + data[1].size();

				float average  = count == 0 ? 0 : sum / count;
				Log.i(LOG_TAG, "Average for 1,5-An is " + average);
				String anhydro = String.valueOf(MeterDataUtil.to15Anhydroglucitol(average));

				anhydroText.setText(anhydro + " mcg/mL (" +
						average + "mg/dL)");

			}

		}.execute(fromDate, toDate);
	}
}
