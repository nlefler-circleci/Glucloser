package com.nlefler.glucloser.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceParcelable : Parcelable {
    public var name: String? = null
    public var foursquareId: String? = null
    public var latitude: Float = 0.toFloat()
    public var longitude: Float = 0.toFloat()

    public constructor() {
    }

    /** Parcelable  */
    protected constructor(`in`: Parcel) {
        name = `in`.readString()
        foursquareId = `in`.readString()
        latitude = `in`.readFloat()
        longitude = `in`.readFloat()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(foursquareId)
        dest.writeFloat(latitude)
        dest.writeFloat(longitude)
    }

    companion object {

        SuppressWarnings("unused")
        public val CREATOR: Parcelable.Creator<PlaceParcelable> = object : Parcelable.Creator<PlaceParcelable> {
            override fun createFromParcel(`in`: Parcel): PlaceParcelable {
                return PlaceParcelable(`in`)
            }

            override fun newArray(size: Int): Array<PlaceParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }
}
