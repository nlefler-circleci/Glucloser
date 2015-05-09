package com.nlefler.glucloser.dataSource

import android.content.Context
import android.os.Bundle
import android.util.Log

import com.google.gson.Gson
import com.nlefler.glucloser.models.CheckInPushedData
import com.nlefler.glucloser.models.Place
import com.nlefler.glucloser.models.PlaceParcelable
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue
import com.parse.FindCallback
import com.parse.GetCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery

import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import rx.functions.Action1
import rx.functions.Action2

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceFactory {
    companion object {
        private val LOG_TAG = "PlaceFactory"

        public fun PlaceForId(id: String?, ctx: Context?, action: Action1<Place>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to fetch Place for id, action is null")
                return
            }
            if (id == null || id.isEmpty() || ctx == null) {
                Log.e(LOG_TAG, "Unable to fetch Place for id")
                action.call(null)
                return
            }

            val realm = Realm.getInstance(ctx)
            val place = PlaceForFoursquareId(id, realm, false)
            if (place != null) {
                action.call(place)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Place.ParseClassName)
            parseQuery.whereEqualTo(Place.FoursquareIdFieldName, id)
            parseQuery.findInBackground(object : FindCallback<ParseObject>() {

                override fun done(parseObjects: List<ParseObject>, e: ParseException) {
                    if (!parseObjects.isEmpty()) {
                        val placeFromParse = PlaceFromParseObject(parseObjects.get(0), realm)
                        action.call(placeFromParse)
                    } else {
                        action.call(null)
                    }
                }
            })
        }

        public fun ParcelableFromFoursquareVenue(venue: NLFoursquareVenue?): PlaceParcelable? {
            if (venue == null || !IsVenueValid(venue)) {
                Log.e(LOG_TAG, "Unable to create Place from 4sq venue")
                return null
            }

            val parcelable = PlaceParcelable()
            parcelable.setName(venue.name)
            parcelable.setFoursquareId(venue.id)
            parcelable.setLatitude(venue.location.lat)
            parcelable.setLongitude(venue.location.lng)

            return parcelable
        }

        public fun PlaceFromFoursquareVenue(venue: NLFoursquareVenue?, ctx: Context?): Place? {
            if (venue == null || !IsVenueValid(venue) || ctx == null) {
                Log.e(LOG_TAG, "Unable to create Place from 4sq venue")
                return null
            }

            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val place = PlaceForFoursquareId(venue.id, realm, true)
            place!!.setName(venue.name)
            place.setFoursquareId(venue.id)
            place.setLatitude(venue.location.lat)
            place.setLongitude(venue.location.lng)
            realm.commitTransaction()

            return place
        }

        public fun ParcelableFromPlace(place: Place): PlaceParcelable? {
            val parcelable = PlaceParcelable()
            parcelable.setName(place.getName())
            parcelable.setFoursquareId(place.getFoursquareId())
            parcelable.setLatitude(place.getLatitude())
            parcelable.setLongitude(place.getLongitude())

            return parcelable
        }

        public fun PlaceFromParcelable(parcelable: PlaceParcelable, ctx: Context): Place {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val place = PlaceForFoursquareId(parcelable.getFoursquareId(), realm, true)
            place!!.setName(parcelable.getName())
            place.setFoursquareId(parcelable.getFoursquareId())
            place.setLatitude(parcelable.getLatitude())
            place.setLongitude(parcelable.getLongitude())
            realm.commitTransaction()

            return place
        }

        public fun ArePlacesEqual(place1: Place?, place2: Place?): Boolean {
            if (place1 == null || place2 == null) {
                return false
            }

            val idOK = place1.getFoursquareId() == place2.getFoursquareId()
            val nameOK = place1.getName() == place2.getName()
            val latOK = place1.getLatitude() == place2.getLatitude()
            val lonOK = place1.getLongitude() == place2.getLongitude()

            return idOK && nameOK && latOK && lonOK
        }

        internal fun PlaceFromParseObject(parseObject: ParseObject?, realm: Realm?): Place? {
            if (parseObject == null || realm == null) {
                return null
            }
            val foursquareId = parseObject.getString(Place.FoursquareIdFieldName)
            if (foursquareId == null || foursquareId.isEmpty()) {
                return null
            }

            val name = parseObject.getString(Place.NameFieldName)
            val lat = parseObject.getDouble(Place.LatitudeFieldName).toFloat()
            val lon = parseObject.getDouble(Place.LongitudeFieldName).toFloat()

            realm.beginTransaction()
            val place = PlaceForFoursquareId(foursquareId, realm, true)!!
            if (place.getFoursquareId()?.isEmpty() ?: false) {
                place.setFoursquareId(foursquareId)
            }
            if (name != null && !name.isEmpty() && place.getName().equals(name)) {
                place.setName(name)
            }
            if (lat != 0f && place.getLatitude() != lat) {
                place.setLatitude(lat)
            }
            if (lon != 0f && place.getLongitude() != lon) {
                place.setLongitude(lon)
            }
            realm.commitTransaction()

            return place
        }

        /**
         * Fetches or creates a ParseObject representing the provided Place.
         * @param place
         * *
         * @param action Returns the fetched/created ParseObject, and true if the object was created
         * *               and should be saved.
         */
        internal fun ParseObjectFromPlace(place: Place?, action: Action2<ParseObject, Boolean>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to create Parse object from Place, action null")
                return
            }
            if (place == null || place.getFoursquareId()== null || place.getFoursquareId()!!.isEmpty()) {
                Log.e(LOG_TAG, "Unable to create Parse object from Place, place null or no Foursquare id")
                action.call(null, false)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Place.ParseClassName)
            parseQuery.whereEqualTo(Place.FoursquareIdFieldName, place.getFoursquareId())

            parseQuery.findInBackground(object : FindCallback<ParseObject>() {
                override fun done(parseObjects: List<ParseObject>, e: ParseException) {
                    val parseObject: ParseObject
                    var created = false
                    if (parseObjects.isEmpty()) {
                        parseObject = ParseObject(Place.ParseClassName)
                        created = true
                    } else {
                        parseObject = parseObjects.get(0)
                    }
                    parseObject.put(Place.FoursquareIdFieldName, place.getFoursquareId())
                    parseObject.put(Place.NameFieldName, place.getName())
                    parseObject.put(Place.LatitudeFieldName, place.getLatitude())
                    parseObject.put(Place.LongitudeFieldName, place.getLongitude())
                    action.call(parseObject, created)
                }
            })
        }

        public fun PlaceParcelableFromCheckInData(data: Bundle?): PlaceParcelable? {
            if (data == null) {
                Log.e(LOG_TAG, "Cannot create Place from check-in data, bundle null")
                return null
            }
            val checkInDataSerialized = data.getString("com.parse.Data")
            if (checkInDataSerialized == null || checkInDataSerialized.isEmpty()) {
                Log.e(LOG_TAG, "Cannot create Place from check-in data, parse bundle null")
                return null
            }
            val checkInData = (Gson()).fromJson<CheckInPushedData>(checkInDataSerialized, javaClass<CheckInPushedData>())
            if (checkInData == null) {
                Log.e(LOG_TAG, "Cannot create Place from check-in data, couldn't parse data")
                return null
            }

            val placeParcelable = PlaceParcelable()
            placeParcelable.setFoursquareId(checkInData.getVenueId())
            placeParcelable.setName(checkInData.getVenueName())
            if (checkInData.getVenueLat() != 0f) {
                placeParcelable.setLatitude(checkInData.getVenueLat())
            }
            if (checkInData.getVenueLon() != 0f) {
                placeParcelable.setLongitude(checkInData.getVenueLon())
            }

            return placeParcelable
        }

        private fun PlaceForFoursquareId(id: String?, realm: Realm, create: Boolean): Place? {
            if (create && (id == null || id.isEmpty())) {
                return realm.createObject<Place>(javaClass<Place>())
            }

            val query = realm.where<Place>(javaClass<Place>())

            query.equalTo(Place.FoursquareIdFieldName, id)
            var result: Place? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<Place>(javaClass<Place>())
                result!!.setFoursquareId(id)
            }

            return result
        }

        private fun IsVenueValid(venue: NLFoursquareVenue?): Boolean {
            return venue != null && venue.id != null && !venue.id.isEmpty() && venue.name != null && !venue.name.isEmpty()
        }
    }
}
