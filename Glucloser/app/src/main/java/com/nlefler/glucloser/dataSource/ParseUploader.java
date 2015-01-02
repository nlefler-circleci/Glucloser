package com.nlefler.glucloser.dataSource;

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
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Action2;

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
        Action1<ParseObject> action = new Action1<ParseObject>() {
            @Override
            public void call(ParseObject placeObject) {
                inProgressUploads.remove(placeId);
                final String mealId = meal.getMealId();
                getUploadedObjectObservable(mealId, meal, placeObject).subscribe(new Action1<ParseObject>() {
                    @Override
                    public void call(ParseObject mealObject) {
                        mealObject.saveInBackground();
                        inProgressUploads.remove(mealId);
                    }
                });
            }
        };
        if (place != null) {
            getUploadedObjectObservable(placeId, place).subscribe(action);
        } else {
            action.call(null);
        }
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
                        if (args == null || args.length < 1 || !(args[0] instanceof ParseObject)) {
                            subscriber.onError(new IllegalArgumentException("Invalid specific arguments"));
                            return;
                        }
                        MealFactory.ParseObjectFromMeal((Meal)toUpload, (ParseObject)args[0], createParseObjectReadyAction(subscriber));
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
                                if (parseObject != null && created) {
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
