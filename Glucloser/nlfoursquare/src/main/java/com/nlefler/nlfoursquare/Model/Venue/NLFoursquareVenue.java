package com.nlefler.nlfoursquare.Model.Venue;

import java.net.URL;
import java.util.List;

/**
 * Created by Nathan Lefler on 5/19/14.
 */
public class NLFoursquareVenue {
    public String id;
    public String name;
    public NLFoursquareVenueContact contact;
    public NLFoursquareLocation location;
    public List<NLFoursquareCategory> categories;
    public boolean verified;
    public NLFoursquareVenueStats stats;

    // The following values may be null
    public URL url;
    public NLFoursquareVenueHours hours;
    public NLFoursquareVenueHours popular;
    public NLFoursquareVenueMenu menu;
    public NLFoursquareVenuePrice price;
    public float rating;
    public NLFoursquareVenueSpecials specials;
    public NLFoursquareVenueHereNow hereNow;
    public String storeId;
    public String description;
    public double createdAt;
    public NLFoursquareVenueTips tips;
    // TODO: listed field
    public List<String> tags;
    public NLFoursquareVenueBeenHere beenHere;
    public URL shortUrl;
    public URL canonicalUrl;
    public List<NLFoursquareVenueSpecial> specialsNearby;
    // TODO: photos field
    // TODO: likes field
    public boolean like;
    public boolean dislike;
    // TODO: roles field
    // TODO: flags field
    // TODO: page field
}
