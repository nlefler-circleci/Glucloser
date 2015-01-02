package com.nlefler.glucloser.dataSource;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.nlefler.glucloser.models.CheckInPushedData;
import com.nlefler.glucloser.models.Place;
import com.nlefler.glucloser.models.PlaceParcelable;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.functions.Action1;
import rx.functions.Action2;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceFactory {
    private static String LOG_TAG = "PlaceFactory";

    public static void PlaceForId(String id, Context ctx, final Action1<Place> action) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to fetch Place for id, action is null");
            return;
        }
        if (id == null || id.isEmpty() || ctx == null) {
            Log.e(LOG_TAG, "Unable to fetch Place for id");
            action.call(null);
            return;
        }

        final Realm realm = Realm.getInstance(ctx);
        Place place = PlaceForFoursquareId(id, realm, false);
        if (place != null) {
            action.call(place);
            return;
        }

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(Place.ParseClassName);
        parseQuery.whereEqualTo(Place.FoursquareIdFieldName, id);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (!parseObjects.isEmpty()) {
                    Place place = PlaceFromParseObject(parseObjects.get(0), realm);
                    action.call(place);
                } else {
                    action.call(null);
                }
            }
        });
    }

    public static Place PlaceFromFoursquareVenue(NLFoursquareVenue venue, Context ctx) {
        if (venue == null || !IsVenueValid(venue) || ctx == null) {
            Log.e(LOG_TAG, "Unable to create Place from 4sq venue");
            return null;
        }

        Realm realm = Realm.getInstance(ctx);

        realm.beginTransaction();
        Place place = PlaceForFoursquareId(venue.id, realm, true);
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
        Place place = PlaceForFoursquareId(parcelable.getFoursquareId(), realm, true);
        place.setName(parcelable.getName());
        place.setFoursquareId(parcelable.getFoursquareId());
        place.setLatitude(parcelable.getLatitude());
        place.setLongitude(parcelable.getLongitude());
        realm.commitTransaction();

        return place;
    }

    public static boolean ArePlacesEqual(Place place1, Place place2) {
        if (place1 == null || place2 == null) {
            return false;
        }

        boolean idOK = place1.getFoursquareId().equals(place2.getFoursquareId());
        boolean nameOK = place1.getName().equals(place2.getName());
        boolean latOK = place1.getLatitude() == place2.getLatitude();
        boolean lonOK = place1.getLongitude() == place2.getLongitude();

        return idOK && nameOK && latOK && lonOK;
    }

    protected static Place PlaceFromParseObject(ParseObject parseObject, Realm realm) {
        if (parseObject == null || realm == null) {
            return null;
        }
        String foursquareId = parseObject.getString(Place.FoursquareIdFieldName);
        if (foursquareId == null || foursquareId.isEmpty()) {
            return null;
        }

        String name = parseObject.getString(Place.NameFieldName);
        float lat = (float)parseObject.getDouble(Place.LatitudeFieldName);
        float lon = (float)parseObject.getDouble(Place.LongitudeFieldName);

        realm.beginTransaction();
        Place place = PlaceForFoursquareId(foursquareId, realm, true);
        if (place.getFoursquareId() == null || place.getFoursquareId().isEmpty()) {
            place.setFoursquareId(foursquareId);
        }
        if (name != null && !name.isEmpty() && !place.getName().equals(name)) {
            place.setName(name);
        }
        if (lat != 0 && place.getLatitude() != lat) {
            place.setLatitude(lat);
        }
        if (lon != 0 && place.getLongitude() != lon) {
            place.setLongitude(lon);
        }
        realm.commitTransaction();

        return place;
    }

    /**
     * Fetches or creates a ParseObject representing the provided Place.
     * @param place
     * @param action Returns the fetched/created ParseObject, and true if the object was created
     *               and should be saved.
     */
    protected static void ParseObjectFromPlace(final Place place, final Action2<ParseObject, Boolean> action) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to create Parse object from Place, action null");
            return;
        }
        if (place == null || place.getFoursquareId() == null || place.getFoursquareId().isEmpty()) {
            Log.e(LOG_TAG, "Unable to create Parse object from Place, place null or no Foursquare id");
            action.call(null, false);
            return;
        }

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(Place.ParseClassName);
        parseQuery.whereEqualTo(Place.FoursquareIdFieldName, place.getFoursquareId());

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                ParseObject parseObject;
                boolean created = false;
                if (parseObjects.isEmpty()) {
                    parseObject = new ParseObject(Place.ParseClassName);
                    created = true;
                } else {
                    parseObject = parseObjects.get(0);
                }
                parseObject.put(Place.FoursquareIdFieldName, place.getFoursquareId());
                parseObject.put(Place.NameFieldName, place.getName());
                parseObject.put(Place.LatitudeFieldName, place.getLatitude());
                parseObject.put(Place.LongitudeFieldName, place.getLongitude());
                action.call(parseObject, created);
            }
        });
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
        boolean modified = false;
        realm.beginTransaction();
        Place place = PlaceForFoursquareId(checkInData.getVenueId(), realm, true);
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

    private static Place PlaceForFoursquareId(String id, Realm realm, boolean create) {
        if (create && (id == null || id.isEmpty())) {
            return realm.createObject(Place.class);
        }

        RealmQuery<Place> query = realm.where(Place.class);

        query.equalTo(Place.FoursquareIdFieldName, id);
        Place result = query.findFirst();

        if (result == null && create) {
            result = realm.createObject(Place.class);
            result.setFoursquareId(id);
        }

        return result;
    }

    private static boolean IsVenueValid(NLFoursquareVenue venue) {
        return venue != null && venue.id != null && !venue.id.isEmpty() &&
                venue.name != null && !venue.name.isEmpty();
    }
}
