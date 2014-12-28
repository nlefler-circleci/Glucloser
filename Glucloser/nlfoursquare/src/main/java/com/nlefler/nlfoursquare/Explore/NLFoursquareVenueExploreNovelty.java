package com.nlefler.nlfoursquare.Explore;

/**
 * Created by lefler on 6/19/14.
 */
public enum NLFoursquareVenueExploreNovelty {
    NLFoursquareVenueExploreNoveltyNew,
    NLFoursquareVenueExploreNoveltyOld;

    public String toString() {
        String noveltyValue = null;
        switch (this) {
            case NLFoursquareVenueExploreNoveltyNew:
            {
                noveltyValue = "new";
                break;
            }
            case NLFoursquareVenueExploreNoveltyOld:
            {
                noveltyValue = "old";
                break;
            }
        }
        return noveltyValue;
    }
}
