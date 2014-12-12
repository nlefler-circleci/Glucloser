package com.nlefler.nlfoursquare.Model.Venue.Explore;

import java.util.List;

/**
 * Created by lefler on 6/19/14.
 */
public class NLFoursquareVenueExploreResponse {
    public List<String> keywords;
    public NLFoursquareVenueExploreWarning warning;
    public List<NLFoursquareVenueExploreGroup> groups;
    public float suggestedRadius;
    public String headerLocation;
    public String headerFullLocation;
    public String headerMessage;
}
