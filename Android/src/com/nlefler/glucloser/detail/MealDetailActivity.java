package com.nlefler.glucloser.detail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nlefler.glucloser.fragments.add.AddMealFragment;
import com.nlefler.glucloser.types.Food;
import com.nlefler.glucloser.types.MealToFood;
import com.nlefler.glucloser.types.Place;
import com.nlefler.glucloser.types.TagToFood;
import com.nlefler.glucloser.util.RequestIdUtil;
import com.nlefler.glucloser.R;
import com.nlefler.glucloser.types.Bolus;
import com.nlefler.glucloser.types.Meal;
import com.nlefler.glucloser.types.Tag;
import com.nlefler.glucloser.util.BloodSugarPlotHandler;
import com.nlefler.glucloser.util.LocationUtil;
import com.nlefler.glucloser.util.MeterDataUtil;
import com.nlefler.glucloser.util.TagToFoodUtil;
import com.nlefler.glucloser.util.MeterDataUtil.BloodSugarDataResults;
import com.nlefler.glucloser.util.database.save.FoodUpdatedEvent;
import com.nlefler.glucloser.util.database.save.PlaceUpdatedEvent;
import com.nlefler.glucloser.util.database.save.SaveManager;
import com.squareup.otto.Subscribe;

public class MealDetailActivity extends Activity {
	private static final String LOG_TAG = "Pump_Meal_Detail_Activity";
	
	public static final String MEAL_KEY = "meal";

	public enum NavigationItem {
		Edit
	}
	
	private Meal meal;

	private ActionBar actionBar;
	
	private static final SimpleDateFormat bolusStartFormatter = 
			new SimpleDateFormat("H:mm:ss");
	private Date bolusStarted;

