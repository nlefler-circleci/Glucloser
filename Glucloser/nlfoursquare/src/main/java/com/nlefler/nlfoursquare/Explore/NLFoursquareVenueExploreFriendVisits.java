package com.nlefler.nlfoursquare.Explore;

/**
 * Created by lefler on 6/19/14.
 */
public enum NLFoursquareVenueExploreFriendVisits {
    NLFoursquareVenueExploreFriendVisitsVisited,
    NLFoursquareVenueExploreFriendVisitsNotVisited;

    public String toString() {
        String visitedValue = null;
        switch (this) {
            case NLFoursquareVenueExploreFriendVisitsVisited:
            {
                visitedValue = "visited";
                break;
            }
            case NLFoursquareVenueExploreFriendVisitsNotVisited:
            {
                visitedValue = "notvisited";
                break;
            }
        }
        return visitedValue;
    }
}
