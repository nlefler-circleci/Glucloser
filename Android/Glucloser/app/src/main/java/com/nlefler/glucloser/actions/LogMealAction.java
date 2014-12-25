package com.nlefler.glucloser.actions;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.nlefler.glucloser.dataSource.PlaceFactory;
import com.nlefler.glucloser.models.Place;
import com.nlefler.glucloser.models.PlaceParcelable;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class LogMealAction implements Parcelable {
    private static String LOG_TAG = "LogMealAction";

    private Place place;

    public LogMealAction() {

    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public void log() {
        Log.d(LOG_TAG, "Logging meal at " + this.place.getName());
    }

    /** Parcelable */
    public LogMealAction(Parcel parcel) {
        this.place = PlaceFactory.PlaceFromParcelable(
                (PlaceParcelable)parcel.readParcelable(PlaceParcelable.class.getClassLoader()));
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(PlaceFactory.ParcelableFromPlace(this.place), flags);
    }

    public static final Parcelable.Creator<LogMealAction> CREATOR
            = new Parcelable.Creator<LogMealAction>() {
        public LogMealAction createFromParcel(Parcel in) {
            return new LogMealAction(in);
        }

        public LogMealAction[] newArray(int size) {
            return new LogMealAction[size];
        }
    };
}
