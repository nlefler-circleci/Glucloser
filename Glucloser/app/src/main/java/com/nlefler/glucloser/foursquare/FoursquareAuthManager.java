package com.nlefler.glucloser.foursquare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;

import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;
import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.nlefler.glucloser.GlucloserApplication;
import com.nlefler.glucloser.R;
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters;

import java.io.IOException;

/**
 * Created by Nathan Lefler on 12/28/14.
 */
public class FoursquareAuthManager {
    private static final String LOG_TAG = "FoursquareAuthManager";

    private static final String SHARED_PREFS_NAME = "com.nlefler.glucloser.foursquareprefs";
    private static final String SHARED_PREFS_4SQ_TOKEN_KEY = "com.nlefler.glucloser.4sqtkn";
    private static final String CONCEAL_ENTITY_NAME = "com.nlefler.glucloser.concealentity";

    public static final int FOURSQUARE_CONNECT_INTENT_CODE = 39228;
    public static final int FOURSQUARE_TOKEN_EXCHG_INTENT_CODE = 39229;

    private Crypto crypto;
    private String _userAccessToken = "";

    private static FoursquareAuthManager _sharedInstance;
    public static FoursquareAuthManager SharedManager() {
        if (_sharedInstance == null) {
            _sharedInstance = new FoursquareAuthManager();
        }
        return _sharedInstance;
    }

    private FoursquareAuthManager() {
        Context ctx = GlucloserApplication.SharedApplication().getApplicationContext();
        this.crypto = new Crypto(
                new SharedPrefsBackedKeyChain(ctx),
                new SystemNativeCryptoLibrary());

        this._userAccessToken = this.getDecryptedAuthToken();
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
            this.encryptAndStoreAuthToken(managingActivity, this._userAccessToken);

            (new FoursquareUserHelper()).fetchAndStoreUserId();
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

    private void encryptAndStoreAuthToken(Context ctx, String token) {
        if (!this.crypto.isAvailable() || token == null || token.isEmpty()) {
            return;
        }
        Entity entity = new Entity(CONCEAL_ENTITY_NAME);
        try {
            byte[] encryptedToken = this.crypto.encrypt(token.getBytes(), entity);
            String encryptedBase64Token = Base64.encodeToString(encryptedToken, Base64.DEFAULT);
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(SHARED_PREFS_NAME,
                Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SHARED_PREFS_4SQ_TOKEN_KEY, encryptedBase64Token);
            editor.apply();
        } catch (KeyChainException | CryptoInitializationException | IOException e) {
            e.printStackTrace();
        }
    }

    private String getDecryptedAuthToken() {
        if (!this.crypto.isAvailable()) {
            return "";
        }
        try {
            SharedPreferences sharedPref = GlucloserApplication.SharedApplication()
                    .getApplicationContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            String encryptedBase64Token = sharedPref.getString(SHARED_PREFS_4SQ_TOKEN_KEY, "");
            Entity entity = new Entity(CONCEAL_ENTITY_NAME);
            byte[] encryptedToken = Base64.decode(encryptedBase64Token, Base64.DEFAULT);

            if (encryptedToken.length > 0) {
                return new String(this.crypto.decrypt(encryptedToken, entity), "UTF-8");
            } else {
                return "";
            }
        } catch (KeyChainException | CryptoInitializationException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
