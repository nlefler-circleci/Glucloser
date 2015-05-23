package com.nlefler.glucloser.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public class BloodSugarParcelable implements Parcelable {
    private String id;
    private int value;
    private Date date;

    public BloodSugarParcelable() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    /** Parcelable */
    protected BloodSugarParcelable(Parcel in) {
        id = in.readString();
        value = in.readInt();
        long time = in.readLong();
        if (time > 0) {
            date = new Date(time);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(value);
        if (date != null) {
            dest.writeLong(date.getTime());
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BloodSugarParcelable> CREATOR = new Parcelable.Creator<BloodSugarParcelable>() {
        @Override
        public BloodSugarParcelable createFromParcel(Parcel in) {
            return new BloodSugarParcelable(in);
        }

        @Override
        public BloodSugarParcelable[] newArray(int size) {
            return new BloodSugarParcelable[size];
        }
    };
}
