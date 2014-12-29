package com.nlefler.glucloser.foursquare;

import android.util.Log;

import com.nlefler.glucloser.GlucloserApplication;
import com.nlefler.nlfoursquare.Common.NLFoursquareEndpoint;
import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse;
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters;
import com.nlefler.nlfoursquare.Model.User.NLFoursquareUserInfoResponse;
import com.nlefler.nlfoursquare.Users.NLFoursquareUserInfo;
import com.parse.ParseInstallation;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Nathan Lefler on 12/29/14.
 */
public class FoursquareUserHelper {
    private static String LOG_TAG = "FoursquareUserHelper";

    private static final String PARSE_INSTALLATION_FOURSQUARE_USER_ID_KEY = "foursquareUserId";

    private RestAdapter restAdapter;

    public FoursquareUserHelper() {
        this.restAdapter = new RestAdapter.Builder()
                .setEndpoint(NLFoursquareEndpoint.NLFOURSQUARE_V2_ENDPOINT)
                .build();
    }
    public void fetchAndStoreUserId() {
        NLFoursquareUserInfo info = this.restAdapter.create(NLFoursquareUserInfo.class);
        NLFoursquareClientParameters authParams = FoursquareAuthManager.SharedManager().getClientAuthParameters(
                GlucloserApplication.SharedApplication().getApplicationContext());
        info.getInfo(authParams.authenticationParameters(),
                NLFoursquareUserInfo.UserIdSelf,
                new Callback<NLFoursquareResponse<NLFoursquareUserInfoResponse>>() {
                    @Override
                    public void success(NLFoursquareResponse<NLFoursquareUserInfoResponse> nlFoursquareUserInfoResponseNLFoursquareResponse, Response response) {
                        String userId = nlFoursquareUserInfoResponseNLFoursquareResponse.response.user.id;
                        if (userId == null || userId.isEmpty()) {
                            Log.e(LOG_TAG, "Unable to get Foursquare user id");
                            return;
                        }
                        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                        installation.put(PARSE_INSTALLATION_FOURSQUARE_USER_ID_KEY, userId);
                        installation.saveInBackground();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(LOG_TAG, "Unable to get Foursquare user id");
                        Log.e(LOG_TAG, error.getBody().toString());
                    }
                });

    }
}
