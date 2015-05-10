package com.nlefler.glucloser.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealParcelable implements Parcelable, BolusEventParcelable {
    private String mealId;
    private Date date;
    private PlaceParcelable placeParcelable;
    private int carbs;
    private float insulin;
    private BloodSugarParcelable beforeSugarParcelable;
    private boolean correction;

    public MealParcelable() {

    }

    public String getMealId() {
        return mealId;
    }

    public void setMealId(String mealId) {
        this.mealId = mealId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public PlaceParcelable getPlaceParcelable() {
        return placeParcelable;
    }

    public void setPlaceParcelable(PlaceParcelable place) {
        this.placeParcelable = place;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public float getInsulin() {
        return insulin;
    }

    public void setInsulin(float insulin) {
        this.insulin = insulin;
    }

    public boolean isCorrection() {
        return this.correction;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    public BloodSugarParcelable getBeforeSugarParcelable() {
        return beforeSugarParcelable;
    }

    public void setBeforeSugarParcelable(BloodSugarParcelable beforeSugar) {
        this.beforeSugarParcelable = beforeSugar;
    }

    /** Parcelable */
    protected MealParcelable(Parcel in) {
        mealId = in.readString();
        placeParcelable = (PlaceParcelable) in.readValue(PlaceParcelable.class.getClassLoader());
        carbs = in.readInt();
        insulin = in.readFloat();
        correction = in.readInt() != 0;
        beforeSugarParcelable = (BloodSugarParcelable)in.readParcelable(BloodSugar.class.getClassLoader());
        date = new Date(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mealId);
        dest.writeParcelable(placeParcelable, flags);
        dest.writeInt(carbs);
        dest.writeFloat(insulin);
        dest.writeInt(correction ? 1 : 0);
        dest.writeParcelable(beforeSugarParcelable, flags);
        dest.writeLong(date.getTime());
   }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MealParcelable> CREATOR = new Parcelable.Creator<MealParcelable>() {
        @Override
        public MealParcelable createFromParcel(Parcel in) {
            return new MealParcelable(in);
        }

        @Override
        public MealParcelable[] newArray(int size) {
            return new MealParcelable[size];
        }
    };
}
