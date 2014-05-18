package com.nlefler.glucloser.detail;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nlefler.glucloser.types.Food;
import com.nlefler.glucloser.R;
import com.nlefler.glucloser.types.Meal;
import com.nlefler.glucloser.types.MealToFood;
import com.nlefler.glucloser.util.BloodSugarPlotHandler;
import com.nlefler.glucloser.util.FoodUtil;
import com.nlefler.glucloser.util.MeterDataUtil;
import com.nlefler.glucloser.util.RequestIdUtil;
import com.nlefler.glucloser.util.MeterDataUtil.BloodSugarDataResults;

public class FoodDetailActivity extends Activity {
	private static final String LOG_TAG = "Pump_Food_Detail_Activity";

	public static final String FOOD_EXTRA_KEY = "foodExtra";

	private boolean isSetup = false;
	private Handler handler;
	private Food food;

	private TextView foodNameView;
	private TextView averageCarbsView;
	private WebView plotView;
	private LinearLayout mealsLayout;
	private LinearLayout tagsLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.food_detail_view);

		// Setup
		// We might be coming back from a pause
		if (!isSetup) {
			setupMemberVars();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
	}


	@Override
	protected void onStop() {
		super.onStop();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onClick(View clickedView) {
		int viewId = clickedView.getId();

	}

	private void setupMemberVars() {
		handler = new Handler();

		foodNameView = (TextView)findViewById(R.id.food_detail_view_food_name);
		averageCarbsView = (TextView)findViewById(R.id.food_detail_view_average_carbs);
		plotView = (WebView)findViewById(R.id.food_detail_view_plot_view);
		mealsLayout = (LinearLayout)findViewById(R.id.food_detail_view_meal_table_list_layout);
		tagsLayout = (LinearLayout)findViewById(R.id.food_detail_view_tags_list_layout);

		final ProgressDialog spinner = ProgressDialog.show(this, "", "Loading...", true);

		final String foodId = getIntent().getStringExtra(FOOD_EXTRA_KEY);
		new AsyncTask<Void, String, Void>() {
			Food food;
			float carbs;
			List<Meal> meals;

			@Override
			protected Void doInBackground(Void... params) {
				food = FoodUtil.getFoodById(foodId);
				publishProgress(food.name);
				carbs = FoodUtil.getAverageCarbsForFoodNamed(food.name);
				meals = FoodUtil.getAllMealsForFoodName(food.name);

				setupViewForCarbs(carbs);

				return null;
			}

			@Override
			protected void onProgressUpdate(String... foodName) {
				setupView(foodName[0]);
			}

			@Override
			protected void onPostExecute(Void result) {
				hideSpinnerIfNecessary(spinner);
				setupViewForCarbs(carbs);
				setupViewForMeals(meals);
			}

		}.execute();

	}

	private void hideSpinnerIfNecessary(final ProgressDialog spinner) {
		if (spinner.isShowing()) {
			spinner.hide();
		}
	}

	private void setupViewForCarbs(final float averageCarbs) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				averageCarbsView.setText(String.valueOf(averageCarbs));
			}

		});
	}

	private void setupViewForMeals(List<Meal> meals) {
		setupBloodSugarPlot(meals);

		for (final Meal meal : meals) {
			meal.linkFoods();
			meal.linkPlace();

			handler.post(new Runnable() {

				@Override
				public void run() {
					final RelativeLayout mealItem = (RelativeLayout)getLayoutInflater().inflate(R.layout.meal_list_item, null);
					TextView time = (TextView)mealItem.findViewById(R.id.meal_item_list_time);
					TextView carbView = (TextView)mealItem.findViewById(R.id.meal_item_list_carbs);

					mealItem.setTag(meal);
					time.setText(DateFormat.format("MMM, dd kk:mm", meal.getDateEaten()));
					for (MealToFood f : meal.mealToFoods) {
						if (f.food.name.equals(food.name)) {
							carbView.setText(String.valueOf(f.food.carbs));
							break;
						}
					}

					mealsLayout.addView(mealItem);
				}
			});
		}
	}

	private void setupBloodSugarPlot(List<Meal> meals) {
		final BloodSugarPlotHandler graphHandler = new BloodSugarPlotHandler(plotView);
		plotView.getSettings().setJavaScriptEnabled(true);
		plotView.addJavascriptInterface(graphHandler, "Android");
		plotView.loadUrl("file:///android_asset/blood_sugar_plots/stats_graph.html");

		getBloodSugarDataForMeal(meals, graphHandler);	
	}

	private void setupView(String foodName) {
		foodNameView.setText(foodName);
	}

	private void getBloodSugarDataForMeal(List<Meal> meals, final BloodSugarPlotHandler plotHandler) {
		new AsyncTask<Meal, Void, Void>() {

			@Override
			protected Void doInBackground(Meal... params) {
				for (Meal meal : params) {
					BloodSugarDataResults results = new BloodSugarDataResults(null, null, RequestIdUtil.getNewId(), true);
					while (results.hasMoreResults) {
						results = MeterDataUtil.getBloodSugarDataForHoursFromDate(meal.getDateEaten(),
								3, results.requestId);
						plotHandler.updateData(
								results.sensorData, results.meterData, null);
						plotHandler.loadGraph();
					}
				}

				return null;
			}

		}.execute(meals.toArray(new Meal[meals.size()]));
	}

}
