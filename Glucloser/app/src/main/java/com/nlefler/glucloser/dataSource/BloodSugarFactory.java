package com.nlefler.glucloser.dataSource;

import com.nlefler.glucloser.models.BloodSugar;
import com.nlefler.glucloser.models.BloodSugarParcelable;

import java.util.Date;

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public class BloodSugarFactory {
    private static final String LOG_TAG = "BloodSugarFactory";

    public static BloodSugar BloodSugar(int value, Date date) {
        BloodSugar sugar = new BloodSugar();
        sugar.setDate(date);
        sugar.setValue(value);
        return sugar;
    }

    public static BloodSugar BloodSugarFromParcelable(BloodSugarParcelable sugar) {
        return BloodSugar(sugar.getValue(), sugar.getDate());
    }

    public static BloodSugarParcelable ParcelableFromBloodSugar(BloodSugar sugar) {
        BloodSugarParcelable parcelable = new BloodSugarParcelable();
        parcelable.setValue(sugar.getValue());
        parcelable.setDate(sugar.getDate());
        return parcelable;
    }
}
