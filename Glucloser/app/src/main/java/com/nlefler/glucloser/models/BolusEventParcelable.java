package com.nlefler.glucloser.models;

import java.util.Date;

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public interface BolusEventParcelable {
    public Date getDate();
    public void setDate(Date date);

    public int getCarbs();
    public void setCarbs(int carbs);

    public float getInsulin();
    public void setInsulin(float insulin);

    public BloodSugarParcelable getBeforeSugarParcelable();
    public void setBeforeSugarParcelable(BloodSugarParcelable beforeSugarParcelable);

    public boolean isCorrection();
    public void setCorrection(boolean isCorrection);
}
