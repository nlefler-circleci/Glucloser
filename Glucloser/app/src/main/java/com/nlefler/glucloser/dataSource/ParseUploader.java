package com.nlefler.glucloser.dataSource;

import android.util.Log;

import com.nlefler.glucloser.models.BloodSugar;
import com.nlefler.glucloser.models.Meal;
import com.nlefler.glucloser.models.Place;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.schedulers.Schedulers;

/**
 * Created by Nathan Lefler on 12/30/14.
 */
public class ParseUploader {
    private static String LOG_TAG = "ParseUploader";

    private static ParseUploader _sharedInstance;
    private Map<String, Observable<ParseObject>> inProgressUploads;

    public static synchronized ParseUploader SharedInstance() {
        if (_sharedInstance == null) {
            _sharedInstance = new ParseUploader();
        }
        return _sharedInstance;
    }
    private ParseUploader() {
        inProgressUploads = new HashMap<String, Observable<ParseObject>>();
    }

    public void uploadPlace(Place place) {
        final String placeId = place.getFoursquareId();
        this.getUploadedObjectObservable(placeId, place).subscribe(new Action1<ParseObject>() {
            @Override
            public void call(ParseObject parseObject) {
                parseObject.saveInBackground();
                inProgressUploads.remove(placeId);
            }
        });
    }

    public void uploadMeal(final Meal meal) {
        final Place place = meal.getPlace();
        final String placeId = place.getFoursquareId();

        final BloodSugar beforeSugar = meal.getBeforeSugar();
        final String beforeSugarId = beforeSugar != null ? beforeSugar.getId() : null;

        Observable<ParseObject> placeFetchObservable = getUploadedObjectObservable(placeId, place);
        Observable<ParseObject> beforeSugarObservable = null;
        if (beforeSugarId != null) {
            beforeSugarObservable = getUploadedObjectObservable(beforeSugarId, beforeSugar);
        }

        Observable<ParseObject> finalObservable = null;
        if (beforeSugarObservable != null) {
            placeFetchObservable.subscribeOn(Schedulers.io());
            finalObservable = Observable.merge(placeFetchObservable, beforeSugarObservable);
        } else {
            finalObservable = placeFetchObservable;
        }
        finalObservable.subscribe(new Observer<ParseObject>() {
            private ParseObject placeParseObject;
            private ParseObject beforeSugarParseObject;

            @Override
            public void onCompleted() {
                final String mealId = meal.getMealId();
                getUploadedObjectObservable(mealId, meal, placeParseObject, beforeSugarParseObject)
                        .subscribe(new Action1<ParseObject>() {
                            @Override
                            public void call(ParseObject mealObject) {
                                mealObject.saveInBackground();
                                inProgressUploads.remove(mealId);
                            }
                        });
            }

            @Override
            public void onError(Throwable e) {
                Log.e(LOG_TAG, "Unable to save Meal to Parse: " + e.getMessage());
            }

            @Override
            public void onNext(ParseObject parseObject) {
                String className = parseObject.getClassName();
                if (className.equals(Place.ParseClassName)) {
                    placeParseObject = parseObject;
                    inProgressUploads.remove(placeId);
                } else if (className.equals(BloodSugar.ParseClassName)) {
                    beforeSugarParseObject = parseObject;
                    inProgressUploads.remove(beforeSugarId);
                }
            }
        });
    }

    public void uploadBloodSugar(final BloodSugar sugar) {
        final String sugarId = sugar.getId();
        this.getUploadedObjectObservable(sugarId, sugar).subscribe(new Action1<ParseObject>() {
            @Override
            public void call(ParseObject parseObject) {
                parseObject.saveInBackground();
                inProgressUploads.remove(sugarId);
            }
        });
    }

    /** Helpers */
    private synchronized Observable<ParseObject> getUploadedObjectObservable(String localId,
                                                                             final Object toUpload,
                                                                             final Object... args) {
        if (this.inProgressUploads.containsKey(localId)) {
            return this.inProgressUploads.get(localId);
        } else {
            Observable<ParseObject> observable = Observable.create(new Observable.OnSubscribe<ParseObject>() {
                @Override
                public void call(final Subscriber<? super ParseObject> subscriber) {
                    if (toUpload == null) {
                        subscriber.onError(new IllegalArgumentException("Object to upload must not be null"));
                        return;
                    }
                    if (toUpload instanceof Place) {
                        PlaceFactory.ParseObjectFromPlace((Place)toUpload, createParseObjectReadyAction(subscriber));
                    } else if (toUpload instanceof Meal) {
                        if (args == null || args.length < 2 ||
                                !(args[0] instanceof ParseObject)) {
                            subscriber.onError(new IllegalArgumentException("Invalid specific arguments"));
                            return;
                        }
                        MealFactory.ParseObjectFromMeal((Meal)toUpload,
                                (ParseObject)args[0],
                                (ParseObject)args[1],
                                createParseObjectReadyAction(subscriber));
                    } else if (toUpload instanceof BloodSugar) {
                        BloodSugarFactory.ParseObjectFromBloodSugar((BloodSugar)toUpload,
                                createParseObjectReadyAction(subscriber));
                    } else {
                        subscriber.onError(new IllegalArgumentException("Invalid type for upload object"));
                    }
                }
            });
            this.inProgressUploads.put(localId, observable);
            return observable;
        }
    }

    private Action2<ParseObject, Boolean> createParseObjectReadyAction(final Subscriber<? super ParseObject> subscriber) {
        return new Action2<ParseObject, Boolean>() {
                            @Override
                            public void call(final ParseObject parseObject, Boolean created) {
                                if (parseObject == null) {
                                    subscriber.onError(new RuntimeException("Unable to create ParseObject"));
                                    return;
                                }

                                if (created) {
                                    parseObject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                subscriber.onNext(parseObject);
                                                subscriber.onCompleted();
                                            } else {
                                                subscriber.onError(e);
                                            }
                                        }
                                    });
                                } else {
                                    subscriber.onNext(parseObject);
                                    subscriber.onCompleted();
                                }
                            }
                        };
    }
}
