package com.nlefler.glucloser.dataSource

import android.util.Log
import com.nlefler.glucloser.models.*

import com.parse.ParseException
import com.parse.ParseObject
import com.parse.SaveCallback

import java.util.HashMap
import java.util.HashSet

import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.functions.Action1
import rx.functions.Action2
import rx.schedulers.Schedulers

/**
 * Created by Nathan Lefler on 12/30/14.
 */
public class ParseUploader private() {
    private val inProgressUploads: MutableMap<String, Observable<ParseObject>>

    init {
        inProgressUploads = HashMap<String, Observable<ParseObject>>()
    }

    public fun uploadPlace(place: Place) {
        val placeId = place.getFoursquareId()!!
        this.getUploadedObjectObservable(placeId, place).subscribe({ parseObject: ParseObject ->
            parseObject.saveInBackground()
            inProgressUploads.remove(placeId)
        })
    }

    public fun uploadSnack(snack: Snack) {
        val beforeSugar = snack.getBeforeSugar()
        val beforeSugarId = beforeSugar?.getId() ?: null

        var beforeSugarObservable: Observable<ParseObject>?
        if (beforeSugarId != null) {
            beforeSugarObservable = getUploadedObjectObservable(beforeSugarId, beforeSugar!!)
            beforeSugarObservable!!.subscribeOn(Schedulers.io())
            beforeSugarObservable!!.subscribe(object : Observer<ParseObject> {
                private var beforeSugarParseObject: ParseObject? = null

                override fun onCompleted() {
                    val snackId = snack.getSnackId()!!
                    getUploadedObjectObservable(snackId, snack, beforeSugarParseObject).subscribe(object : Action1<ParseObject> {
                        override fun call(snackObject: ParseObject) {
                            snackObject.saveInBackground()
                            inProgressUploads.remove(snackId)
                        }
                    })
                }

                override fun onError(e: Throwable) {
                    Log.e(LOG_TAG, "Unable to save Snack to Parse: " + e.getMessage())
                }

                override fun onNext(parseObject: ParseObject) {
                    beforeSugarParseObject = parseObject
                    inProgressUploads.remove(beforeSugarId)
                }
            })
        }
    }

    public fun uploadMeal(meal: Meal) {
        val place = meal.getPlace()!!
        val placeId = place.getFoursquareId()!!

        val beforeSugar = meal.getBeforeSugar()
        val beforeSugarId = beforeSugar?.getId() ?: null

        val placeFetchObservable = getUploadedObjectObservable(placeId, place)
        var beforeSugarObservable: Observable<ParseObject>? = null
        if (beforeSugarId != null) {
            beforeSugarObservable = getUploadedObjectObservable(beforeSugarId, beforeSugar!!)
        }

        var finalObservable: Observable<ParseObject>?
        if (beforeSugarObservable != null) {
            placeFetchObservable.subscribeOn(Schedulers.io())
            finalObservable = Observable.merge<ParseObject>(placeFetchObservable, beforeSugarObservable)
        } else {
            finalObservable = placeFetchObservable
        }
        finalObservable!!.subscribe(object : Observer<ParseObject> {
            private var placeParseObject: ParseObject? = null
            private var beforeSugarParseObject: ParseObject? = null

            override fun onCompleted() {
                val mealId = meal.getMealId()!!
                getUploadedObjectObservable(mealId, meal, placeParseObject, beforeSugarParseObject).subscribe({ mealObject: ParseObject ->
                    mealObject.saveInBackground()
                    inProgressUploads.remove(mealId)
                })
            }

            override fun onError(e: Throwable) {
                Log.e(LOG_TAG, "Unable to save Meal to Parse: " + e.getMessage())
            }

            override fun onNext(parseObject: ParseObject) {
                val className = parseObject.getClassName()
                if (className == Place.ParseClassName) {
                    placeParseObject = parseObject
                    inProgressUploads.remove(placeId)
                } else if (className == BloodSugar.ParseClassName) {
                    beforeSugarParseObject = parseObject
                    inProgressUploads.remove(beforeSugarId)
                }
            }
        })
    }

    public fun uploadBloodSugar(sugar: BloodSugar) {
        val sugarId = sugar.getId()!!
        this.getUploadedObjectObservable(sugarId, sugar).subscribe({ parseObject: ParseObject ->
            parseObject.saveInBackground()
            inProgressUploads.remove(sugarId)
        })
    }

    public fun uploadFood(food: Food) {
        val foodId = food.getFoodId()
        this.getUploadedObjectObservable(foodId, food).subscribe({ parseObject: ParseObject ->
            parseObject.saveInBackground()
            inProgressUploads.remove(foodId)
        })
    }

    /** Helpers  */
    synchronized private fun getUploadedObjectObservable(localId: String, toUpload: Any, vararg args: Any?): Observable<ParseObject> {
        if (this.inProgressUploads.containsKey(localId)) {
            return this.inProgressUploads.get(localId)!!
        } else {
            val observable = Observable.create<ParseObject>(object : Observable.OnSubscribe<ParseObject> {
                override fun call(subscriber: Subscriber<in ParseObject>) {
                    when (toUpload) {
                        is Place -> {
                            PlaceFactory.ParseObjectFromPlace(toUpload : Place, createParseObjectReadyAction(subscriber))
                        }
                        is Meal -> {
                            if (args.size() < 2 || args[0] !is ParseObject) {
                                subscriber.onError(IllegalArgumentException("Invalid specific arguments for Meal"))
                                return
                            }
                            MealFactory.ParseObjectFromMeal(toUpload : Meal, args[0] as ParseObject?, args[1] as ParseObject?, createParseObjectReadyAction(subscriber))
                        }
                        is BloodSugar -> {
                            BloodSugarFactory.ParseObjectFromBloodSugar(toUpload : BloodSugar, createParseObjectReadyAction(subscriber))
                        }
                        is Snack -> {
                            if (args.size() != 1) {
                                subscriber.onError(IllegalArgumentException("Invalid specific arguments for Snack"))
                                return
                            }
                            SnackFactory.ParseObjectFromSnack(toUpload : Snack, args[0] as ParseObject?, createParseObjectReadyAction(subscriber))
                        }
                        else -> {
                            subscriber.onError(IllegalArgumentException("Invalid type for upload object"))
                        }
                    }
                }
            })
            this.inProgressUploads.put(localId, observable)
            return observable
        }
    }

    private fun createParseObjectReadyAction(subscriber: Subscriber<in ParseObject>): Action2<ParseObject?, Boolean> {
        return object : Action2<ParseObject?, Boolean> {
            override fun call(parseObject: ParseObject?, created: Boolean?) {
                if (parseObject == null) {
                    subscriber.onError(RuntimeException("Unable to create ParseObject"))
                    return
                }

                if (created!!) {
                    parseObject.saveInBackground({e: ParseException? ->
                        if (e == null) {
                            subscriber.onNext(parseObject)
                            subscriber.onCompleted()
                        } else {
                            subscriber.onError(e)
                        }
                    })
                } else {
                    subscriber.onNext(parseObject)
                    subscriber.onCompleted()
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = "ParseUploader"

        private var _sharedInstance: ParseUploader? = null

        synchronized public fun SharedInstance(): ParseUploader {
            if (_sharedInstance == null) {
                _sharedInstance = ParseUploader()
            }
            return _sharedInstance!!
        }
    }
}
