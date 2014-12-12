package com.nlefler.glucloser.models;

import io.realm.RealmObject;

/**
 * Created by Nathan Lefler on 12/11/14.
 */
public class Meal extends RealmObject {
    private String mealId;
    private Place place;
    private int carbs;
    private float insulin;

    public String getMealId() {
        return mealId;
    }

    public void setMealId(String mealId) {
        this.mealId = mealId;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
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
}
