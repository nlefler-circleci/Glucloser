package com.nlefler.glucloser.dataSource;

import android.content.Context;
import android.util.Log;

import com.nlefler.glucloser.models.Meal;
import com.nlefler.glucloser.models.MealParcelable;
import com.nlefler.glucloser.models.Place;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.functions.Action1;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealFactory {
    private static String LOG_TAG = "MealFactory";

    public static Meal Meal(Context ctx) {
        Realm realm = Realm.getInstance(ctx);

        realm.beginTransaction();
        Meal meal = MealForMealId(null, realm, true);
        meal.setMealId(UUID.randomUUID().toString());
        realm.commitTransaction();

        return meal;
    }

    public static void FetchMeal(String id, final Context ctx, final Action1<Meal> action) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to fetch Meal, action is null");
            return;
        }
        if (id == null || id.isEmpty() || ctx == null) {
            Log.e(LOG_TAG, "Unable to fetch Meal, invalid args");
            action.call(null);
        }
        Realm realm = Realm.getInstance(ctx);
        realm.beginTransaction();
        Meal meal = MealForMealId(id, realm, false);
        if (meal != null) {
            action.call(meal);
            return;
        }

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(Meal.ParseClassName);
        parseQuery.whereEqualTo(Meal.MealIdFieldName, id);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (!parseObjects.isEmpty()) {
                    Realm realm = Realm.getInstance(ctx);
                    Meal meal = MealFromParseObject(parseObjects.get(0), realm);
                    action.call(meal);
                } else {
                    action.call(null);
                }
            }
        });
    }

    public static MealParcelable ParcelableFromMeal(Meal meal) {
        MealParcelable parcelable = new MealParcelable();
        parcelable.setPlace(meal.getPlace());
        parcelable.setCarbs(meal.getCarbs());
        parcelable.setInsulin(meal.getInsulin());
        parcelable.setMealId(meal.getMealId());
        parcelable.setCorrection(meal.getCorrection());
        parcelable.setBeforeSugar(meal.getBeforeSugar());

        return parcelable;
    }

    public static Meal MealFromParcelable(MealParcelable parcelable, Context ctx) {
        Realm realm = Realm.getInstance(ctx);

        realm.beginTransaction();
        Meal meal = MealForMealId(parcelable.getMealId(), realm, true);
        meal.setInsulin(parcelable.getInsulin());
        meal.setMealId(parcelable.getMealId());
        meal.setCarbs(parcelable.getCarbs());
        meal.setPlace(parcelable.getPlace());
        meal.setCorrection(parcelable.getCorrection());
        meal.setBeforeSugar(parcelable.getBeforeSugar());
        realm.commitTransaction();

        return meal;
    }

    protected static Meal MealFromParseObject(ParseObject parseObject, Realm realm) {
        if (parseObject == null || realm == null) {
            Log.e(LOG_TAG, "Can't create Meal from Parse object, null");
            return null;
        }
        String mealId = parseObject.getString(Meal.MealIdFieldName);
        if (mealId == null || mealId.isEmpty()) {
            Log.e(LOG_TAG, "Can't create Meal from Parse object, no id");
        }
        int carbs = parseObject.getInt(Meal.CarbsFieldName);
        float insulin = (float)parseObject.getDouble(Meal.InsulinFieldName);
        int beforeSugar = parseObject.getInt(Meal.BeforeSugarFieldName);
        boolean correction = parseObject.getBoolean(Meal.CorrectionFieldName);
        Place place = PlaceFactory.PlaceFromParseObject(parseObject.getParseObject(Meal.PlaceFieldName), realm);

        realm.beginTransaction();
        Meal meal = MealForMealId(mealId, realm, true);
        if (carbs >= 0 && carbs != meal.getCarbs()) {
            meal.setCarbs(carbs);
        }
        if (insulin >= 0 && meal.getInsulin() != insulin) {
            meal.setInsulin(insulin);
        }
        if (beforeSugar >= 0 && meal.getBeforeSugar() != beforeSugar) {
            meal.setBeforeSugar(beforeSugar);
        }
        if (meal.getCorrection() != correction) {
            meal.setCorrection(correction);
        }
        if (place != null && !PlaceFactory.ArePlacesEqual(place, meal.getPlace())) {
            meal.setPlace(place);
        }
        realm.commitTransaction();

        return meal;
    }

    protected static void ParseObjectFromMeal(final Meal meal, final Action1<ParseObject> action) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to create Parse object from Meal, action null");
            return;
        }
        if (meal == null || meal.getMealId() == null || meal.getMealId().isEmpty()) {
            Log.e(LOG_TAG, "Unable to create Parse object from Meal, meal null or no id");
            action.call(null);
            return;
        }

        Action1<ParseObject> onPlaceAction = new Action1<ParseObject>() {
            @Override
            public void call(final ParseObject placeParseObject) {
                        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(Meal.ParseClassName);
                parseQuery.whereEqualTo(Meal.MealIdFieldName, meal.getMealId());

                parseQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        ParseObject parseObject;
                        if (parseObjects.isEmpty()) {
                            parseObject = new ParseObject(Meal.ParseClassName);
                        } else {
                            parseObject = parseObjects.get(0);
                        }
                        parseObject.put(Meal.MealIdFieldName, meal.getMealId());
                        if (placeParseObject != null) {
                            parseObject.put(Meal.PlaceFieldName, placeParseObject);
                        }
                        parseObject.put(Meal.CorrectionFieldName, meal.getCorrection());
                        parseObject.put(Meal.BeforeSugarFieldName, meal.getBeforeSugar());
                        parseObject.put(Meal.CarbsFieldName, meal.getCarbs());
                        parseObject.put(Meal.InsulinFieldName, meal.getInsulin());
                        action.call(parseObject);
                    }
                });
            }
        };

        if (meal.getPlace() != null) {
            PlaceFactory.ParseObjectFromPlace(meal.getPlace(), onPlaceAction);
        } else {
            onPlaceAction.call(null);
        }
    }

    public boolean AreMealsEqual(Meal meal1, Meal meal2) {
        if (meal1 == null || meal2 == null) {
            return false;
        }

        boolean idOK = meal1.getMealId().equals(meal2.getMealId());
        boolean placeOK = PlaceFactory.ArePlacesEqual(meal1.getPlace(), meal2.getPlace());
        boolean carbsOK = meal1.getCarbs() == meal2.getCarbs();
        boolean insulinOK = meal1.getInsulin() == meal2.getInsulin();
        boolean correctionOK = meal1.getCorrection() == meal2.getCorrection();
        boolean beforeSugarOK = meal1.getBeforeSugar() == meal2.getBeforeSugar();

        return idOK && placeOK && carbsOK && insulinOK && correctionOK && beforeSugarOK;
    }

    private static Meal MealForMealId(String id, Realm realm, boolean create) {
        if (create && (id == null || id.isEmpty())) {
            return realm.createObject(Meal.class);
        }

        RealmQuery<Meal> query = realm.where(Meal.class);

        query.equalTo(Meal.MealIdFieldName, id);
        Meal result = query.findFirst();

        if (result == null && create) {
            result = realm.createObject(Meal.class);
        }

        return result;
    }
}
