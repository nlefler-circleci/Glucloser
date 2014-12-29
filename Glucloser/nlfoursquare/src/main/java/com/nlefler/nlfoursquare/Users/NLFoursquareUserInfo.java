package com.nlefler.nlfoursquare.Users;

import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse;
import com.nlefler.nlfoursquare.Model.User.NLFoursquareUserInfoResponse;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.QueryMap;

/**
 * Created by Nathan Lefler on 12/29/14.
 */
public interface NLFoursquareUserInfo {
    public static final String UserIdSelf = "self";

    @GET("/users/{userId}")
    void getInfo(@QueryMap Map<String, String> parameters,
                 @Path("userId") String userId,
                Callback<NLFoursquareResponse<NLFoursquareUserInfoResponse>> callback);
}
