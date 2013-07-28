package com.hagia.glucloser.fragments.home;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hagia.glucloser.types.Food;
import com.hagia.glucloser.types.Meal;
import com.hagia.glucloser.types.MealToFood;
import com.hagia.glucloser.types.Place;
import com.hagia.glucloser.types.PlaceToMeal;
import com.hagia.glucloser.util.FoodUtil;
import com.hagia.glucloser.util.LocationUtil;
import com.hagia.glucloser.util.MealUtil;
import com.hagia.glucloser.util.PlaceUtil;
import com.hagia.glucloser.R;

public class HomeListAdapter extends BaseAdapter implements ListAdapter {
	private static final String LOG_TAG = "Pump_Home_List_Adapter";

	private static final int MOST_POPULAR_MEAL_LIMIT = 3;
	private static final float MIN_DISTANCE_CHANGE_FOR_SEARCH = 5;

	private AsyncTask<String, SortedMap<Place, List<List<Food>>>,
	SortedMap<Place, List<List<Food>>>> popularMealDataSource;
	private AsyncTask<String, SortedMap<Place, List<Meal>>,
	SortedMap<Place, List<Meal>>> historicMealDataSource;
	private AsyncTask<String, SortedMap<Place, Integer>, SortedMap<Place, Integer>>
	matchingPlaceDataSource;

	private Comparator<Place> placeDistanceComparator = null;

	private SortedMap<Place, Integer> placeResults = null;
	private SortedMap<Place, List<List<Food>>> popularMealResults = null;
	private SortedMap<Place, List<Meal>> historicMealResults = null;
	private int popularMealResultsCount = 0;
	private int historicMealResultsCount = 0;
	private LayoutInflater inflater;

	private Location lastSearchLocation = null;
	private String lastSearchTerm = null;

	public HomeListAdapter(LayoutInflater l) {
		inflater = l;
	}

	public void fetchData(Location currentLocation) {
		fetchDataWithTerm(currentLocation, null);
	}

	public void fetchDataWithTerm(Location currentLocation, String term) {
		if (term != null && term.equals("")) {
			Log.v(LOG_TAG, "Search term is the empty string, ignoring it");
			term = null;
		}

		Log.v(LOG_TAG, "Starting fetch for Home data with location " +
				currentLocation + ", term " + term);
		// Make sure we have some location
		if (currentLocation == null) {
			currentLocation = new Location(LocationUtil.NO_PROVIDER);
			currentLocation.setLatitude(0);
			currentLocation.setLongitude(0);
		}

		// If the search params haven't noticeably changed, don't
		// redo the search
		if (lastSearchLocation != null &&
				lastSearchLocation.distanceTo(currentLocation) < MIN_DISTANCE_CHANGE_FOR_SEARCH &&
				((lastSearchTerm == null && term == null) ||
						lastSearchTerm != null && lastSearchTerm.equals(term))) {
			Log.v(LOG_TAG, "Search parameters haven't changed, aborting search");
			return;
		}

		lastSearchLocation = currentLocation;
		lastSearchTerm = term;

		// Create the comparator so we can order places based
		// on the relative distance to the user
		final Location currentLocationPointer = currentLocation;
		placeDistanceComparator = new Comparator<Place>() {

			@Override
			public int compare(Place lhs, Place rhs) {
				return (int) (lhs.location.distanceTo(currentLocationPointer) -
						rhs.location.distanceTo(currentLocationPointer));
			}
		};

		cancel();

		popularMealDataSource = constructPopularMealFetchDataTask();
		popularMealDataSource.execute(term);
		historicMealDataSource = constructHistoricMealFetchDataTask();
		historicMealDataSource.execute(term);
		matchingPlaceDataSource = constructMatchingPlaceFetchDataTask();
		matchingPlaceDataSource.execute(term);
	}

	public void cancel() {
		if (popularMealDataSource != null) {
			popularMealDataSource.cancel(true);
		}
		if (historicMealDataSource != null) {
			historicMealDataSource.cancel(true);
		}
		if (matchingPlaceDataSource != null) {
			matchingPlaceDataSource.cancel(true);
		}
		popularMealResults = null;
		historicMealResults = null;
		placeResults = null;

		popularMealResultsCount = 0;
		historicMealResultsCount = 0;

		notifyDataSetInvalidated();
	}

