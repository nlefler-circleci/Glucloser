package com.nlefler.nlfoursquare.Explore;

/**
 * Created by lefler on 6/19/14.
 */
public enum NLFoursquareVenueExploreTime {
    NLFoursquareVenueExploreTimeAny;

    public String toString() {
        String timeValue = null;
        switch (this) {
            case NLFoursquareVenueExploreTimeAny:
            {
                timeValue = "any";
                break;
            }
        }
        return timeValue;
    }
}
