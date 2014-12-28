package com.nlefler.nlfoursquare.Explore;

import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse;
import com.nlefler.nlfoursquare.Model.Venue.Explore.NLFoursquareVenueExploreResponse;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

/**
 * Created by lefler on 6/19/14.
 */
public interface NLFoursquareVenueExplore {
    @GET("/venues/explore")
    void explore(@QueryMap Map<String, String> parameters,
                Callback<NLFoursquareResponse<NLFoursquareVenueExploreResponse>> callback);
}
