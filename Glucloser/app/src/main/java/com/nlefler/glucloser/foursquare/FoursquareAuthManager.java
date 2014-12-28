package com.nlefler.glucloser.foursquare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.nlefler.glucloser.R;
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters;

/**
 * Created by Nathan Lefler on 12/28/14.
 */
public class FoursquareAuthManager {
    private static final String LOG_TAG = "FoursquareAuthManager";

    public static final int FOURSQUARE_CONNECT_INTENT_CODE = 39228;
    public static final int FOURSQUARE_TOKEN_EXCHG_INTENT_CODE = 39229;

    private String _userAccessToken = "";

    private static FoursquareAuthManager _sharedInstance;
    public static FoursquareAuthManager SharedManager() {
        if (_sharedInstance == null) {
            _sharedInstance = new FoursquareAuthManager();
        }
        return _sharedInstance;
    }

    public void startAuthRequest(Activity managingActivity) {
        Intent intent = FoursquareOAuth.getConnectIntent(managingActivity,
                managingActivity.getString(R.string.foursquare_app_id));
        managingActivity.startActivityForResult(intent, FOURSQUARE_CONNECT_INTENT_CODE);
    }

    public void gotAuthResponse(Activity managingActivity,
                                int responseCode, Intent responseData) {
        AuthCodeResponse codeResponse = FoursquareOAuth.getAuthCodeFromResult(responseCode, responseData);
        if (codeResponse.getException() == null) {
            Intent intent = FoursquareOAuth.getTokenExchangeIntent(managingActivity,
                    managingActivity.getString(R.string.foursquare_app_id),
                    managingActivity.getString(R.string.foursquare_app_secret),
                    codeResponse.getCode());
            managingActivity.startActivityForResult(intent, FOURSQUARE_TOKEN_EXCHG_INTENT_CODE);
        }
    }

    public void gotTokenExchangeResponse(Activity managingActivity,
                                         int responseCode, Intent responseData) {
        AccessTokenResponse tokenResponse = FoursquareOAuth.getTokenFromResult(responseCode, responseData);
        if (tokenResponse.getException() == null) {
            _userAccessToken = tokenResponse.getAccessToken();
        }
    }

    public NLFoursquareClientParameters getClientAuthParameters(Context ctx) {
        String appId = ctx.getString(R.string.foursquare_app_id);
        String appSecret = ctx.getString(R.string.foursquare_app_secret);

        NLFoursquareClientParameters params = new NLFoursquareClientParameters(appId);
        if (!this._userAccessToken.isEmpty()) {
            params.setUserOAuthToken(this._userAccessToken);
        } else {
            params.setClientSecret(appSecret);
        }

        return params;
    }
}
