package com.nlefler.nlfoursquare.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nathan Lefler on 5/19/14.
 */
public class NLFoursquareClientParameters {
    private String _clientID;
    private String _clientSecret;
    private String _clientAPIVersion = "20140518";
    private String _clientMode = "foursquare";
    private String _userOAuthToken = "";

    public NLFoursquareClientParameters(String clientID) {
        _clientID = clientID;
    }

    public String clientID() {
        return _clientID;
    }

    public void setClientSecret(String clientSecret) {
        this._clientSecret = clientSecret;
    }

    public void setUserOAuthToken(String token) {
        this._userOAuthToken = token;
    }

    public boolean isValid() {
        return (this._clientID != null && !this._clientID.isEmpty()) &&
                ((this._clientSecret != null && !this._clientSecret.isEmpty()) ||
                        (this._userOAuthToken != null && !this._userOAuthToken.isEmpty()));
    }

    public Map<String, String> authenticationParameters() {
        if (!this.isValid()) {
            return new HashMap<>();
        }

        Map<String, String> params = new HashMap<String, String>() {{
            put("client_id", _clientID);
            put("v", _clientAPIVersion);
            put("m", _clientMode);
        }};
        if (this._userOAuthToken != null && !this._userOAuthToken.isEmpty()) {
            params.put("oauth_token", this._userOAuthToken);
        } else if (this._clientSecret != null && !this._clientSecret.isEmpty()) {
            params.put("client_secret", _clientSecret);
        }

        return params;
    }
}