	private TextView placeNameView;
	private TextView timeEatenView;
	private TextView bolusStartedView;
	private WebView plotView;
	private LinearLayout foodLayout;
	private LinearLayout bolusLayout;
	private LinearLayout tagsLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meal_detail_view);
		
		actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
		
		LocationUtil.initialize(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this.getApplicationContext());
		extractUIElements();
		
		setupState(savedInstanceState != null ? savedInstanceState : getIntent().getExtras());

        SaveManager.getPlaceUpdatedBus().register(this);
	}
	
	@Override
	protected void onStop() {
		Log.i(LOG_TAG, "Stop");
		LocationUtil.shutdown();

        SaveManager.getPlaceUpdatedBus().unregister(this);

		super.onStop();
	}

	@Override
	protected void onResume() {
		Log.i(LOG_TAG, "Resume");

		LocationUtil.initialize(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this.getApplicationContext());

        SaveManager.getPlaceUpdatedBus().register(this);

		super.onResume();
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
			Bundle args = new Bundle();
			
			args.putSerializable(AddMealFragment.MEAL_KEY, meal);

			AddMealFragment fragment = new AddMealFragment();
			fragment.setArguments(args);

			showFragment(fragment, "EDIT");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void extractUIElements() {
		placeNameView = (TextView)findViewById(R.id.meal_detail_view_place_name);
		timeEatenView = (TextView)findViewById(R.id.meal_detail_view_eaten_time);
		bolusStartedView = (TextView)findViewById(R.id.meal_detail_view_bolus_started_text);
		plotView = (WebView)findViewById(R.id.meal_detail_view_plot_view);
		foodLayout = (LinearLayout)findViewById(R.id.meal_detail_view_food_table_list_layout);
		bolusLayout = (LinearLayout)findViewById(R.id.meal_detail_view_bolus_list_layout);
		tagsLayout = (LinearLayout)findViewById(R.id.meal_detail_view_tags_list_layout);
	}

	private void setupState(Bundle state) {
		if (state != null && state.containsKey(MEAL_KEY)) {
			meal = (Meal)state.getSerializable(MEAL_KEY);
		} else {
			meal = new Meal();
		}
		
		new AsyncTask<Meal, Void, Void>() {
			@Override
			protected Void doInBackground(Meal... params) {
				meal.linkPlace();
				meal.linkFoods();
				return null;
			}

			@Override
			protected void onPostExecute(Void nil) {				
				setupViews();
			}

		}.execute(meal);
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

	@SuppressLint("SetJavaScriptEnabled")
	private void setupViews() {
		placeNameView.setText(meal.placeToMeal.place.name);

		Date dateEaten = meal.getDateEaten();
		timeEatenView.setText(DateFormat.format("MMM dd, kk:mm", dateEaten));

		final BloodSugarPlotHandler graphHandler = new BloodSugarPlotHandler(plotView);
		plotView.getSettings().setJavaScriptEnabled(true);

		plotView.addJavascriptInterface(graphHandler, "Android");
		plotView.loadUrl("file:///android_asset/blood_sugar_plots/stats_graph.html");

		getBloodSugarData(meal, graphHandler);

		final Handler handler = new Handler();
		new AsyncTask<Meal, Void, Void>() {

			@Override
			protected Void doInBackground(Meal... params) {
				Meal meal = params[0];

				Log.v(LOG_TAG, "Meal food and tag data ready");
				Log.v(LOG_TAG, "Making " + meal.mealToFoods.size() + " view for foods");

				int carbTotal = 0;
				for (MealToFood mealToFood : meal.mealToFoods) {
					final Food food = mealToFood.food;
					carbTotal += food.carbs;
					handler.post(new Runnable() {

						@Override
						public void run() {
							createViewForFood(food);
						}

					});


					Log.v(LOG_TAG, "Adding food to detail view");
					List<TagToFood> tagToFoods = TagToFoodUtil.getAllTagToFoodsForFood(food);
					for (TagToFood tagToFood : tagToFoods) {
						final Tag tag = tagToFood.tag;
						handler.post(new Runnable() {

							@Override
							public void run() {
								createViewForTag(tag);
							}

						});
					}
				}
				final Collection<Bolus> bolusList = MeterDataUtil.getBolusDataForMeal(meal, carbTotal);
				Log.v(LOG_TAG, "Got " + bolusList.size() + " boluses for meal");

				// TODO: Stop using handler
				handler.post(new Runnable() {

					@Override
					public void run() {
						if (bolusList.isEmpty()) {
							createNoBolusMessages();
						} else {
							List<Date> bolusDateList = new ArrayList<Date>();
							for (final Bolus bolus : bolusList) {
								createViewForBolus(bolus);
								updateBolusStartedViewWith(bolus);
								bolusDateList.add(bolus.timeStarted);
							}

							graphHandler.updateData(
									null, null, bolusDateList);
							graphHandler.loadGraph();
						}
					}
				});

				return null;
			}

		}.execute(meal);
	}

	private void createViewForFood(final Food food) {
		RelativeLayout foodItem = (RelativeLayout)getLayoutInflater().inflate(
				R.layout.food_line_item, null);
		TextView name = (TextView)foodItem.findViewById(R.id.food_line_item_food_name);
		TextView carbs = (TextView)foodItem.findViewById(R.id.food_line_item_carbs_value);

		String foodName = food.name;
		if (food.isCorrection) {
			foodName += " (C)";
		}
		foodItem.setTag(food);
		name.setText(foodName);
		carbs.setText(String.valueOf(food.carbs));

		foodItem.setClickable(true);
		foodItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent detailIntent = new Intent(MealDetailActivity.this, FoodDetailActivity.class);
				detailIntent.putExtra(FoodDetailActivity.FOOD_EXTRA_KEY, food.id);
				//detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MealDetailActivity.this.startActivity(detailIntent);
			}
		});
		foodLayout.addView(foodItem);
	}

	private void createNoBolusMessages() {
		TextView noBolusView = new TextView(getApplicationContext());
		noBolusView.setText(R.string.no_bolus_found);
		noBolusView.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_DeviceDefault_Medium);
        noBolusView.setTextColor(getResources().getColor(R.color.black));

		bolusLayout.addView(noBolusView);
		bolusStartedView.setText(R.string.no_bolus_found);
	}

	private void createViewForBolus(Bolus bolus) {
		RelativeLayout bolusItem = (RelativeLayout) bolusLayout.findViewWithTag(bolus);
		if (bolusItem == null) {
			bolusItem = (RelativeLayout)getLayoutInflater().inflate(R.layout.bolus_list_item, null);
			bolusLayout.addView(bolusItem);
		}
		TextView type = (TextView)bolusItem.findViewById(R.id.bolus_item_list_type);
		TextView units = (TextView)bolusItem.findViewById(R.id.bolus_item_list_unit_count);

		String typeText = bolus.getTypeForDisplay();
		if (bolus.type.equals(Bolus.TYPE_DUAL_SQUARE) ||
				bolus.type.equals(Bolus.TYPE_SQUARE)) {
			typeText += " (" + bolus.getLengthForDisplay() + ")";
		}
		type.setText(typeText);
		units.setText(String.valueOf(bolus.getUnitsForDisplay()));

		bolusItem.setTag(bolus);
	}

	private void updateBolusStartedViewWith(Bolus bolus) {
		if (bolusStarted == null ||
				bolus.timeStarted.after(bolusStarted)) {
			return;
		}
		bolusStarted = bolus.timeStarted;
		Calendar print = Calendar.getInstance(TimeZone.getDefault());
		print.setTime(bolusStarted);
		bolusStartedView.setText(bolusStartFormatter.format(print.getTime()));
	}

	private void createViewForTag(Tag tag) {
		Log.i(LOG_TAG, "Adding tag to detail view");
		TextView newTag = (TextView)getLayoutInflater().inflate(R.layout.tag_list_item, null);
		newTag.setTag(tag);

		newTag.setText(tag.name);
		tagsLayout.addView(newTag);
	}

	private void getBloodSugarData(Meal meal, final BloodSugarPlotHandler graphHandler) {
		new AsyncTask<Meal, Map<Date, Integer>, Void>() {

			@Override
			protected Void doInBackground(Meal... params) {
				BloodSugarDataResults results = new BloodSugarDataResults(null, null,
						RequestIdUtil.getNewId(), true);
				while (results.hasMoreResults) {
					results = MeterDataUtil.getBloodSugarDataForHoursFromDate(params[0].getDateEaten(),
						3, results.requestId);
					graphHandler.updateData(results.sensorData, results.meterData, null);
					graphHandler.loadGraph();
				}
				
				return null;
			}
			
		}.execute(meal);
	}
	
	@Subscribe public void foodUpdated(FoodUpdatedEvent event) {
		Log.v(LOG_TAG, "Got food updated notification for food " + event.getFood());
	}
	
	@Subscribe public void placeUpdated(PlaceUpdatedEvent event) {
		Log.v(LOG_TAG, "Got place updated notification for place " + event.getPlace());
	}
}
