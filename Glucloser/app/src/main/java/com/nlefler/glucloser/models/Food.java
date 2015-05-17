package com.nlefler.glucloser.models;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Nathan Lefler on 5/16/15.
 */
public class Food extends RealmObject {
    @Ignore
    public static final String ParseClassName = "Food";

    @Ignore
    public static final String FoodIdFieldName = "foodId";
    private String foodId;

    @Ignore
    public static final String CarbsFieldName = "carbs";
    private int carbs;

    @Ignore
    public static final String FoodNameFieldName = "name";
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
