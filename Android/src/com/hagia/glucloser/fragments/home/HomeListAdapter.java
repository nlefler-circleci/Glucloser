package com.hagia.glucloser.fragments.home;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.hagia.glucloser.fragments.home.listItems.HistoricMealItem;
import com.hagia.glucloser.fragments.home.listItems.HomeListItem;
import com.hagia.glucloser.fragments.home.listItems.PlaceListItem;
import com.hagia.glucloser.fragments.home.listItems.PopularMealListItem;
import com.hagia.glucloser.types.Food;
import com.hagia.glucloser.types.Meal;
import com.hagia.glucloser.types.Place;
import com.hagia.glucloser.util.FoodUtil;
import com.hagia.glucloser.util.LocationUtil;
import com.hagia.glucloser.util.PlaceUtil;

public class HomeListAdapter extends BaseAdapter implements ListAdapter {
	private static final String LOG_TAG = "Glucloser_Home_List_Adapter";

	private static final int MOST_POPULAR_MEAL_LIMIT = 3;
	private static final float MIN_DISTANCE_CHANGE_FOR_SEARCH = 5;

	private AsyncTask<String, SortedSet<PopularMealListItem>, SortedSet<PopularMealListItem>> popularMealDataSource;
	private AsyncTask<String, SortedSet<HistoricMealItem>, SortedSet<HistoricMealItem>> historicMealDataSource;
	private AsyncTask<String, SortedSet<PlaceListItem>, SortedSet<PlaceListItem>> matchingPlaceDataSource;

	private Comparator<HomeListItem> placeDistanceComparator = null;