	@Override
	public int getCount() {
		int count = popularMealResultsCount + historicMealResultsCount;

		if (placeResults != null) {
			count += placeResults.size();
		}

		return count;
	}

	@Override
	public Object getItem(int position) {
		long start = System.currentTimeMillis();
		ResultContainer container = new ResultContainer();

		if (popularMealResults != null) {
			if (position < popularMealResultsCount) {
				Place place = null;
				List<List<Food>> meals = null;
				List<Food> foods = null;
				Iterator<Place> it = popularMealResults.keySet().iterator();
				while (it.hasNext()) {
					place = it.next();
					meals = popularMealResults.get(place);
					if (position >= meals.size()) {
						position -= meals.size();
						continue;
					}
					Iterator<List<Food>> foodsIt = meals.iterator();
					for (; position >= 0; --position) {
						foods = foodsIt.next();
					}
					break;
				}
				container.placeAndFoods = new Object[] {place, foods};
				Log.v(LOG_TAG, "Popular get took " + (System.currentTimeMillis() - start));
				return container;
			} else {
				position -= popularMealResultsCount;
			}
		} else 	if (historicMealResults != null) {
			if (position < historicMealResultsCount) {
				Place place = null;
				List<Meal> meals = null;
				Meal meal = null;
				Iterator<Place> it = historicMealResults.keySet().iterator();
				while (it.hasNext()) {
					place = it.next();
					meals = historicMealResults.get(place);
					if (position >= meals.size()) {
						position -= meals.size();
						continue;
					}
					Iterator<Meal> mealIt = meals.iterator();
					for (; position >= 0; --position) {
						meal = mealIt.next();
					}
					break;
				}
				container.placeAndMeal = new Object[] {place, meal};
				Log.v(LOG_TAG, "Historic get took " + (System.currentTimeMillis() - start));

				return container;
			} else {
				position -= historicMealResultsCount;
			}
		}

		if (placeResults != null) {
			if (position < placeResults.size()) {
				Place place = null;
				Iterator<Place> it = placeResults.keySet().iterator();
				for (; position >= 0; --position) {
					place = it.next();
				}
				container.placeAndMealCount = 
						new Object[] {place, placeResults.get(place)};
				Log.v(LOG_TAG, "Place get took " + (System.currentTimeMillis() - start));

				return container;
			} else {
				position -= placeResults.size();
			}
		}

		Log.v(LOG_TAG, "Returning null for item");
		return null;
	}

