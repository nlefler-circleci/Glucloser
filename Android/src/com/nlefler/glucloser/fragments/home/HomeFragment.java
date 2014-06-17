package com.nlefler.glucloser.fragments.home;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.nlefler.glucloser.GlucloserActivity;
import com.nlefler.glucloser.detail.MealDetailActivity;
import com.nlefler.glucloser.detail.PlaceDetailActivity;
import com.nlefler.glucloser.fragments.add.AddMealFragment;
import com.nlefler.glucloser.fragments.home.listItems.HistoricMealItem;
import com.nlefler.glucloser.fragments.home.listItems.HomeListItem;
import com.nlefler.glucloser.fragments.home.listItems.PlaceListItem;
import com.nlefler.glucloser.fragments.home.listItems.PopularMealListItem;
import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.util.LocationUtil;
import com.nlefler.glucloser.model.meal.MealUtil;
import com.nlefler.glucloser.R;

public class HomeFragment extends ListFragment {
	private static final String LOG_TAG = "Glucloser_Home_Fragment";

	private HomeListAdapter listAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		listAdapter = new HomeListAdapter(getActivity().getLayoutInflater());
		setListAdapter(listAdapter);
		
		listAdapter.fetchData(LocationUtil.getLastKnownLocation());
		LocationUtil.addLocationListener(new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
				listAdapter.fetchData(location);
			}
		});
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		return inflater.inflate(R.layout.home_view, container, false);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		HomeListItem item = (HomeListItem) getListView().getItemAtPosition(position);

		if (item instanceof PopularMealListItem) {
            PopularMealListItem popItem = (PopularMealListItem)item;
			Bundle args = new Bundle();

			args.putSerializable(AddMealFragment.MEAL_KEY,
                    setupMealWithPlaceAndFoods(popItem.getPlace(), popItem.getFoods()));

            ((GlucloserActivity)getActivity()).pushFragment(new AddMealFragment(), args);
		} else if (item instanceof HistoricMealItem) {
            final HistoricMealItem historicItem = (HistoricMealItem)item;

			// Show the action popup
			// 1) View Meal Details
			// 2) Use For New Meal

			// Keep this in syc with the OnClickListener
			CharSequence[] actions = new CharSequence[] {
					"View Meal Details",
					"Use For New Meal"
			};

			final DialogFragment actionFragment = 
					new ActionDialogFrament(actions,
							new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
							case 0:
								showDetails();
								break;
							case 1:
								addNewMeal();
								break;
							default:
								showDetails();
							}
						}

						private void showDetails() {
							Intent detailIntent = new Intent(getActivity(), MealDetailActivity.class);
							detailIntent.putExtra(MealDetailActivity.MEAL_KEY, historicItem.getMeal());
							getActivity().startActivity(detailIntent);
						}

						private void addNewMeal() {
							Bundle args = new Bundle();
														
							ArrayList<Food> foods = new ArrayList<Food>();
							// TODO: Is this running in a thread?
							for (Food food : historicItem.getMeal().getFoods()) {
								foods.add(food);
							}
							Meal meal = setupMealWithPlaceAndFoods(historicItem.getPlace(), foods);
							args.putSerializable(AddMealFragment.MEAL_KEY, meal);

                            ((GlucloserActivity)getActivity()).pushFragment(new AddMealFragment(), args);
						}

					});
			actionFragment.show(getActivity().getFragmentManager(), 
					"meal_action_dialog");
		} else if (item instanceof PlaceListItem) {
            final PlaceListItem placeItem = (PlaceListItem)item;

			// Show the action popup
			// 1) View Place Details
			// 2) Use For New Meal

			// Keep this in sycn with the OnClickListener
			CharSequence[] actions = new CharSequence[] {
					"View Place Details",
					"Use For New Meal"
			};

			final DialogFragment actionFragment = 
					new ActionDialogFrament(actions,
							new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							switch (which) {
							case 0:
								showDetails();
								break;
							case 1:
								addNewMeal();
								break;
							default:
								showDetails();
							}
						}

						private void showDetails() {
							Intent detailIntent = new Intent(getActivity(), PlaceDetailActivity.class);
							detailIntent.putExtra(PlaceDetailActivity.PLACE_EXTRA_KEY, placeItem.getPlace());
							getActivity().startActivity(detailIntent);
						}

						private void addNewMeal() {
							Bundle args = new Bundle();
							
							args.putSerializable(AddMealFragment.MEAL_KEY,
                                    setupMealWithPlaceAndFoods(placeItem.getPlace(), new ArrayList<Food>()));

                            ((GlucloserActivity)getActivity()).pushFragment(new AddMealFragment(), args);
						}

					});
			actionFragment.show(getActivity().getFragmentManager(), 
					"meal_action_dialog");
		}
	}

	public void search(String searchTerm) {
		// getLastKnownLocation may be null, but it's better than doing nothing
		// while waiting for location and appearing to lag after the user
		// presses search
		listAdapter.fetchDataWithTerm(LocationUtil.getLastKnownLocation(), searchTerm);
	}

	private Meal setupMealWithPlaceAndFoods(Place place, List<Food> foods) {
		Meal meal = new Meal();
		
        meal.setPlace(place);
		
		for (Food food : foods) {
			meal.addFood(food);
		}
		return meal;
	}
}
