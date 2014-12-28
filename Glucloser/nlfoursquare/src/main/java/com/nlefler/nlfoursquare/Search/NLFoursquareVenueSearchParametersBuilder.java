package com.nlefler.nlfoursquare.Search;

import com.nlefler.nlfoursquare.Common.NLFoursquareEndpointParametersBuilder;
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nathan Lefler on 5/19/14.
 */
public class NLFoursquareVenueSearchParametersBuilder extends NLFoursquareEndpointParametersBuilder {

    public boolean validate() {
        return _params.containsKey("ll") || _params.containsKey("near");
    }

    /*!
     * Latitude and Longitude to anchor the search to. Required unless
     * 'near' is provided.
     */
    public NLFoursquareVenueSearchParametersBuilder latLon(double lat, double lon) {
        _params.put("ll", String.valueOf(lat) + "," + String.valueOf(lon));

        return this;
    }

    /*!
     * The name of a place which is geocodable at which the search will be anchored.
     * Required unless latitude and longitude are provided.
     */
    public NLFoursquareVenueSearchParametersBuilder near(String near) {
        if (near == null) {
            return this;
        }
        _params.put("near", near);

        return this;
    }

    /*!
     * Accuracy of latitude and longitude, in meters.
     */
    public NLFoursquareVenueSearchParametersBuilder latLonAccuracy(double acc) {
        _params.put("llAcc", String.valueOf(acc));

        return this;
    }

    /*!
     * Altitude of the user's location, in meters.
     */
    public NLFoursquareVenueSearchParametersBuilder altitude(double alt) {
       _params.put("alt", String.valueOf(alt));

        return this;
    }

    /*!
     * Accuracy of the user's altitude, in meters.
     */
    public NLFoursquareVenueSearchParametersBuilder altitudeAccuracy(double acc) {
        _params.put("altAcc", String.valueOf(acc));

        return this;
    }

    /*!
     * A search term to be applied against venue names.
     */
    public NLFoursquareVenueSearchParametersBuilder query(String query) {
        if (query == null || query.isEmpty()) {
            return this;
        }
        _params.put("query", query);

        return this;
    }

    /*!
     * The number of results to return. Capped at 50.
     */
    public NLFoursquareVenueSearchParametersBuilder limit(int limit) {
        // Capped by Foursquare
        _params.put("limit", String.valueOf(limit));

        return this;
    }

    /*!
     * One of @ref NLFoursquareVenueSearchIntent.
     */
    public NLFoursquareVenueSearchParametersBuilder intent(NLFoursquareVenueSearchIntent intent) {
        String intentValue = "";
        switch (intent) {
            case NLFoursquareVenueSearchIntentCheckIn:
            {
                intentValue = "checkin";
                break;
            }
            case NLFoursquareVenueSearchIntentBrowse:
            {
                intentValue = "browse";
                break;
            }
            case NLFoursquareVenueSearchIntentGlobal:
            {
                intentValue = "global";
                break;
            }
            case NLFoursquareVenueSearchIntentMatch:
            {
                intentValue = "match";
                break;
            }
        }
        if (intentValue.isEmpty()) {
            return this;
        }
        _params.put("intent", intentValue);

        return this;
    }

    /*!
     * Limits results to within this this many meters of the anchor point.
     */
    public NLFoursquareVenueSearchParametersBuilder radius(double radius) {
        _params.put("radius", String.valueOf(radius));

        return this;
    }

    /*!
     * With @ref northEast the limit of a bounding box.
     */
    public NLFoursquareVenueSearchParametersBuilder southWest(double lat, double lon) {
        _params.put("sw", String.valueOf(lat) + "," + String.valueOf(lon));

        return this;
    }

    /*!
     * With @ref southWest the limit of a bounding box.
     */
    public NLFoursquareVenueSearchParametersBuilder northEast(double lat, double lon) {
        _params.put("ne", String.valueOf("lat") + "," + String.valueOf("lon"));

        return this;
    }

    /*!
     * A list of categories to limit results to.
     */
    public NLFoursquareVenueSearchParametersBuilder limitToCategories(List<String> categories) {
        if (categories == null) {
            return this;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String category : categories) {
            if (category.isEmpty()) {
                continue;
            }
            stringBuilder.append(category).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        _params.put("categoryId", stringBuilder.toString());

        return this;
    }

    /*!
     * A third party URL to match against venue URLs.
     */
    public NLFoursquareVenueSearchParametersBuilder url(String url) {
        if (url == null || url.isEmpty()) {
            return this;
        }
        _params.put("url", url);

        return this;
    }

    /*!
     * Identifier for a known third party.
     */
    public NLFoursquareVenueSearchParametersBuilder providerId(String id) {
        if (id == null || id.isEmpty()) {
            return this;
        }
        _params.put("providerId", id);

        return this;
    }

    /*!
     * Identifier used by the third party specified by @ref providerId.
     */
    public NLFoursquareVenueSearchParametersBuilder linkedId(String id) {
        if (id == null | id.isEmpty()) {
            return this;
        }
        _params.put("linkedId", id);

        return this;
    }
}
