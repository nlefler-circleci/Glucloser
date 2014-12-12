package com.nlefler.nlfoursquare.Explore;

import com.nlefler.nlfoursquare.Common.NLFoursquareEndpointParametersBuilder;


/**
 * Created by Nathan Lefler on 6/19/14.
 */
public class NLFoursquareVenueExploreParametersBuilder extends NLFoursquareEndpointParametersBuilder {

    public boolean validate() {
        return _params.containsKey("ll") || _params.containsKey("near");
    }

    /*!
    * Latitude and Longitude of the user's location. Required unless
    * 'near' is provided.
    */
    public NLFoursquareVenueExploreParametersBuilder latLon(double lat, double lon) {
        _params.put("ll", String.valueOf(lat) + "," + String.valueOf(lon));

        return this;
    }

    /*!
     * The name of a place which is geocodable at which the search will be anchored.
     * Required unless latitude and longitude are provided.
     */
    public NLFoursquareVenueExploreParametersBuilder near(String near) {
        if (near == null) {
            return this;
        }
        _params.put("near", near);

        return this;
    }

    /*!
     * Accuracy of latitude and longitude, in meters.
     */
    public NLFoursquareVenueExploreParametersBuilder latLonAccuracy(double acc) {
        _params.put("llAcc", String.valueOf(acc));

        return this;
    }

    /*!
     * Altitude of the user's location, in meters.
     */
    public NLFoursquareVenueExploreParametersBuilder altitude(double alt) {
        _params.put("alt", String.valueOf(alt));

        return this;
    }

    /*!
     * Accuracy of the user's altitude, in meters.
     */
    public NLFoursquareVenueExploreParametersBuilder altitudeAccuracy(double acc) {
        _params.put("altAcc", String.valueOf(acc));

        return this;
    }

    /*!
     * Limits results to within this this many meters of the anchor point.
     */
    public NLFoursquareVenueExploreParametersBuilder radius(double radius) {
        _params.put("radius", String.valueOf("radius"));

        return this;
    }

    /*!
     * One of @ref NLFoursquareVenueExploreSection. Limits results to venues with the specified
     * category or property.
     */
    public NLFoursquareVenueExploreParametersBuilder section(NLFoursquareVenueExploreSection section) {
        String sectionValue = section.toString();
        if (sectionValue != null) {
            _params.put("section", sectionValue);
        }

        return this;
    }

    /*!
     * A search term to be applied against venue names.
     */
    public NLFoursquareVenueExploreParametersBuilder query(String query) {
        if (query == null || query.isEmpty()) {
            return this;
        }
        _params.put("query", query);

        return this;
    }

    /*!
     * The number of results to return. Capped at 50.
     */
    public NLFoursquareVenueExploreParametersBuilder limit(int limit) {
        // Capped by Foursquare
        _params.put("limit", String.valueOf(limit));

        return this;
    }

    /*!
     * Used to page.
     */
    public NLFoursquareVenueExploreParametersBuilder offset(int offset) {
        _params.put("offset", String.valueOf(offset));

        return this;
    }

    /*!
     * Pass @ref NLFoursquareVenueExploreNovelty.NLFoursquareVenueExploreNoveltyNew to limit
     * results to places the user hasn't been to, @ref
     * NLFoursquareVenueExploreNovelty.NLFoursquareVenueExploreNoveltyOld for places the user has
     * been to. Omitting this value returns a mixture.
     */
    public NLFoursquareVenueExploreParametersBuilder novelty(NLFoursquareVenueExploreNovelty novelty) {
        String noveltyValue = novelty.toString();
        if (noveltyValue != null) {
            _params.put("novelty", noveltyValue);
        }

        return this;
    }

    /*!
     * Pass @ref NLFoursquareVenueExploreFriendVisits.NLFoursquareVenueExploreFriendVisitsVisited
     * to limit results to places the user's friends have been to, @ref
     * NLFoursquareVenueExploreFriendVisits.NLFoursquareVenueExploreFriendVisitsVisited for places
     * the user's friends have not visited. Omitting this value returns a mixture.
     */
    public NLFoursquareVenueExploreParametersBuilder friendVisits(NLFoursquareVenueExploreFriendVisits visits) {
        String visitValue = visits.toString();
        if (visitValue != null) {
            _params.put("friendVisits", visitValue);
        }

        return this;
    }

    /*!
     * Pass @ref NLFoursquareVenueExploreTime.NLFoursquareVenueExploreTimeAny to retrieve results for
     * any time of day. Omitting this value limits results to the current time of day.
     */
    public NLFoursquareVenueExploreParametersBuilder time(NLFoursquareVenueExploreTime time) {
        String timeValue = time.toString();
        if (timeValue != null) {
            _params.put("time", timeValue);
        }

        return this;
    }

    /*!
     * Pass @ref NLFoursquareVenueExploreDay.NLFoursquareVenueExploreDayAny to retrieve results for
     * any day of the week. Omitting this value limits results to the current day of the week.
     */
    public NLFoursquareVenueExploreParametersBuilder day(NLFoursquareVenueExploreDay day) {
        String dayValue = day.toString();
        if (dayValue != null) {
            _params.put("day", dayValue);
        }

        return this;
    }

    /*!
     * Include a photo for each venue, if available.
     */
    public NLFoursquareVenueExploreParametersBuilder includeVenuePhotos(boolean includePhotos) {
        _params.put("venuePhotos", includePhotos ? "1" : "0");

        return this;
    }

    /*!
     * A venue Id to use in combination with @ref
     * NLFoursquareVenueExploreSection.NLFoursquareVenueExploreSectionNextVenues.
     */
    public NLFoursquareVenueExploreParametersBuilder lastVenue(String venueId) {
        if (venueId != null) {
            _params.put("lastVenue", venueId);
        }

        return this;
    }

    /*!
     * Limit results to venues which are currently open.
     */
    public NLFoursquareVenueExploreParametersBuilder openNow(boolean open) {
        _params.put("openNow", open ? "1" : "0");

        return this;
    }

    /*!
     * Sort results by distance instead of relevance.
     */
    public NLFoursquareVenueExploreParametersBuilder sortByDistance(boolean byDistance) {
        _params.put("sortByDistance", byDistance ? "1" : "0");

        return this;
    }

    /*!
     * Limit results to venues matching the provided price points.
     * @param pricePoints A mask of @ref NLFoursquareVenueExplorePricePoint
     */
    public NLFoursquareVenueExploreParametersBuilder price(NLFoursquareVenueExplorePricePoint[] pricePoints) {
        String priceValue = "";
        for (int idx = 0; idx < pricePoints.length; ++idx) {
            String pointValue = pricePoints[idx].toString();
            if (pointValue != null) {
                priceValue += pointValue;
                if (idx < pricePoints.length - 1) {
                    priceValue += ",";
                }
            }
        }
        if (!priceValue.isEmpty()) {
            _params.put("price", priceValue);
        }

        return this;
    }

    /*!
     * Limit results to venues the user has saved on a list.
     */
    public NLFoursquareVenueExploreParametersBuilder saved(boolean saved) {
        _params.put("saved", saved ? "1" : "0");

        return this;
    }

    /*!
     * Limit results to venues with a special.
     */
    public NLFoursquareVenueExploreParametersBuilder specials(boolean specialsOnly) {
        _params.put("specials", specialsOnly ? "1" : "0");

        return this;
    }
}
