package com.nlefler.glucloser.model.sync;

import com.nlefler.glucloser.model.GlucloserBaseModel;
import com.nlefler.glucloser.model.bolus.Bolus;
import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.meterdata.MeterData;
import com.nlefler.glucloser.model.place.Place;

import java.util.Date;

import se.emilsjolander.sprinkles.annotations.Column;

/**
 * Created by lefler on 6/17/14.
 */
public class SyncEvent extends GlucloserBaseModel {

    protected static final String BolusColumnName = "bolus";
    protected static final String FoodColumnName = "food";
    protected static final String MeterDataColumnName = "meterdata";
    protected static final String PlaceColumnName = "place";

    @Column(SyncEvent.BolusColumnName)
    protected Date bolusSyncTime;

    @Column(SyncEvent.FoodColumnName)
    protected Date foodSyncTime;

    @Column(SyncEvent.MeterDataColumnName)
    protected Date meterDataSyncTime;

    @Column(SyncEvent.PlaceColumnName)
    protected Date placeSyncTime;

    public Date getTimeForModel(Class modelClass) {
         // TODO
        if (modelClass.equals(Bolus.class)) {
            return bolusSyncTime;
        }
        else if (modelClass.equals(Food.class)) {
            return foodSyncTime;
        }
        else if (modelClass.equals(MeterData.class)) {
            return meterDataSyncTime;
        }
        else if (modelClass.equals(Place.class)) {
            return placeSyncTime;
        }
        return null;
    }

    public void setTimeForModel(Date newTime, Class modelClass) {
        // TODO
        if (modelClass.equals(Bolus.class)) {
            bolusSyncTime = newTime;
        }
        else if (modelClass.equals(Food.class)) {
            foodSyncTime = newTime;
        }
        else if (modelClass.equals(MeterData.class)) {
            meterDataSyncTime = newTime;
        }
        else if (modelClass.equals(Place.class)) {
            placeSyncTime = newTime;
        }
    }
}
