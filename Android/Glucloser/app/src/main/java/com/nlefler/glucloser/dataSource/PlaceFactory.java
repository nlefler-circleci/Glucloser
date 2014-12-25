package com.nlefler.glucloser.dataSource;

import com.nlefler.glucloser.models.Place;
import com.nlefler.glucloser.models.PlaceParcelable;
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

    public static PlaceParcelable ParcelableFromPlace(Place place) {
        PlaceParcelable parcelable = new PlaceParcelable();
        parcelable.setName(place.getName());
        parcelable.setFoursquareId(place.getFoursquareId());
        parcelable.setLatitude(place.getLatitude());
        parcelable.setLongitude(place.getLongitude());

        return parcelable;
    }

    public static Place PlaceFromParcelable(PlaceParcelable parcelable) {
        Place place = new Place();
        place.setName(parcelable.getName());
        place.setFoursquareId(parcelable.getFoursquareId());
        place.setLatitude(parcelable.getLatitude());
        place.setLongitude(parcelable.getLongitude());

        return place;
    }
}
