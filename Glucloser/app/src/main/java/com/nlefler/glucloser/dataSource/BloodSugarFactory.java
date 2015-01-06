package com.nlefler.glucloser.dataSource;

import android.content.Context;
import android.util.Log;

import com.nlefler.glucloser.models.BloodSugar;
import com.nlefler.glucloser.models.BloodSugarParcelable;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.functions.Action2;

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public class BloodSugarFactory {
    private static final String LOG_TAG = "BloodSugarFactory";

    public static BloodSugar BloodSugar(Context ctx) {
        Realm realm = Realm.getInstance(ctx);

        realm.beginTransaction();
        BloodSugar sugar = BloodSugarForBloodSugarId(null, realm, true);
        realm.commitTransaction();

        return sugar;
    }

    public static BloodSugar BloodSugarFromParcelable(BloodSugarParcelable parcelable, Context ctx) {
        Realm realm = Realm.getInstance(ctx);

        realm.beginTransaction();
        BloodSugar sugar = BloodSugarForBloodSugarId(parcelable.getId(), realm, true);
        sugar.setId(parcelable.getId());
        sugar.setValue(parcelable.getValue());
        sugar.setDate(parcelable.getDate());
        realm.commitTransaction();

        return sugar;
    }

    public static BloodSugarParcelable ParcelableFromBloodSugar(BloodSugar sugar) {
        BloodSugarParcelable parcelable = new BloodSugarParcelable();
        parcelable.setId(sugar.getId());
        parcelable.setValue(sugar.getValue());
        parcelable.setDate(sugar.getDate());
        return parcelable;
    }

    public static boolean AreBloodSugarsEqual(BloodSugar sugar1, BloodSugar sugar2) {
        if (sugar1 == null || sugar2 == null) {
            return false;
        }

        boolean valueOk = sugar1.getValue() == sugar2.getValue();
        boolean dateOK = sugar1.getDate().equals(sugar2.getDate());

        return valueOk && dateOK;
    }

    protected static BloodSugar BloodSugarFromParseObject(ParseObject parseObject, Realm realm) {
        if (parseObject == null || realm == null) {
            Log.e(LOG_TAG, "Can't create BloodSugar from Parse object, null");
            return null;
        }
        String sugarId = parseObject.getString(BloodSugar.IdFieldName);
        if (sugarId == null || sugarId.isEmpty()) {
            Log.e(LOG_TAG, "Can't create BloodSugar from Parse object, no id");
        }
        int sugarValue = parseObject.getInt(BloodSugar.ValueFieldName);
        Date sugarDate = parseObject.getDate(BloodSugar.DateFieldName);

        realm.beginTransaction();
        BloodSugar sugar = BloodSugarForBloodSugarId(sugarId, realm, true);
        if (sugarValue >= 0 && sugarValue != sugar.getValue()) {
            sugar.setValue(sugarValue);
        }
        if (sugarDate != null) {
            sugar.setDate(sugarDate);
        }
        realm.commitTransaction();

        return sugar;
    }

    /**
     * Fetches or creates a ParseObject representing the provided BloodSugar
     * @param bloodSugar
     * @param action Returns the ParseObject, and true if the object was created and should be saved.
     */
    protected static void ParseObjectFromBloodSugar(final BloodSugar bloodSugar,
                                              final Action2<ParseObject, Boolean> action) {
        if (action == null) {
            Log.e(LOG_TAG, "Unable to create Parse object from BloodSugar, action null");
            return;
        }
        if (bloodSugar == null || bloodSugar.getId() == null || bloodSugar.getId().isEmpty()) {
            Log.e(LOG_TAG, "Unable to create Parse object from BloodSugar, blood sugar null or no id");
            action.call(null, false);
            return;
        }

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(BloodSugar.ParseClassName);
        parseQuery.whereEqualTo(BloodSugar.IdFieldName, bloodSugar.getId());

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                ParseObject parseObject;
                boolean created = false;
                if (parseObjects.isEmpty()) {
                    parseObject = new ParseObject(BloodSugar.ParseClassName);
                    created = true;
                } else {
                    parseObject = parseObjects.get(0);
                }
                parseObject.put(BloodSugar.IdFieldName, bloodSugar.getId());
                parseObject.put(BloodSugar.ValueFieldName, bloodSugar.getValue());
                parseObject.put(BloodSugar.DateFieldName, bloodSugar.getDate());
                action.call(parseObject, created);
            }
        });
    }

    private static BloodSugar BloodSugarForBloodSugarId(String id, Realm realm, boolean create) {
        if (create && (id == null || id.isEmpty())) {
            BloodSugar sugar = realm.createObject(BloodSugar.class);
            sugar.setId(UUID.randomUUID().toString());
            return sugar;
        }

        RealmQuery<BloodSugar> query = realm.where(BloodSugar.class);

        query.equalTo(BloodSugar.IdFieldName, id);
        BloodSugar result = query.findFirst();

        if (result == null && create) {
            result = realm.createObject(BloodSugar.class);
            result.setId(id);
        }

        return result;
    }
}
