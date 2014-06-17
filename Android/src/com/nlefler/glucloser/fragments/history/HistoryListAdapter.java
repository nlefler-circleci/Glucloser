package com.nlefler.glucloser.fragments.history;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.nlefler.glucloser.detail.MealDetailActivity;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.meal.MealUtil;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.place.PlaceUtil;
import com.nlefler.glucloser.util.RequestIdUtil;
import com.nlefler.glucloser.R;

public class HistoryListAdapter extends BaseAdapter implements ListAdapter {
	private static final String LOG_TAG = "Pump_History_List_Adapter";

	private AsyncTask<Integer, List<Meal>, List<Meal>> dataSource;

	private List<Meal> meals;
	private Context context;
	private LayoutInflater inflater;

	public HistoryListAdapter(Context c, LayoutInflater l) {
		// TODO Correctly handle no max meals
		this(c, l, 1000);
	}

	public HistoryListAdapter(Context c, LayoutInflater l, int maxMeals) {
		context = c;
		inflater = l;
		meals = new ArrayList<Meal>();

		dataSource = new AsyncTask<Integer, List<Meal>, List<Meal>>() {
			@Override
			protected List<Meal> doInBackground(Integer... maxMealsToReturn) {
				List<Meal> meals = MealUtil.getRecentMeals(maxMealsToReturn[0]);
                return meals;
			}		

			@Override
			protected void onPostExecute(List<Meal> result) {
				meals = result;
				notifyDataSetChanged();

				super.onPostExecute(result);
			}

		};
		dataSource.execute(maxMeals);
	}

	@Override
	public int getCount() {
		return meals.size();
	}

	@Override
	public Object getItem(int position) {
		return meals.get(position);
	}

	@Override
	public long getItemId(int position) {
		Meal meal = meals.get(position);
		if (meal != null) {
			return meal.hashCode();
		}
		return 0;
	}

	@Override
	public View getView(int position, View reuseView, ViewGroup parent) {
		final Meal meal = meals.get(position);

		LinearLayout theView = null;
		final long requestId = RequestIdUtil.getNewId();

		if (reuseView == null || reuseView.getId() != R.layout.history_list_item) {
			theView = (LinearLayout)inflater.inflate(R.layout.history_list_item, null);
		} else {
			theView = (LinearLayout) reuseView;
		}

		TextView name = (TextView)theView.findViewById(R.id.history_list_item_name);
		TextView time = (TextView)theView.findViewById(R.id.history_list_item_time);
		theView.setTag(requestId);

        // TODO: Thread
        Place place = meal.getPlace();
		name.setText(place.name);
		time.setText(meal.getDateEatenForDisplay());

		theView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (requestId == (Long)v.getTag()) {
					Intent detailIntent = new Intent(context, MealDetailActivity.class);
					detailIntent.putExtra(MealDetailActivity.MEAL_KEY, meal);
					context.startActivity(detailIntent);
				}
			}
		});

		return theView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		if (position >= meals.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}

        // TODO: What is this
        return true;
	}
}
