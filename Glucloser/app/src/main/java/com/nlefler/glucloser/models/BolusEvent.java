package com.nlefler.glucloser.models;

import java.util.Date;
import java.util.List;

import io.realm.RealmList;

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public interface BolusEvent {
    public String getId();
    public void setId(String id);

    public Date getDate();
    public void setDate(Date date);

    public int getCarbs();
    public void setCarbs(int carbs);

    public float getInsulin();
    public void setInsulin(float insulin);

    public BloodSugar getBeforeSugar();
    public void setBeforeSugar(BloodSugar beforeSugar);

    public boolean isCorrection();
    public void setCorrection(boolean isCorrection);

    public RealmList<Food> getFoods();
    public void setFoods(RealmList<Food> foods);
}
