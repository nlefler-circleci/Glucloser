package com.nlefler.nlfoursquare.Common;

import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lefler on 6/19/14.
 */
public abstract class NLFoursquareEndpointParametersBuilder {
    protected Map<String, String> _params;

    public NLFoursquareEndpointParametersBuilder() {
        _params = new HashMap<String, String>();
    }

    public boolean validate() {
        return false;
    }

    /*!
     * Builds the query parameters map for a @ref NLFoursquareVenueSearch call.
     */
    public Map<String, String> buildWithClientParameters(NLFoursquareClientParameters clientParams) {
        Map<String, String> fullParams = new HashMap<String, String>();
        // TODO: Error if invalid parameters
        fullParams.putAll(clientParams.authenticationParameters());
        fullParams.putAll(_params);

        return fullParams;
    }
}
