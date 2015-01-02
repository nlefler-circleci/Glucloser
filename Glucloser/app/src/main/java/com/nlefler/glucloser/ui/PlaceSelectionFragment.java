package com.nlefler.glucloser.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nlefler.glucloser.R;
import com.nlefler.glucloser.actions.LogMealAction;
import com.nlefler.glucloser.dataSource.PlaceSelectionRecyclerAdapter;
import com.nlefler.glucloser.foursquare.FoursquarePlaceHelper;
import com.nlefler.glucloser.models.Place;
import com.nlefler.glucloser.models.PlaceSelectionDelegate;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceSelectionFragment
        extends Fragment
        implements Observer<List<NLFoursquareVenue>>,
        PlaceSelectionDelegate {
    private static String LOG_TAG = "PlaceSelectionFragment";


    private FoursquarePlaceHelper foursquareHelper;
    private Subscription closestPlacesSubscription;
    private Scheduler subscriptionScheduler;
    private LogMealAction logMealAction;

    private RecyclerView placeSelectionList;
    private PlaceSelectionRecyclerAdapter placeSelectionAdapter;
    private RecyclerView.LayoutManager placeSelectionLayoutManager;

    public PlaceSelectionFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        this.setHasOptionsMenu(true);

        foursquareHelper = new FoursquarePlaceHelper(getActivity());
        subscriptionScheduler = Schedulers.newThread();
        getClosestPlaces(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_place, container, false);
        this.placeSelectionList = (RecyclerView)rootView.findViewById(R.id.place_selection_list);

        this.placeSelectionLayoutManager = new LinearLayoutManager(getActivity());
        this.placeSelectionAdapter = new PlaceSelectionRecyclerAdapter(this,
                new ArrayList<NLFoursquareVenue>());

        this.placeSelectionList.setLayoutManager(this.placeSelectionLayoutManager);
        this.placeSelectionList.setAdapter(this.placeSelectionAdapter);
        this.placeSelectionList.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_place_selection, menu);
        MenuItem searchItem = menu.findItem(R.id.action_place_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getClosestPlaces(searchView.getQuery().toString());
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getClosestPlaces(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        closestPlacesSubscription.unsubscribe();
    }

    /** Observer<T> */
    @Override
    public void onCompleted() {
        this.closestPlacesSubscription.unsubscribe();
    }

    @Override
    public void onError(Throwable e) {
        // TODO: Show UI
        Log.e(LOG_TAG, "Unable to get places from 4sq " + e.toString());
        getActivity().finish();
    }

    @Override
    public void onNext(List<NLFoursquareVenue> nlFoursquareVenues) {
        this.placeSelectionAdapter.setVenues(nlFoursquareVenues);
    }

    /** PlaceSelectionDelegate */
    public void placeSelected(Place place) {
        if (!(getActivity() instanceof PlaceSelectionDelegate)) {
            return;
        }
        ((PlaceSelectionDelegate)getActivity()).placeSelected(place);
    }

    /** Helpers */
    private void getClosestPlaces(String searchTerm) {
        if (closestPlacesSubscription != null) {
            closestPlacesSubscription.unsubscribe();
        }
        closestPlacesSubscription = AndroidObservable.bindFragment(this,
                foursquareHelper.closestVenues(searchTerm))
                .subscribeOn(subscriptionScheduler)
                .subscribe(this);
    }
}
