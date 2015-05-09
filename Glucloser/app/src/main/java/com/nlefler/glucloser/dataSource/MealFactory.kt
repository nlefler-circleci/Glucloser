package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import com.nlefler.glucloser.models.Meal
import com.nlefler.glucloser.models.MealParcelable

import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery

import java.util.Date
import java.util.UUID

import io.realm.Realm
import io.realm.RealmQuery
import rx.functions.Action1
import rx.functions.Action2

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealFactory {

    public fun AreMealsEqual(meal1: Meal?, meal2: Meal?): Boolean {
        if (meal1 == null || meal2 == null) {
            return false
        }

        val idOK = meal1.getMealId() == meal2.getMealId()
        val placeOK = PlaceFactory.ArePlacesEqual(meal1.getPlace(), meal2.getPlace())
        val carbsOK = meal1.getCarbs() == meal2.getCarbs()
        val insulinOK = meal1.getInsulin() == meal2.getInsulin()
        val correctionOK = meal1.isCorrection() == meal2.isCorrection()
        val beforeSugarOK = BloodSugarFactory.AreBloodSugarsEqual(meal1.getBeforeSugar(), meal2.getBeforeSugar())
        val dateOK = meal1.getDate() == meal2.getDate()

        return idOK && placeOK && carbsOK && insulinOK && correctionOK && beforeSugarOK && dateOK
    }

    companion object {
        private val LOG_TAG = "MealFactory"

        public fun Meal(ctx: Context): Meal {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val meal = MealForMealId(null, realm, true)!!
            realm.commitTransaction()

            return meal
        }

        public fun FetchMeal(id: String?, ctx: Context?, action: Action1<Meal>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to fetch Meal, action is null")
                return
            }
            if (id == null || id.isEmpty() || ctx == null) {
                Log.e(LOG_TAG, "Unable to fetch Meal, invalid args")
                action.call(null)
                return
            }
            val realm = Realm.getInstance(ctx)
            realm.beginTransaction()
            val meal = MealForMealId(id, realm, false)
            if (meal != null) {
                action.call(meal)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Meal.ParseClassName)
            parseQuery.whereEqualTo(Meal.MealIdFieldName, id)
            parseQuery.findInBackground(object : FindCallback<ParseObject>() {
                override fun done(parseObjects: List<ParseObject>, e: ParseException) {
                    if (!parseObjects.isEmpty()) {
                        val mealFromParse = MealFromParseObject(parseObjects.get(0), realm)
                        action.call(mealFromParse)
                    } else {
                        action.call(null)
                    }
                }
            })
        }

        public fun ParcelableFromMeal(meal: Meal): MealParcelable {
            val parcelable = MealParcelable()
            if (meal.getPlace() != null) {
                parcelable.setPlaceParcelable(PlaceFactory.ParcelableFromPlace(meal.getPlace()!!))
            }
            parcelable.setCarbs(meal.getCarbs())
            parcelable.setInsulin(meal.getInsulin())
            parcelable.setMealId(meal.getMealId())
            parcelable.setCorrection(meal.isCorrection())
            if (meal.getBeforeSugar() != null) {
                parcelable.setBeforeSugarParcelable(BloodSugarFactory.ParcelableFromBloodSugar(meal.getBeforeSugar()!!))
            }
            parcelable.setDate(meal.getDate())

            return parcelable
        }

        public fun MealFromParcelable(parcelable: MealParcelable, ctx: Context): Meal {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val meal = MealForMealId(parcelable.getMealId(), realm, true)!!
            meal.setInsulin(parcelable.getInsulin())
            meal.setMealId(parcelable.getMealId())
            meal.setCarbs(parcelable.getCarbs())
            meal.setPlace(PlaceFactory.PlaceFromParcelable(parcelable.getPlaceParcelable()!!, ctx))
            meal.setCorrection(parcelable.isCorrection())
            if (parcelable.getBeforeSugarParcelable() != null) {
                meal.setBeforeSugar(BloodSugarFactory.BloodSugarFromParcelable(parcelable.getBeforeSugarParcelable()!!, ctx))
            }
            meal.setDate(parcelable.getDate())
            realm.commitTransaction()

            return meal
        }

        protected fun MealFromParseObject(parseObject: ParseObject?, realm: Realm?): Meal? {
            if (parseObject == null || realm == null) {
                Log.e(LOG_TAG, "Can't create Meal from Parse object, null")
                return null
            }
            val mealId = parseObject.getString(Meal.MealIdFieldName)
            if (mealId == null || mealId.isEmpty()) {
                Log.e(LOG_TAG, "Can't create Meal from Parse object, no id")
            }
            val carbs = parseObject.getInt(Meal.CarbsFieldName)
            val insulin = parseObject.getDouble(Meal.InsulinFieldName).toFloat()
            val correction = parseObject.getBoolean(Meal.CorrectionFieldName)
            val mealDate = parseObject.getDate(Meal.MealDateFieldName)
            val place = PlaceFactory.PlaceFromParseObject(parseObject.getParseObject(Meal.PlaceFieldName), realm)
            val beforeSugar = BloodSugarFactory.BloodSugarFromParseObject(parseObject.getParseObject(Meal.BeforeSugarFieldName), realm)

            realm.beginTransaction()
            val meal = MealForMealId(mealId, realm, true)!!
            if (carbs >= 0 && carbs != meal.getCarbs()) {
                meal.setCarbs(carbs)
            }
            if (insulin >= 0 && meal.getInsulin() != insulin) {
                meal.setInsulin(insulin)
            }
            if (beforeSugar != null && !BloodSugarFactory.AreBloodSugarsEqual(meal.getBeforeSugar(), beforeSugar)) {
                meal.setBeforeSugar(beforeSugar)
            }
            if (meal.isCorrection() != correction) {
                meal.setCorrection(correction)
            }
            if (place != null && !PlaceFactory.ArePlacesEqual(place, meal.getPlace())) {
                meal.setPlace(place)
            }
            if (mealDate != null) {
                meal.setDate(mealDate)
            }
            realm.commitTransaction()

            return meal
        }

        /**
         * Fetches or creates a ParseObject representing the provided Meal
         * @param meal
         * *
         * @param action Returns the ParseObject, and true if the object was created and should be saved.
         */
        internal fun ParseObjectFromMeal(meal: Meal, placeObject: ParseObject?, beforeSugarObject: ParseObject?, action: Action2<ParseObject, Boolean>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to create Parse object from Meal, action null")
                return
            }
            if (meal.getMealId()?.isEmpty() ?: false) {
                Log.e(LOG_TAG, "Unable to create Parse object from Meal, meal null or no id")
                action.call(null, false)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Meal.ParseClassName)
            parseQuery.whereEqualTo(Meal.MealIdFieldName, meal.getMealId())

            parseQuery.findInBackground(object : FindCallback<ParseObject>() {
                override fun done(parseObjects: List<ParseObject>, e: ParseException) {
                    val parseObject: ParseObject
                    var created = false
                    if (parseObjects.isEmpty()) {
                        parseObject = ParseObject(Meal.ParseClassName)
                        created = true
                    } else {
                        parseObject = parseObjects.get(0)
                    }
                    parseObject.put(Meal.MealIdFieldName, meal.getMealId())
                    if (placeObject != null) {
                        parseObject.put(Meal.PlaceFieldName, placeObject)
                    }
                    if (beforeSugarObject != null) {
                        parseObject.put(Meal.BeforeSugarFieldName, beforeSugarObject)
                    }
                    parseObject.put(Meal.CorrectionFieldName, meal.isCorrection())
                    parseObject.put(Meal.CarbsFieldName, meal.getCarbs())
                    parseObject.put(Meal.InsulinFieldName, meal.getInsulin())
                    parseObject.put(Meal.MealDateFieldName, meal.getDate())
                    action.call(parseObject, created)
                }
            })
        }

        private fun MealForMealId(id: String?, realm: Realm, create: Boolean): Meal? {
            if (create && (id == null || id.isEmpty())) {
                val meal = realm.createObject<Meal>(javaClass<Meal>())
                meal.setMealId(UUID.randomUUID().toString())
                return meal
            }

            val query = realm.where<Meal>(javaClass<Meal>())

            query.equalTo(Meal.MealIdFieldName, id)
            var result: Meal? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<Meal>(javaClass<Meal>())
                result!!.setMealId(id)
            }

            return result
        }
    }
}
