package com.nlefler.nlfoursquare.Explore;

/**
 * Created by lefler on 6/19/14.
 */
public enum NLFoursquareVenueExploreSection {
    NLFoursquareVenueExploreSectionFood,
    NLFoursquareVenueExploreSectionDrinks,
    NLFoursquareVenueExploreSectionCoffee,
    NLFoursquareVenueExploreSectionShops,
    NLFoursquareVenueExploreSectionArts,
    NLFoursquareVenueExploreSectionOutdoors,
    NLFoursquareVenueExploreSectionSights,
    NLFoursquareVenueExploreSectionTrending,
    NLFoursquareVenueExploreSectionSpecials,
    NLFoursquareVenueExploreSectionNextVenues,
    NLFoursquareVenueExploreSectionTopPicks;

    public String toString() {
        String sectionValue = null;
        switch (this) {
            case NLFoursquareVenueExploreSectionFood:
            {
                sectionValue = "food";
                break;
            }
            case NLFoursquareVenueExploreSectionDrinks:
            {
                sectionValue = "drinks";
                break;
            }
            case NLFoursquareVenueExploreSectionCoffee:
            {
                sectionValue = "coffee";
                break;
            }
            case NLFoursquareVenueExploreSectionShops:
            {
                sectionValue = "shops";
                break;
            }
            case NLFoursquareVenueExploreSectionArts:
            {
                sectionValue = "arts";
                break;
            }
            case NLFoursquareVenueExploreSectionOutdoors:
            {
                sectionValue = "outdoors";
                break;
            }
            case NLFoursquareVenueExploreSectionSights:
            {
                sectionValue = "sights";
                break;
            }
            case NLFoursquareVenueExploreSectionTrending:
            {
                sectionValue = "trending";
                break;
            }
            case NLFoursquareVenueExploreSectionSpecials:
            {
                sectionValue = "specials";
                break;
            }
            case NLFoursquareVenueExploreSectionNextVenues:
            {
                sectionValue = "nextVenues";
                break;
            }
            case NLFoursquareVenueExploreSectionTopPicks:
            {
                sectionValue = "topPicks";
                break;
            }
        }
        return sectionValue;
    }
}

