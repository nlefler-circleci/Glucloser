package com.nlefler.glucloser.models;

import java.util.Date;
import java.util.List;

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public interface BolusEvent {
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

    public List<Food> getFoods();
    public void setFoods(List<Food> foods);
}
