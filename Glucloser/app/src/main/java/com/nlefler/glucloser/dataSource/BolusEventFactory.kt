package com.nlefler.glucloser.dataSource

import android.os.Parcelable
import com.nlefler.glucloser.models.BolusEvent
import com.nlefler.glucloser.models.Meal
import com.nlefler.glucloser.models.Snack

/**
 * Created by Nathan Lefler on 7/2/15.
 */
public class BolusEventFactory {
    companion object {
        public fun ParcelableFromBolusEvent(bolusEvent: BolusEvent): Parcelable? {
            when (bolusEvent) {
                is Snack -> return SnackFactory ParcelableFromSnack bolusEvent
                is Meal -> return MealFactory.ParcelableFromMeal(bolusEvent)
                else -> return null
            }
        }
    }
}