	@Override
	public long getItemId(int position) {
		ResultContainer container = (ResultContainer) getItem(position);

		if (container.placeAndFoods != null) {
			return container.placeAndFoods[1].hashCode();
		} else if (container.placeAndMeal != null) {
			return container.placeAndMeal[1].hashCode();
		} else if (container.placeAndMealCount != null) {
			return container.placeAndMealCount[0].hashCode();
		}

		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ResultContainer container = (ResultContainer) getItem(position);
		if (container == null) {
			return null;
		}

		if (container.placeAndFoods != null) {
			return getViewForPopularMeal((Place)container.placeAndFoods[0],
					(List<Food>)container.placeAndFoods[1], convertView, parent);
		} else if (container.placeAndMeal != null) {
			return getViewForHistoricMeal((Place)container.placeAndMeal[0],
					(Meal)container.placeAndMeal[1], convertView, parent);
		} else if (container.placeAndMealCount != null) {
			return getViewForPlace((Place)container.placeAndMealCount[0],
					(Integer)container.placeAndMealCount[1], convertView, parent);
		}

		Log.v(LOG_TAG, "Returning null for view");
		return null;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	private AsyncTask<String, SortedMap<Place, List<List<Food>>>, SortedMap<Place, List<List<Food>>>>
	constructPopularMealFetchDataTask() {
		return new AsyncTask<String, SortedMap<Place, List<List<Food>>>, SortedMap<Place, List<List<Food>>>>() {
			@Override
			// String searchTerm
			protected SortedMap<Place, List<List<Food>>> doInBackground(String... params) {
				long start = System.currentTimeMillis();
				SortedMap<Place, List<List<Food>>> meals = 
						new TreeMap<Place, List<List<Food>>>(placeDistanceComparator);

				String searchTerm = null;
				if (params.length > 0) {
					searchTerm =(String)params[0];
				}

				List<Place> places = new ArrayList<Place>();

				if (searchTerm != null) {
					places.addAll(PlaceUtil.getAllPlacesWithNameContaining(searchTerm));
					Log.v(LOG_TAG, "Got " + places.size() +
							" places matching search term '" + searchTerm + "'");
				} else {
					Log.v(LOG_TAG, "No search term for popular places, using closest place");
					long clos = System.currentTimeMillis();
					Place closestPlace = LocationUtil.getClosestPlace(lastSearchLocation);
					if (closestPlace != null) {
						places.add(closestPlace);
					} else {
						Log.v(LOG_TAG, "Couldn't get closest place");
					}
					Log.v(LOG_TAG, "Closget took " + (System.currentTimeMillis() - clos));
				}
				for (Place place : places) {
					// Get popular meals
					long get = System.currentTimeMillis();
					List<List<Food>> mealsForPlace = 
							PlaceUtil.getMostPopularMealsForPlace(place, MOST_POPULAR_MEAL_LIMIT);
					Log.v(LOG_TAG, "Popget took " + (System.currentTimeMillis() - get));
					Log.d(LOG_TAG, "Found " + mealsForPlace.size() +
							" meals for place (" + place.name + ")");
					if (mealsForPlace.isEmpty()) {
						continue;
					}

					if (!meals.containsKey(place)) {
						meals.put(place, new ArrayList<List<Food>>());
					}

					meals.get(place).addAll(mealsForPlace);
					popularMealResultsCount += mealsForPlace.size();
					
					// Get address
					if (place.readableAddress == null) {
						PlaceUtil.updateReadableLocation(place);
						(new AsyncTask<Place, Void, Void>() {

							@Override
							protected Void doInBackground(Place... params) {
								if (params.length > 0) {
									PlaceUtil.savePlace(params[0]);
								}
								return null;
							}

						}).execute(place);
					}
				}
				Log.d(LOG_TAG, "Popular meals took " + (System.currentTimeMillis() - start));
				return meals;
			}		

			@Override
			protected void onPostExecute(final SortedMap<Place, List<List<Food>>> result) {
				popularMealResults = result;
				notifyDataSetChanged();

				super.onPostExecute(result);
			}

		};
	}

	private AsyncTask<String, SortedMap<Place, List<Meal>>, SortedMap<Place, List<Meal>>>
	constructHistoricMealFetchDataTask() {
		return new AsyncTask<String, SortedMap<Place, List<Meal>>, SortedMap<Place, List<Meal>>>() {
			@Override
			// String searchTerm
			protected SortedMap<Place, List<Meal>> doInBackground(String... params) {
				long start = System.currentTimeMillis();
				String searchTerm = null;
				if (params.length > 0) {
					searchTerm =(String)params[0];
				}

				SortedMap<Place, List<Meal>> meals = 
						new TreeMap<Place, List<Meal>>(placeDistanceComparator);

				if (searchTerm != null && !searchTerm.isEmpty()) {
					List<Meal> foundMeals = FoodUtil.getAllMealsForFoodNameContaining(searchTerm);
					Log.d(LOG_TAG, "Found " + foundMeals.size() + " meals matching " + searchTerm);
					for (Meal meal : foundMeals) {
						PlaceToMeal p2m = MealUtil.getPlaceForMeal(meal);
						if (p2m == null || p2m.place == null) {
							continue;
						}
						if (!meals.containsKey(p2m.place)) {
							meals.put(p2m.place, new ArrayList<Meal>());
						}
						meals.get(p2m.place).add(meal);
						historicMealResultsCount += 1;
					}
				}
				Log.d(LOG_TAG, "Historic meals took " + (System.currentTimeMillis() - start));

				return meals;
			}		

			@Override
			protected void onPostExecute(final SortedMap<Place, List<Meal>> result) {
				historicMealResults = result;
				notifyDataSetChanged();

				super.onPostExecute(result);
			}

		};
	}

	private AsyncTask<String, SortedMap<Place, Integer>, SortedMap<Place, Integer>>
	constructMatchingPlaceFetchDataTask() {
		return new AsyncTask<String, SortedMap<Place, Integer>, SortedMap<Place, Integer>>() {
			@Override
			// String searchTerm
			protected SortedMap<Place, Integer> doInBackground(String... params) {
				long start = System.currentTimeMillis();
				String searchTerm = null;
				if (params.length > 0) {
					searchTerm = (String)params[0];
				}

				SortedMap<Place, Integer> places = 
						new TreeMap<Place, Integer>(placeDistanceComparator);
				if (searchTerm != null) {
					List<Place> foundPlaces = PlaceUtil.getAllPlacesWithNameContaining(searchTerm);
					Log.d(LOG_TAG, "Found " + foundPlaces.size() + " places matching term " + searchTerm);
					for (Place place : foundPlaces) {
						places.put(place, PlaceUtil.getNumberOfMealsForPlace(place));
					}
				} else {
					List<Place> nearbyPlaces = PlaceUtil.getPlacesNear(lastSearchLocation);
					for (Place place : nearbyPlaces) {
						// Get total meal count
						places.put(place,
								PlaceUtil.getNumberOfMealsForPlace(place));
					}
				}
				Log.d(LOG_TAG, "Places took " + (System.currentTimeMillis() - start));

				return places;
			}		

			@Override
			protected void onPostExecute(final SortedMap<Place, Integer> result) {
				placeResults = result;
				notifyDataSetChanged();

				super.onPostExecute(result);
			}

		};
	}

	private View getViewForPopularMeal(Place place, List<Food> foods, 
			View convertView, ViewGroup parent) {
		long start = System.currentTimeMillis();
		LinearLayout theView = null;

		if (place == null || foods == null) {
			return convertView;
		}

		double requestId = place.location.getLatitude() +
				place.location.getLongitude() + 
				foods.hashCode();

		if (convertView == null || convertView.getId() != R.layout.home_line_item) {
			theView = (LinearLayout)inflater.inflate(R.layout.home_line_item, null);
		} else {
			theView = (LinearLayout) convertView;
			if (((Double)theView.getTag()) == requestId) {
				return theView;
			}
		}

		if (place == null || foods == null) {
			return theView;
		}

		LinearLayout mealItem = (LinearLayout)inflater.inflate(
				R.layout.popular_meal_line_item, null);
		theView.addView(mealItem);

		theView.setTag(requestId);


		TextView placeName = (TextView) mealItem.findViewById(R.id.popular_meal_place_name);
		TextView placeAddress = (TextView) mealItem.findViewById(R.id.popular_meal_place_address);
		LinearLayout foodsLayout = (LinearLayout) mealItem.findViewById(R.id.popular_meal_foods_container);

		placeName.setText(place.name);

		if (place.readableAddress != null) {
			placeAddress.setText(place.readableAddress);
		}

		for (Food food : foods) {
			RelativeLayout foodLayout = (RelativeLayout) inflater.inflate(
					R.layout.food_line_item, null);
			TextView foodName = (TextView) foodLayout.findViewById(R.id.food_line_item_food_name);
			TextView carbsValue = (TextView) foodLayout.findViewById(R.id.food_line_item_carbs_value);

			foodName.setText(food.name);
			carbsValue.setText(String.valueOf(food.carbs));
			foodsLayout.addView(foodLayout);
		}

		Log.d(LOG_TAG, "Popular meal view took " + (System.currentTimeMillis() - start));

		return theView;
	}

	private View getViewForHistoricMeal(Place place, Meal meal, 
			View convertView, ViewGroup parent) {
		long start = System.currentTimeMillis();
		LinearLayout theView = null;

		if (place == null || meal == null) {
			return convertView;
		}

		double requestId = place.location.getLatitude() +
				place.location.getLongitude() +
				+ meal.dateEaten.getTime();

		if (convertView == null || convertView.getId() != R.layout.home_line_item) {
			theView = (LinearLayout)inflater.inflate(R.layout.home_line_item, null);
		} else {
			theView = (LinearLayout) convertView;
			if (((Double)theView.getTag()) == requestId) {
				return theView;
			}
		}
		LinearLayout mealItem = (LinearLayout)inflater.inflate(
				R.layout.popular_meal_line_item, null);
		theView.addView(mealItem);

		theView.setTag(requestId);


		TextView placeName = (TextView) mealItem.findViewById(R.id.popular_meal_place_name);
		// TODO: Stop using the for the date
		TextView placeAddress = (TextView) mealItem.findViewById(R.id.popular_meal_place_address);
		LinearLayout foodsLayout = (LinearLayout) mealItem.findViewById(R.id.popular_meal_foods_container);

		placeName.setText(place.name);
		placeAddress.setText(meal.getDateEatenForDisplay());

		List<Food> foods = new ArrayList<Food>();
		// TODO: Is this running in a thread?
		for (MealToFood m2f : MealUtil.getFoodsForMeal(meal)) {
			Food food = m2f.food;
			foods.add(food);

			RelativeLayout foodLayout = (RelativeLayout) inflater.inflate(
					R.layout.food_line_item, null);
			TextView foodName = (TextView) foodLayout.findViewById(R.id.food_line_item_food_name);
			TextView carbsValue = (TextView) foodLayout.findViewById(R.id.food_line_item_carbs_value);

			foodName.setText(food.name);
			carbsValue.setText(String.valueOf(food.carbs));
			foodsLayout.addView(foodLayout);
		}

		Log.d(LOG_TAG, "Historic meal view took " + (System.currentTimeMillis() - start));

		return theView;
	}

	private View getViewForPlace(Place place, int mealCount, View convertView, ViewGroup parent) {		
		long start = System.currentTimeMillis();
		LinearLayout theView = null;

		if (place == null) {
			return convertView;
		}

		// TODO: Probably not unique
		double requestId = place.name.hashCode() + 
				place.location.getLatitude() + 
				place.location.getLongitude() + 
				mealCount;

		if (convertView == null || convertView.getId() != R.layout.home_line_item) {
			theView = (LinearLayout)inflater.inflate(R.layout.home_line_item, null);
		} else {
			theView = (LinearLayout) convertView;
			if (((Double)theView.getTag()) == requestId) {
				return theView;
			}
		}
		LinearLayout placeItem = (LinearLayout)inflater.inflate(
				R.layout.place_line_item, null);
		theView.addView(placeItem);

		theView.setTag(requestId);


		TextView nameView = (TextView)placeItem.findViewById(R.id.place_line_item_place_name);
		TextView addressView = (TextView)placeItem.findViewById(R.id.place_line_item_place_address);
		TextView mealCountView = (TextView)placeItem.findViewById(R.id.place_line_item_place_meal_count);

		nameView.setText(place.name);

		String readableAddress = place.readableAddress;
		if (readableAddress == null || readableAddress.equals("")) {
			List<Address> addresses = LocationUtil.getAddressFromLocation(place.location, 1);
			if (!addresses.isEmpty()) {
				// TODO drill up until we find a non-null address line value
				addressView.setText(addresses.get(0).getAddressLine(0));
				// TODO This is a temporary workaround
				PlaceUtil.updateReadableLocation(place);
				PlaceUtil.savePlace(place);
			}
		}

		if (mealCount >= 0) {
			String meals = mealCount > 1 ? " Meals" : " Meal";
			mealCountView.setText(mealCount + meals);
		}

		Log.d(LOG_TAG, "Place view took " + (System.currentTimeMillis() - start));

		return theView;
	}

	protected class ResultContainer {
		// Place, List<Food>
		protected Object[] placeAndFoods;
		// Place, Meal
		protected Object[] placeAndMeal;
		// Place, Integer
		protected Object[] placeAndMealCount;
	}
}
