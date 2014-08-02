package com.nlefler.glucloser.util;

import android.location.Location;
import android.util.Log;

import com.nlefler.glucloser.R;
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

/**
 * Created by nathan on 8/1/14.
 */
public class FoursquareUtil {
    public static void placeNamesNearCurrentLocation(final NLFoursquareClientParameters clientParameters,
                                                     final com.nlefler.glucloser.util.Callback<List<String>> callback) {
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
                            List<String> foursquarePlaces = new ArrayList<String>();
                            for (NLFoursquareVenueExploreGroup group : foursquareResponse.response.groups) {
                                for (NLFoursquareVenueExploreGroupRecommendedItem item : group.items) {
                                    NLFoursquareVenue venue = item.venue;
                                    foursquarePlaces.add(venue.name);
                                }
                            }
                            callback.call(foursquarePlaces);
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
