package com.nlefler.glucloser.dataSource;

import android.content.Context;

import com.nlefler.glucloser.models.Meal;
import com.nlefler.glucloser.models.MealParcelable;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealFactory {
    public static Meal Meal(Context ctx) {
        Realm realm = Realm.getInstance(ctx);

        realm.beginTransaction();
        Meal meal = CreateOrFetchForMealId(null, realm);
        meal.setMealId(UUID.randomUUID().toString());
        realm.commitTransaction();

        return meal;
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
        Meal meal = CreateOrFetchForMealId(parcelable.getMealId(), realm);
        meal.setInsulin(parcelable.getInsulin());
        meal.setMealId(parcelable.getMealId());
        meal.setCarbs(parcelable.getCarbs());
        meal.setPlace(parcelable.getPlace());
        meal.setCorrection(parcelable.getCorrection());
        meal.setBeforeSugar(parcelable.getBeforeSugar());
        realm.commitTransaction();

        return meal;
    }
    private static Meal CreateOrFetchForMealId(String id, Realm realm) {
        if (id == null || id.isEmpty()) {
            return realm.createObject(Meal.class);
        }

        RealmQuery<Meal> query = realm.where(Meal.class);

        query.equalTo(Meal.MealIdFieldName, id);
        Meal result = query.findFirst();

        if (result == null) {
            result = realm.createObject(Meal.class);
        }

        return result;
    }
}
