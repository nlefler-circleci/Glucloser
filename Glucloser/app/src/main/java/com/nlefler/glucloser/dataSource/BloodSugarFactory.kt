package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log
import com.nlefler.glucloser.models.BloodSugar
import com.nlefler.glucloser.models.BloodSugarParcelable

import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery

import java.util.Date
import java.util.UUID

import io.realm.Realm
import io.realm.RealmQuery
import rx.functions.Action2

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public class BloodSugarFactory {
    companion object {
        private val LOG_TAG = "BloodSugarFactory"

        public fun BloodSugar(ctx: Context): BloodSugar {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val sugar = BloodSugarForBloodSugarId(null, realm, true)!!
            realm.commitTransaction()

            return sugar
        }

        public fun BloodSugarFromParcelable(parcelable: BloodSugarParcelable, ctx: Context): BloodSugar {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val sugar = BloodSugarForBloodSugarId(parcelable.getId(), realm, true)!!
            sugar.setValue(parcelable.getValue())
            sugar.setDate(parcelable.getDate())
            realm.commitTransaction()

            return sugar
        }

        public fun ParcelableFromBloodSugar(sugar: BloodSugar): BloodSugarParcelable {
            val parcelable = BloodSugarParcelable()
            parcelable.setId(sugar.getId())
            parcelable.setValue(sugar.getValue())
            parcelable.setDate(sugar.getDate())
            return parcelable
        }

        public fun AreBloodSugarsEqual(sugar1: BloodSugar?, sugar2: BloodSugar?): Boolean {
            if (sugar1 == null || sugar2 == null) {
                return false
            }

            val valueOk = sugar1.getValue() == sugar2.getValue()
            val dateOK = sugar1.getDate() == sugar2.getDate()

            return valueOk && dateOK
        }

        internal fun BloodSugarFromParseObject(parseObject: ParseObject?, realm: Realm?): BloodSugar? {
            if (parseObject == null || realm == null) {
                Log.e(LOG_TAG, "Can't create BloodSugar from Parse object, null")
                return null
            }
            val sugarId = parseObject.getString(BloodSugar.IdFieldName)
            if (sugarId == null || sugarId.isEmpty()) {
                Log.e(LOG_TAG, "Can't create BloodSugar from Parse object, no id")
            }
            val sugarValue = parseObject.getInt(BloodSugar.ValueFieldName)
            val sugarDate = parseObject.getDate(BloodSugar.DateFieldName)

            realm.beginTransaction()
            val sugar = BloodSugarForBloodSugarId(sugarId, realm, true)!!
            if (sugarValue >= 0 && sugarValue != sugar.getValue()) {
                sugar.setValue(sugarValue)
            }
            if (sugarDate != null) {
                sugar.setDate(sugarDate)
            }
            realm.commitTransaction()

            return sugar
        }

        /**
         * Fetches or creates a ParseObject representing the provided BloodSugar
         * @param bloodSugar
         * *
         * @param action Returns the ParseObject, and true if the object was created and should be saved.
         */
        internal fun ParseObjectFromBloodSugar(bloodSugar: BloodSugar, action: Action2<ParseObject?, Boolean>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to create Parse object from BloodSugar, action null")
                return
            }
            if (bloodSugar.getId()?.isEmpty() ?: true) {
                Log.e(LOG_TAG, "Unable to create Parse object from BloodSugar, blood sugar null or no id")
                action.call(null, false)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(BloodSugar.ParseClassName)
            parseQuery.whereEqualTo(BloodSugar.IdFieldName, bloodSugar.getId())

            parseQuery.findInBackground({parseObjects: List<ParseObject>, e: ParseException? ->
                val parseObject: ParseObject
                var created = false
                if (parseObjects.isEmpty()) {
                    parseObject = ParseObject(BloodSugar.ParseClassName)
                    created = true
                } else {
                    parseObject = parseObjects.get(0)
                }
                parseObject.put(BloodSugar.IdFieldName, bloodSugar.getId())
                parseObject.put(BloodSugar.ValueFieldName, bloodSugar.getValue())
                parseObject.put(BloodSugar.DateFieldName, bloodSugar.getDate())
                action.call(parseObject, created)
            })
        }

        private fun BloodSugarForBloodSugarId(id: String?, realm: Realm, create: Boolean): BloodSugar? {
            if (create && (id == null || id.isEmpty())) {
                val sugar = realm.createObject<BloodSugar>(javaClass<BloodSugar>())
                sugar.setId(UUID.randomUUID().toString())
                return sugar
            }

            val query = realm.where<BloodSugar>(javaClass<BloodSugar>())

            query.equalTo(BloodSugar.IdFieldName, id)
            var result: BloodSugar? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<BloodSugar>(javaClass<BloodSugar>())
                result!!.setId(id)
            }

            return result
        }
    }
}
