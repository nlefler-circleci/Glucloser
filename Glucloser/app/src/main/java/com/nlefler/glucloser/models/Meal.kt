package com.nlefler.glucloser.models

import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Ignore

/**
 * Created by Nathan Lefler on 12/11/14.
 */
public class Meal : RealmObject(), BolusEvent {
    public var mealId: String? = null
    private var date: Date? = null
    public var place: Place? = null
    private var carbs: Int = 0
    private var insulin: Float = 0.toFloat()
    private var beforeSugar: BloodSugar? = null
    private var correction: Boolean = false

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

    override fun getBeforeSugar(): BloodSugar? {
        return beforeSugar
    }

    override fun setBeforeSugar(beforeSugar: BloodSugar) {
        this.beforeSugar = beforeSugar
    }

    override fun isCorrection(): Boolean {
        return this.correction
    }

    override fun setCorrection(isCorrection: Boolean) {
        this.correction = correction
    }

    companion object {
        Ignore
        public val ParseClassName: String = "Meal"

        Ignore
        public val MealIdFieldName: String = "mealId"

        Ignore
        public val MealDateFieldName: String = "date"

        Ignore
        public val PlaceFieldName: String = "place"

        Ignore
        public val CarbsFieldName: String = "carbs"

        Ignore
        public val InsulinFieldName: String = "insulin"

        Ignore
        public val BeforeSugarFieldName: String = "beforeSugar"

        Ignore
        public val CorrectionFieldName: String = "correction"
    }
}
