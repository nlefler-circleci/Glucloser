package com.nlefler.nlfoursquare.Explore;

/**
 * Created by lefler on 6/19/14.
 */
public enum NLFoursquareVenueExploreDay {
    NLFoursquareVenueExploreDayAny;

    public String toString() {
        String dayValue = null;
        switch (this) {
            case NLFoursquareVenueExploreDayAny:
            {
                dayValue = "any";
                break;
            }
        }
        return dayValue;
    }
}
