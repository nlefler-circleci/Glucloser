package com.nlefler.glucloser.util.database.save;

import com.nlefler.glucloser.model.place.Place;

/**
 * Created by nathan on 5/18/14.
 */
public class PlaceUpdatedEvent {
    private Place _place;

    public PlaceUpdatedEvent(Place place) {
        _place = place;
    }

    public Place getPlace() {
        return _place;
    }
}
