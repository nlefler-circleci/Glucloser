package com.nlefler.glucloser.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class PlaceParcelable implements Parcelable {
    private String name;
    private String foursquareId;
    private float latitude;
    private float longitude;

    public PlaceParcelable() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFoursquareId() {
        return foursquareId;
    }

    public void setFoursquareId(String foursquareId) {
        this.foursquareId = foursquareId;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    /** Parcelable */
    protected PlaceParcelable(Parcel in) {
        name = in.readString();
        foursquareId = in.readString();
        latitude = in.readFloat();
        longitude = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(foursquareId);
        dest.writeFloat(latitude);
        dest.writeFloat(longitude);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PlaceParcelable> CREATOR = new Parcelable.Creator<PlaceParcelable>() {
        @Override
        public PlaceParcelable createFromParcel(Parcel in) {
            return new PlaceParcelable(in);
        }

        @Override
        public PlaceParcelable[] newArray(int size) {
            return new PlaceParcelable[size];
        }
    };
}
