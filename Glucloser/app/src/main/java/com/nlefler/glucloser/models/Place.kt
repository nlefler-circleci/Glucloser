package com.nlefler.glucloser.models

import io.realm.RealmObject
import io.realm.annotations.Ignore

/**
 * Created by Nathan Lefler on 12/11/14.
 */
public class Place : RealmObject() {
    public var name: String? = null
    public var foursquareId: String? = null
    public var latitude: Float = 0.toFloat()
    public var longitude: Float = 0.toFloat()

    companion object {
        Ignore
        public val ParseClassName: String = "Place"

        Ignore
        public val NameFieldName: String = "name"

        Ignore
        public val FoursquareIdFieldName: String = "foursquareId"

        Ignore
        public val LatitudeFieldName: String = "latitude"

        Ignore
        public val LongitudeFieldName: String = "longitude"
    }
}
