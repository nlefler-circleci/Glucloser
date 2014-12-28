package com.nlefler.glucloser.dataSource;

import android.content.Context;

import com.nlefler.glucloser.models.Place;
import com.nlefler.glucloser.models.PlaceParcelable;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceFactory {
    public static Place FromFoursquareVenue(NLFoursquareVenue venue, Context ctx) {
        Realm realm = Realm.getInstance(ctx);

        realm.beginTransaction();
        Place place = CreateOrFetchForFoursquareId(venue.id, realm);
        place.setName(venue.name);
        place.setFoursquareId(venue.id);
        place.setLatitude(venue.location.lat);
        place.setLongitude(venue.location.lng);
        realm.commitTransaction();

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

    public static Place PlaceFromParcelable(PlaceParcelable parcelable, Context ctx) {
        Realm realm = Realm.getInstance(ctx);

        realm.beginTransaction();
        Place place = CreateOrFetchForFoursquareId(parcelable.getFoursquareId(), realm);
        place.setName(parcelable.getName());
        place.setFoursquareId(parcelable.getFoursquareId());
        place.setLatitude(parcelable.getLatitude());
        place.setLongitude(parcelable.getLongitude());
        realm.commitTransaction();

        return place;
    }

    private static Place CreateOrFetchForFoursquareId(String id, Realm realm) {
        if (id == null || id.isEmpty()) {
            return realm.createObject(Place.class);
        }

        RealmQuery<Place> query = realm.where(Place.class);

        query.equalTo(Place.FoursquareIdFieldName, id);
        Place result = query.findFirst();

        if (result == null) {
            result = realm.createObject(Place.class);
        }

        return result;
    }
}
