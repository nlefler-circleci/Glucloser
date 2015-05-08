package com.nlefler.glucloser.models

import android.os.Parcel
import android.os.Parcelable

import java.util.Date

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealParcelable : Parcelable, BolusEventParcelable {
    public var mealId: String? = null
    private var date: Date? = null
    public var placeParcelable: PlaceParcelable? = null
    private var carbs: Int = 0
    private var insulin: Float = 0.toFloat()
    private var beforeSugarParcelable: BloodSugarParcelable? = null
    private var correction: Boolean = false

    public constructor() {
    }

    override fun getDate(): Date {
        return date!!
    }

    override fun setDate(date: Date) {
        this.date = date
    }

    override fun getCarbs(): Int {
        return carbs
    }

    override fun setCarbs(carbs: Int) {
        this.carbs = carbs
    }

    override fun getInsulin(): Float {
        return insulin
    }

    override fun setInsulin(insulin: Float) {
        this.insulin = insulin
    }

    override fun isCorrection(): Boolean {
        return this.correction
    }

    override fun setCorrection(isCorrection: Boolean) {
        this.correction = correction
    }

    override fun getBeforeSugarParcelable(): BloodSugarParcelable? {
        return beforeSugarParcelable
    }

    override fun setBeforeSugarParcelable(beforeSugarParcelable: BloodSugarParcelable) {
        this.beforeSugarParcelable = beforeSugarParcelable
    }

    /** Parcelable  */
    protected constructor(`in`: Parcel) {
        mealId = `in`.readString()
        placeParcelable = `in`.readValue(javaClass<PlaceParcelable>().getClassLoader()) as PlaceParcelable
        carbs = `in`.readInt()
        insulin = `in`.readFloat()
        correction = `in`.readInt() != 0
        beforeSugarParcelable = `in`.readParcelable<Parcelable>(javaClass<BloodSugar>().getClassLoader()) as BloodSugarParcelable
        date = Date(`in`.readLong())
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(mealId)
        dest.writeParcelable(placeParcelable, flags)
        dest.writeInt(carbs)
        dest.writeFloat(insulin)
        dest.writeInt(if (correction) 1 else 0)
        dest.writeParcelable(beforeSugarParcelable, flags)
        dest.writeLong(date!!.getTime())
    }

    companion object {

        SuppressWarnings("unused")
        public val CREATOR: Parcelable.Creator<MealParcelable> = object : Parcelable.Creator<MealParcelable> {
            override fun createFromParcel(`in`: Parcel): MealParcelable {
                return MealParcelable(`in`)
            }

            override fun newArray(size: Int): Array<MealParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }
}
