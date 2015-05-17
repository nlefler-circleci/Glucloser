package com.nlefler.glucloser.models;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by Nathan Lefler on 5/16/15.
 */
public class FoodParcelable implements Parcelable {
    private String foodId;
    private int carbs;

    public FoodParcelable() {

    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public int getCarbs() {
        return carbs;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    /** Parcelable */
    protected FoodParcelable(Parcel in) {
        foodId = in.readString();
        carbs = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(foodId);
        dest.writeInt(carbs);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FoodParcelable> CREATOR = new Parcelable.Creator<FoodParcelable>() {
        @Override
        public FoodParcelable createFromParcel(Parcel in) {
            return new FoodParcelable(in);
        }

        @Override
        public FoodParcelable[] newArray(int size) {
            return new FoodParcelable[size];
        }
    };
}
