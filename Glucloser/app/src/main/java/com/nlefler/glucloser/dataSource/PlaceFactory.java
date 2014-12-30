package com.nlefler.glucloser.dataSource;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.nlefler.glucloser.models.CheckInPushedData;
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
    private static String LOG_TAG = "PlaceFactory";

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

    public static Place PlaceFromCheckInData(Context ctx, Bundle data) {
        if (ctx == null || data == null) {
            Log.e(LOG_TAG, "Cannot create Place from check-in data, context or bundle null");
            return null;
        }
        String checkInDataSerialized = data.getString("com.parse.Data");
        if (checkInDataSerialized == null || checkInDataSerialized.isEmpty()) {
            Log.e(LOG_TAG, "Cannot create Place from check-in data, parse bundle null");
            return null;
        }
        CheckInPushedData checkInData = (new Gson()).fromJson(checkInDataSerialized, CheckInPushedData.class);
        if (checkInData == null) {
            Log.e(LOG_TAG, "Cannot create Place from check-in data, couldn't parse data");
            return null;
        }

        Realm realm = Realm.getInstance(ctx);
        Place place = CreateOrFetchForFoursquareId(checkInData.getVenueId(), realm);
        boolean modified = false;
        realm.beginTransaction();
        if (checkInData.getVenueName() != null && !checkInData.getVenueName().isEmpty() &&
                !place.getName().equals(checkInData.getVenueName())) {
            modified = true;
            place.setName(checkInData.getVenueName());
        }
        if (checkInData.getVenueLat() != place.getLatitude()) {
            modified = true;
            place.setLatitude(checkInData.getVenueLat());
        }
        if (checkInData.getVenueLon() != place.getLongitude()) {
            modified = true;
            place.setLongitude(checkInData.getVenueLon());
        }
        if (modified) {
            realm.commitTransaction();
        } else {
            realm.cancelTransaction();
        }

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
            realm.beginTransaction();
            result = realm.createObject(Place.class);
            realm.commitTransaction();
        }

        return result;
    }
}
