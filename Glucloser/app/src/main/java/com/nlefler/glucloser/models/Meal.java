package com.nlefler.glucloser.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Nathan Lefler on 12/11/14.
 */
public class Meal extends RealmObject {
    @Ignore
    public static final String ParseClassName = "Meal";

    @Ignore
    public static final String MealIdFieldName = "mealId";
    private String mealId;

    @Ignore
    public static final String MealDateFieldName = "date";
    private Date mealDate;

    @Ignore
    public static final String PlaceFieldName = "place";
    private Place place;

    @Ignore
    public static final String CarbsFieldName = "carbs";
    private int carbs;

    @Ignore
    public static final String InsulinFieldName = "insulin";
    private float insulin;

    @Ignore
    public static final String BeforeSugarFieldName = "beforeSugar";
    private int beforeSugar;

    @Ignore
    public static final String CorrectionFieldName = "correction";
    private boolean correction;

    public String getMealId() {
        return mealId;
    }

    public void setMealId(String mealId) {
        this.mealId = mealId;
    }

    public Date getMealDate() {
        return mealDate;
    }

    public void setMealDate(Date mealDate) {
        this.mealDate = mealDate;
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

    public int getBeforeSugar() {
        return beforeSugar;
    }

    public void setBeforeSugar(int beforeSugar) {
        this.beforeSugar = beforeSugar;
    }

    public boolean getCorrection() {
        return this.correction;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }
}
