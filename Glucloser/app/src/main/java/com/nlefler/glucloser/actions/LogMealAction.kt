package com.nlefler.glucloser.actions

import android.app.Application
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log

import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.dataSource.BloodSugarFactory
import com.nlefler.glucloser.dataSource.MealFactory
import com.nlefler.glucloser.dataSource.ParseUploader
import com.nlefler.glucloser.dataSource.PlaceFactory
import com.nlefler.glucloser.models.BloodSugar
import com.nlefler.glucloser.models.Meal
import com.nlefler.glucloser.models.MealParcelable
import com.nlefler.glucloser.models.Place
import com.nlefler.glucloser.models.PlaceParcelable

import java.util.Date

import io.realm.Realm

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class LogMealAction : Parcelable {

    private var placeParcelable: PlaceParcelable? = null
    private var mealParcelable: MealParcelable? = null

    public constructor() {
    }

    public fun setPlaceParcelable(placeParcelable: PlaceParcelable) {
        this.placeParcelable = placeParcelable
    }

    public fun setMealParcelable(mealParcelable: MealParcelable) {
        this.mealParcelable = mealParcelable
    }

    public fun log() {
        if (this.placeParcelable == null || this.mealParcelable == null) {
            Log.e(LOG_TAG, "Can't log meal, meal or place null")
            return
        }

        Log.d(LOG_TAG, "Logging meal at " + this.placeParcelable!!.name)

        val sharedContext = GlucloserApplication.SharedApplication().getApplicationContext()
        val realm = Realm.getInstance(sharedContext)

        val place = PlaceFactory.PlaceFromParcelable(this.placeParcelable!!, sharedContext)

        var beforeSugar: BloodSugar? = null
        if (mealParcelable!!.getBeforeSugarParcelable() != null) {
            beforeSugar = BloodSugarFactory.BloodSugarFromParcelable(mealParcelable!!.getBeforeSugarParcelable()!!, sharedContext)
        }

        val meal = MealFactory.MealFromParcelable(this.mealParcelable!!, sharedContext)
        realm.beginTransaction()
        if (beforeSugar != null) {
            meal.setBeforeSugar(beforeSugar!!)
        }
        meal.place = place
        realm.commitTransaction()

        ParseUploader.SharedInstance().uploadPlace(place)
        if (beforeSugar != null) {
            ParseUploader.SharedInstance().uploadBloodSugar(beforeSugar!!)
        }
        ParseUploader.SharedInstance().uploadMeal(meal)
    }

    /** Parcelable  */
    public constructor(parcel: Parcel) {
        this.placeParcelable = parcel.readParcelable<Parcelable>(javaClass<PlaceParcelable>().getClassLoader()) as PlaceParcelable
        this.mealParcelable = parcel.readParcelable<Parcelable>(javaClass<MealParcelable>().getClassLoader()) as MealParcelable
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeParcelable(this.placeParcelable, flags)
        out.writeParcelable(this.mealParcelable, flags)
    }

    companion object {
        private val LOG_TAG = "LogMealAction"

        public val CREATOR: Parcelable.Creator<LogMealAction> = object : Parcelable.Creator<LogMealAction> {
            override fun createFromParcel(`in`: Parcel): LogMealAction {
                return LogMealAction(`in`)
            }

            override fun newArray(size: Int): Array<LogMealAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
