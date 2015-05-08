package com.nlefler.glucloser.models

import android.os.Parcel
import android.os.Parcelable

import java.util.Date

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public class BloodSugarParcelable : Parcelable {
    public var id: String? = null
    public var value: Int = 0
    public var date: Date? = null

    public constructor() {
    }

    /** Parcelable  */
    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        value = `in`.readInt()
        date = Date(`in`.readLong())
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeInt(value)
        dest.writeLong(date!!.getTime())
    }

    companion object {

        SuppressWarnings("unused")
        public val CREATOR: Parcelable.Creator<BloodSugarParcelable> = object : Parcelable.Creator<BloodSugarParcelable> {
            override fun createFromParcel(`in`: Parcel): BloodSugarParcelable {
                return BloodSugarParcelable(`in`)
            }

            override fun newArray(size: Int): Array<BloodSugarParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }
}