	private SortedSet<PlaceListItem> placeResults = null;
	private SortedSet<PopularMealListItem> popularMealResults = null;
	private SortedSet<HistoricMealItem> historicMealResults = null;
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
			Log.v(LOG_TAG, "Search term is empty, ignoring it");
			term = null;
		}

		Log.v(LOG_TAG, "Starting fetch for Home data with location " +
				currentLocation + ", term " + term);

		// If the search params haven't noticeably changed, don't
		// redo the search
        boolean locationChanged = currentLocation != null && lastSearchLocation != null &&
				lastSearchLocation.distanceTo(currentLocation) < MIN_DISTANCE_CHANGE_FOR_SEARCH;
        boolean termChanged = (lastSearchTerm != term) ||
                (lastSearchTerm != null && !lastSearchTerm.equals(term));
		if (!locationChanged && !termChanged) {
			Log.v(LOG_TAG, "Search parameters haven't changed, aborting search");
			return;
		}

		lastSearchLocation = currentLocation;
		lastSearchTerm = term;

		// Create the comparator so we can order places based
		// on the relative distance to the user
        if (currentLocation != null)
        {
            final Location currentLocationPointer = currentLocation;
            placeDistanceComparator = new Comparator<HomeListItem>() {
                @Override
                public int compare(HomeListItem lhs, HomeListItem rhs) {
                    if (lhs == null || lhs.getPlace() == null) {
                        return 1;
                    }
                    if (rhs == null || rhs.getPlace() == null) {
                        return -1;
                    }
                    return (int) (lhs.getPlace().location.distanceTo(currentLocationPointer) -
                            lhs.getPlace().location.distanceTo(currentLocationPointer));
                }
            };
		} else {
            placeDistanceComparator = new Comparator<HomeListItem>() {
                @Override
                public int compare(HomeListItem lhs, HomeListItem rhs) {
                    return -1;
                }
            };
        }

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

		notifyDataSetInvalidated();
	}

	@Override
	public int getCount() {
		int count = 0;

        if (popularMealResults != null) {
            count += popularMealResults.size();
        }
		if (placeResults != null) {
			count += placeResults.size();
		}
        if (historicMealResults != null) {
            count += historicMealResults.size();
        }

		return count;
	}

	@Override
	public Object getItem(int position) {
		if (popularMealResults != null) {
			if (position < popularMealResults.size()) {
                Iterator<PopularMealListItem> iterator = popularMealResults.iterator();
                for (int i = 0; i < position; ++i) {
                    iterator.next();
                }
                return iterator.next();
			} else {
				position -= popularMealResults.size();
			}
		}
        if (historicMealResults != null) {
			if (position < historicMealResults.size()) {
                Iterator<HistoricMealItem> iterator = historicMealResults.iterator();
                for (int i = 0; i < position; ++i) {
                    iterator.next();
                }
                return iterator.next();
			} else {
				position -= historicMealResults.size();
			}
		}
        if (placeResults != null) {
			if (position < placeResults.size()) {
				Iterator<PlaceListItem> iterator = placeResults.iterator();
                for (int i = 0; i < position; ++i) {
                    iterator.next();
                }
                return iterator.next();
			} else {
				position -= placeResults.size();
			}
		}

        Log.e(LOG_TAG, "Invalid item position, don't have enough results");
        return null;
	}

	@Override
	public long getItemId(int position) {
		HomeListItem item = (HomeListItem)getItem(position);
        return item.getItemId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        HomeListItem item = (HomeListItem)getItem(position);
        return item.getView(inflater, convertView, parent);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	private AsyncTask<String, SortedSet<PopularMealListItem>, SortedSet<PopularMealListItem>>
	constructPopularMealFetchDataTask() {
		return new AsyncTask<String, SortedSet<PopularMealListItem>, SortedSet<PopularMealListItem>>() {
			@Override
			// String searchTerm
			protected SortedSet<PopularMealListItem> doInBackground(String... params) {
				String searchTerm = null;
				if (params.length > 0) {
					searchTerm = (String)params[0];
				}

				SortedSet<PopularMealListItem> meals = new TreeSet<PopularMealListItem>(placeDistanceComparator);

                List<Place> foundPlaces = new ArrayList<Place>();
				if (searchTerm != null) {
					foundPlaces = PlaceUtil.getAllPlacesWithNameContaining(searchTerm);
				} else {
					Place closestPlace = PlaceUtil.getClosestPlace();
					if (closestPlace != null) {
						foundPlaces.add(closestPlace);
					}
				}
				for (Place place : foundPlaces) {
					// Get popular meals
					List<List<Food>> mealsForPlace =
							PlaceUtil.getMostPopularMealsForPlace(place, MOST_POPULAR_MEAL_LIMIT);
                    for (List<Food> foods : mealsForPlace) {
                        meals.add(new PopularMealListItem(place, foods));
                    }
                }

				return meals;
			}		

			@Override
			protected void onPostExecute(final SortedSet<PopularMealListItem> result) {
				popularMealResults = result;
				notifyDataSetChanged();
			}

		};
	}

	private AsyncTask<String, SortedSet<HistoricMealItem>, SortedSet<HistoricMealItem>>
	constructHistoricMealFetchDataTask() {
		return new AsyncTask<String, SortedSet<HistoricMealItem>, SortedSet<HistoricMealItem>>() {
			@Override
			protected SortedSet<HistoricMealItem> doInBackground(String... params) {
				String searchTerm = null;
				if (params.length > 0) {
					searchTerm = (String)params[0];
				}
                if (searchTerm == null || searchTerm.isEmpty()) {
                    return new TreeSet<HistoricMealItem>();
                }

                SortedSet<HistoricMealItem> meals = new TreeSet<HistoricMealItem>(placeDistanceComparator);

                List<Meal> foundMeals = FoodUtil.getAllMealsForFoodNameContaining(searchTerm);
                for (Meal meal : foundMeals) {
                    meals.add(new HistoricMealItem(meal));
                }

				return meals;
			}		

			@Override
			protected void onPostExecute(final SortedSet<HistoricMealItem> result) {
				historicMealResults = result;
				notifyDataSetChanged();
			}

		};
	}

	private AsyncTask<String, SortedSet<PlaceListItem>, SortedSet<PlaceListItem>> constructMatchingPlaceFetchDataTask() {
		return new AsyncTask<String, SortedSet<PlaceListItem>, SortedSet<PlaceListItem>> () {
			@Override
			protected SortedSet<PlaceListItem> doInBackground(String... params) {
				String searchTerm = null;
				if (params.length > 0) {
					searchTerm = (String)params[0];
				}

                SortedSet<PlaceListItem> places = new TreeSet<PlaceListItem>(placeDistanceComparator);
                List<Place> foundPlaces = null;
				if (searchTerm != null) {
					foundPlaces = PlaceUtil.getAllPlacesWithNameContaining(searchTerm);
				} else {
					foundPlaces = PlaceUtil.getPlacesNear(lastSearchLocation);
				}
                for (Place place : foundPlaces) {
                    places.add(new PlaceListItem(place, PlaceUtil.getNumberOfMealsForPlace(place)));
                }

				return places;
			}		

			@Override
			protected void onPostExecute(final SortedSet<PlaceListItem> result) {
				placeResults = result;
				notifyDataSetChanged();
			}

		};
	}
}
