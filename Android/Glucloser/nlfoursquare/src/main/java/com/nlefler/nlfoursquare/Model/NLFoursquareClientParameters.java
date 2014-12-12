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

    public NLFoursquareClientParameters(String clientID, String clientSecret) {
        _clientID = clientID;
        _clientSecret = clientSecret;
    }

    public String clientID() {
        return _clientID;
    }

    public String clientSecret() {
        return _clientSecret;
    }

    public boolean isValid() {
        return !_clientID.isEmpty() && !_clientSecret.isEmpty();
    }

    public Map<String, String> authenticationParameters() {
        if (!this.isValid()) {
            return new HashMap<>();
        }

        return new HashMap<String, String>() {{
            put("client_id", _clientID);
            put("client_secret", _clientSecret);
            put("v", _clientAPIVersion);
        }};
    }
}
