package com.nlefler.glucloser.models;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Nathan Lefler on 12/11/14.
 */
public class Meal extends RealmObject implements BolusEvent, HasPlace {
    @Ignore
    public static final String ParseClassName = "Meal";

    @Ignore
    public static final String MealIdFieldName = "mealId";
    private String id;

    @Ignore
    public static final String MealDateFieldName = "date";
    private Date date;

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
    private BloodSugar beforeSugar;

    @Ignore
    public static final String CorrectionFieldName = "correction";
    private boolean correction;

    @Ignore
    public static final String FoodListFieldName = "foods";
    private RealmList<Food> foods;

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public BloodSugar getBeforeSugar() {
        return beforeSugar;
    }

    public void setBeforeSugar(BloodSugar beforeSugar) {
        this.beforeSugar = beforeSugar;
    }

    public boolean isCorrection() {
        return this.correction;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    @Override
    public RealmList<Food> getFoods() {
        return foods;
    }

    public void setFoods(RealmList<Food> foods) {
        this.foods = foods;
    }
}
