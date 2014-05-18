package com.nlefler.glucloser.util.database.save;

import com.nlefler.glucloser.types.Place;

/**
 * Created by nathan on 5/18/14.
 */
public class PlaceSavedEvent {
    private Place _place;

    public PlaceSavedEvent(Place place) {
        _place = place;
    }

    public Place getPlace() {
        return _place;
    }
}
