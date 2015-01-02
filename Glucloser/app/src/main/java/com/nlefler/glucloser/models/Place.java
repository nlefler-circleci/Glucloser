package com.nlefler.glucloser.models;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Nathan Lefler on 12/11/14.
 */
public class Place extends RealmObject {
    @Ignore
    public static final String ParseClassName = "Place";

    @Ignore
    public static final String NameFieldName = "name";
    private String name;

    @Ignore
    public static final String FoursquareIdFieldName = "foursquareId";
    private String foursquareId;

    @Ignore
    public static final String LatitudeFieldName = "latitude";
    private float latitude;

    @Ignore
    public static final String LongitudeFieldName = "longitude";
    private float longitude;

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
}
