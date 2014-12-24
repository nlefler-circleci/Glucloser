package com.nlefler.glucloser.dataSource;

import com.nlefler.glucloser.models.Place;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceFactory {
    public static Place FromFoursquareVenue(NLFoursquareVenue venue) {
        Place place = new Place();
        place.setName(venue.name);
        place.setFoursquareId(venue.id);
        place.setLatitude(venue.location.lat);
        place.setLongitude(venue.location.lng);

        return place;
    }
}
