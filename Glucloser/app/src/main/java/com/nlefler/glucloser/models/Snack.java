package com.nlefler.glucloser.models;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public class Snack extends RealmObject implements BolusEvent {
    @Ignore
    public static String ParseClassName = "Snack";
    @Ignore
    public static String SnackIdFieldName = "snackId";
    @Ignore
    public static String SnackDateFieldName = "date";
    @Ignore
    public static String CarbsFieldName = "carbs";
    @Ignore
    public static String InsulinFieldName = "insulin";
    @Ignore
    public static String BeforeSugarFieldName = "beforeSugar";
    @Ignore
    public static String CorrectionFieldName = "correction";
    @Ignore
    public static String FoodListFieldName = "foods";

    private String id;
    private Date date;
    private int carbs;
    private float insulin;
    private BloodSugar beforeSugar;
    private boolean correction;
    private RealmList<Food> foods;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
        return correction;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    @Override
    public RealmList<Food> getFoods() {
        return foods;
    }

    @Override
    public void setFoods(RealmList<Food> foods) {
        this.foods = foods;
    }
}
