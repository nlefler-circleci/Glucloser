package com.nlefler.glucloser.models

import java.util.Date

/**
 * Created by Nathan Lefler on 4/30/15.
 */
public trait BolusEvent {
    public fun getDate(): Date
    public fun setDate(date: Date)

    public fun getCarbs(): Int
    public fun setCarbs(carbs: Int)

    public fun getInsulin(): Float
    public fun setInsulin(insulin: Float)

    public fun getBeforeSugar(): BloodSugar?
    public fun setBeforeSugar(beforeSugar: BloodSugar)

    public fun isCorrection(): Boolean
    public fun setCorrection(isCorrection: Boolean)
}
