package com.nlefler.nlfoursquare.Explore;

/**
 * Created by lefler on 6/19/14.
 */
public enum NLFoursquareVenueExplorePricePoint {
    NLFoursquareVenueExplorePricePoint1,
    NLFoursquareVenueExplorePricePoint2,
    NLFoursquareVenueExplorePricePoint3,
    NLFoursquareVenueExplorePricePoint4;

    public String toString() {
        String priceValue = null;
        switch (this) {
            case NLFoursquareVenueExplorePricePoint1:
            {
                priceValue = "1";
                break;
            }
            case NLFoursquareVenueExplorePricePoint2:
            {
                priceValue = "2";
                break;
            }
            case NLFoursquareVenueExplorePricePoint3:
            {
                priceValue = "3";
                break;
            }
            case NLFoursquareVenueExplorePricePoint4:
            {
                priceValue = "4";
                break;
            }
        }
        return priceValue;
    }
}
