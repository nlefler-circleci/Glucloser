package com.nlefler.glucloser.util;

import android.location.Location;
import android.util.Log;

import com.nlefler.glucloser.R;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.place.PlaceUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.nlfoursquare.Explore.NLFoursquareVenueExplore;
import com.nlefler.nlfoursquare.Explore.NLFoursquareVenueExploreParametersBuilder;
import com.nlefler.nlfoursquare.Explore.NLFoursquareVenueExploreSection;
import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse;
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters;
import com.nlefler.nlfoursquare.Model.Venue.Explore.NLFoursquareVenueExploreGroup;
import com.nlefler.nlfoursquare.Model.Venue.Explore.NLFoursquareVenueExploreGroupRecommendedItem;
import com.nlefler.nlfoursquare.Model.Venue.Explore.NLFoursquareVenueExploreResponse;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.emilsjolander.sprinkles.Query;

/**
 * Created by nathan on 8/1/14.
 */
public class FoursquareUtil {

    private static class DataExtractor<T> {
        public List<T> extract(List<NLFoursquareVenue> venues) {
            return new ArrayList<T>();
        }
    }

    private static final DataExtractor<String> extractNames = new DataExtractor<String>() {
        @Override
        public List<String> extract(List<NLFoursquareVenue> venues) {
            List<String> names = new ArrayList<String>();
            for (NLFoursquareVenue venue : venues) {
                names.add(venue.name);
            }
            return names;
        }
    };

    public static void placeNamesNearCurrentLocation(final NLFoursquareClientParameters clientParameters,
                                                     final com.nlefler.glucloser.util.Callback<List<String>> callback) {
        foursquareVenuesNearCurrentLocation(clientParameters, callback, extractNames);
    }

    private static final DataExtractor<Place> extractPlaces = new DataExtractor<Place>() {
        @Override
        public List<Place> extract(List<NLFoursquareVenue> venues) {
            List<Place> places = new ArrayList<Place>();
            for (NLFoursquareVenue venue : venues) {
                Place place = PlaceUtil.getPlaceWithFoursquareId(venue.id);
                if (place == null) {
                    place = new Place();
                    place.foursquareId = venue.id;
                    place.name = venue.name;
                }
                places.add(place);
            }

            return places;
        }
    };

    public static void placesNearCurrentLocation(final NLFoursquareClientParameters clientParameters,
                                                 final com.nlefler.glucloser.util.Callback<List<Place>> callback) {
        foursquareVenuesNearCurrentLocation(clientParameters, callback, extractPlaces);
    }

    private static <T> void foursquareVenuesNearCurrentLocation(final NLFoursquareClientParameters clientParameters,
                                                     final com.nlefler.glucloser.util.Callback<List<T>> callback,
                                                     final DataExtractor<T> dataExtractor) {
        Location location = LocationUtil.getLastKnownLocation();
        if (location != null) {
            NLFoursquareVenueExploreParametersBuilder paramsBuilder = new NLFoursquareVenueExploreParametersBuilder();
            paramsBuilder.latLon(location.getLatitude(), location.getLongitude());
            paramsBuilder.section(NLFoursquareVenueExploreSection.NLFoursquareVenueExploreSectionFood);
            paramsBuilder.limit(10);

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("https://api.foursquare.com/v2")
                    .build();

            NLFoursquareVenueExplore exploreEndpoint = restAdapter.create(NLFoursquareVenueExplore.class);
            exploreEndpoint.explore(paramsBuilder.buildWithClientParameters(clientParameters),
                    new Callback<NLFoursquareResponse<NLFoursquareVenueExploreResponse>>() {
                        @Override
                        public void success(NLFoursquareResponse<NLFoursquareVenueExploreResponse> foursquareResponse,
                                            Response response) {
                            List<NLFoursquareVenue> venues = new ArrayList<NLFoursquareVenue>();
                            for (NLFoursquareVenueExploreGroup group : foursquareResponse.response.groups) {
                                for (NLFoursquareVenueExploreGroupRecommendedItem item : group.items) {
                                    venues.add(item.venue);
                                }
                            }
                            callback.call(dataExtractor.extract(venues));
                        }

                        @Override
                        public void failure(RetrofitError retrofitError) {
                            callback.error("");
                        }
                    }
            );
        }
    }
}
