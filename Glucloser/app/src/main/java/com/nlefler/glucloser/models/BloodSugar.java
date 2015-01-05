package com.nlefler.glucloser.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public class BloodSugar extends RealmObject {
    @Ignore
    public static final String ParseClassName = "BloodSugar";

    @Ignore
    public static final String ValueFieldName = "value";
    private int value;

    @Ignore
    public static final String DateFieldName = "date";
    private Date date;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
