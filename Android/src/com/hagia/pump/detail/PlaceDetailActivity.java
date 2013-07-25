package com.hagia.pump.detail;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;
import com.hagia.hnotificationcenter.NotificationCenter;
import com.hagia.pump.R;
import com.hagia.pump.PumpActivity.NavigationItem;
import com.hagia.pump.R.id;
import com.hagia.pump.R.layout;
import com.hagia.pump.fragments.add.AddMealFragment;
import com.hagia.pump.fragments.add.AddPlaceFragment;
import com.hagia.pump.fragments.home.HomeFragment;
import com.hagia.pump.fragments.stats.StatsFragment;
import com.hagia.pump.types.Meal;
import com.hagia.pump.types.MealToFood;
import com.hagia.pump.types.Place;
import com.hagia.pump.util.BloodSugarPlotHandler;
import com.hagia.pump.util.LocationUtil;
import com.hagia.pump.util.MeterDataUtil;
import com.hagia.pump.util.PlaceUtil;
import com.hagia.pump.util.RequestIdUtil;
import com.hagia.pump.util.MeterDataUtil.BloodSugarDataResults;
import com.hagia.pump.util.database.DatabaseUtil;
import com.hagia.pump.util.database.save.SaveManager;

public class PlaceDetailActivity extends Activity {
	private static final String LOG_TAG = "Pump_Place_Detail_Activity";

	public static final String PLACE_EXTRA_KEY = "placeExtra";

	public enum NavigationItem {
		Edit
	}

	private ActionBar actionBar;

	private Place place;
	private TextView placeNameView;
	private TextView placeAddressView;
	private WebView plotView;
	private ListView mealsListView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads()
		.detectDiskWrites()
		.detectNetwork()
		.penaltyLog()
		.build());

		setContentView(R.layout.place_detail_view);
		actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);

		LocationUtil.initialize(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this.getApplicationContext());

		setupMemberVars(savedInstanceState != null ? savedInstanceState : getIntent().getExtras());

		try {
			NotificationCenter.getInstance().addObserverForNotificationCallingMethod(
					this, SaveManager.PLACE_UPDATED_NOTIFICATION, PlaceDetailActivity.class.getDeclaredMethod("updatePlace", Place.class));
		} catch (NoSuchMethodException e) {
			Log.e(LOG_TAG, "Can't listener to place updated notification, no handler method found");
			e.printStackTrace();
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStop() {
		Log.i(LOG_TAG, "Stop");
		LocationUtil.shutdown();

		super.onStop();
	}

	@Override
	protected void onResume() {
		Log.i(LOG_TAG, "Resume");

		LocationUtil.initialize(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this.getApplicationContext());

		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		Log.i(LOG_TAG, "Destroy");
		NotificationCenter.getInstance().removeObserver(this);
		
		super.onDestroy();
	}

	private void setupMemberVars(Bundle state) {
		placeNameView = (TextView)findViewById(R.id.place_detail_view_place_name);
		placeAddressView = (TextView)findViewById(R.id.place_detail_view_place_address);
		plotView = (WebView)findViewById(R.id.place_detail_view_plot_view);
		mealsListView = (ListView)findViewById(R.id.place_detail_view_meals_list_layout);

		place = null;
		if (state != null &&
				state.containsKey(PLACE_EXTRA_KEY)) {
			place = (Place)state.getSerializable(PLACE_EXTRA_KEY);
		} else if (getIntent() != null && getIntent().hasExtra(PLACE_EXTRA_KEY)) {
			place = (Place)getIntent().getSerializableExtra(PLACE_EXTRA_KEY);
		}
		if (place != null) {
			placeNameView.setText(place.name);
			placeAddressView.setText(place.readableAddress);

			// Plot
			final BloodSugarPlotHandler graphHandler = new BloodSugarPlotHandler(plotView);
			plotView.getSettings().setJavaScriptEnabled(true);
			plotView.addJavascriptInterface(graphHandler, "Android");
			plotView.loadUrl("file:///android_asset/blood_sugar_plots/stats_graph.html");
			new AsyncTask<Place, Void, Void> () {

				@Override
				protected Void doInBackground(Place... params) {
					Place place = params[0];

					List<Meal> meals = PlaceUtil.getAllMealsForPlace(place);

					for (Meal meal : meals) {
						long requestId = RequestIdUtil.getNewId();
						BloodSugarDataResults results = new BloodSugarDataResults(null, null, 0, true);

						while (results.hasMoreResults) {
							results = MeterDataUtil.getBloodSugarDataForHoursFromDate(
									meal.dateEaten, 3, requestId);
							graphHandler.updateData(results.sensorData, results.meterData, null);
							graphHandler.loadGraph();
						}
					}

					return null;
				}

			}.execute(place);

			// Meals List
			PlaceDetailMealListAdapter mealsAdapter = new PlaceDetailMealListAdapter(place);
			mealsListView.setAdapter(mealsAdapter);
		}
	}

	private void updatePlace(Place updatedPlace) {
		Log.v(LOG_TAG, "Got notification to update place");
		if (place == updatedPlace || place.id.equals(updatedPlace.id)) {
			Log.v(LOG_TAG, "Place matches, updating");

			place = updatedPlace;
			
			(new Handler()).post(new Runnable() {

				@Override
				public void run() {
					placeNameView.setText(place.name);
					placeAddressView.setText(place.readableAddress);
				}
				
			});
		} else {
			Log.v(LOG_TAG, "Updated place doesn't match");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.place_detail_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit:
			AddPlaceFragment fragment = new AddPlaceFragment();

			Bundle args = new Bundle();
			args.putSerializable(AddPlaceFragment.PLACE_KEY, place);
			fragment.setArguments(args);

			showFragment(fragment, "EDITPLACE");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void showFragment(Fragment fragment, String tag) {
		if (tag == null) {
			tag = "NOTAG";
		}

		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.main_fragment_container, 
				fragment, tag);
		transaction.addToBackStack(tag); 
		transaction.commit();
	}

	public void popFragmentStack() {
		FragmentManager manager = getFragmentManager();
		manager.popBackStack();
	}

}
