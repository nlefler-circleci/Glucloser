package com.nlefler.glucloser.dataSource;

import com.nlefler.glucloser.models.Meal;
import com.nlefler.glucloser.models.Place;
import com.parse.ParseObject;

import rx.functions.Action1;

/**
 * Created by Nathan Lefler on 12/30/14.
 */
public class ParseUploader {
    private static String LOG_TAG = "ParseUploader";

    public static void UploadPlace(Place place) {
        PlaceFactory.ParseObjectFromPlace(place, new Action1<ParseObject>() {
            @Override
            public void call(ParseObject parseObject) {
                if (parseObject != null) {
                    parseObject.saveInBackground();
                }
            }
        });
    }

    public static void UploadMeal(Meal meal) {
        MealFactory.ParseObjectFromMeal(meal, new Action1<ParseObject>() {
            @Override
            public void call(ParseObject parseObject) {
                if (parseObject != null) {
                    parseObject.saveInBackground();
                }
            }
        });
    }
}
