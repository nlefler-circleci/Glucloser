package com.nlefler.glucloser.dataSource

import android.content.Context
import android.util.Log

import com.nlefler.glucloser.models.BloodSugar
import com.nlefler.glucloser.models.Snack
import com.nlefler.glucloser.models.SnackParcelable
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
 * Created by Nathan Lefler on 4/25/15.
 */
public class SnackFactory {

    public fun AreSnacksEqual(Snack1: Snack?, Snack2: Snack?): Boolean {
        if (Snack1 == null || Snack2 == null) {
            return false
        }

        val idOK = Snack1.snackId == Snack2.snackId
        val carbsOK = Snack1.getCarbs() == Snack2.getCarbs()
        val insulinOK = Snack1.getInsulin() == Snack2.getInsulin()
        val correctionOK = Snack1.isCorrection() == Snack2.isCorrection()
        val beforeSugarOK = BloodSugarFactory.AreBloodSugarsEqual(Snack1.getBeforeSugar(), Snack2.getBeforeSugar())
        val dateOK = Snack1.getDate() == Snack2.getDate()

        return idOK && carbsOK && insulinOK && correctionOK && beforeSugarOK && dateOK
    }

    companion object {
        private val LOG_TAG = "SnackFactory"

        public fun Snack(ctx: Context): Snack {
            val realm = Realm.getInstance(ctx)

            realm.beginTransaction()
            val snack = SnackForSnackId(null, realm, true)!!
            realm.commitTransaction()

            return snack
        }

        public fun FetchSnack(id: String?, ctx: Context?, action: Action1<Snack>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to fetch Snack, action is null")
                return
            }
            if (id == null || id.isEmpty() || ctx == null) {
                Log.e(LOG_TAG, "Unable to fetch Snack, invalid args")
                action.call(null)
                return
            }
            val realm = Realm.getInstance(ctx)
            realm.beginTransaction()
            val snack = SnackForSnackId(id, realm, false)
            if (snack != null) {
                action.call(snack)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Snack.ParseClassName)
            parseQuery.whereEqualTo(Snack.SnackIdFieldName, id)
            parseQuery.findInBackground(object : FindCallback<ParseObject>() {
                override fun done(parseObjects: List<ParseObject>, e: ParseException) {
                    if (!parseObjects.isEmpty()) {
                        val snackFromParse = SnackFromParseObject(parseObjects.get(0), realm)
                        action.call(snackFromParse)
                    } else {
                        action.call(null)
                    }
                }
            })
        }

        public fun ParcelableFromSnack(snack: Snack): SnackParcelable {
            val parcelable = SnackParcelable()
            parcelable.setCarbs(snack.getCarbs())
            parcelable.setInsulin(snack.getInsulin())
            parcelable.snackId = snack.snackId
            parcelable.setCorrection(snack.isCorrection())
            if (snack.getBeforeSugar() != null) {
                parcelable.setBeforeSugarParcelable(BloodSugarFactory.ParcelableFromBloodSugar(snack.getBeforeSugar()!!))
            }
            parcelable.setDate(snack.getDate())

            return parcelable
        }

        public fun SnackFromParcelable(parcelable: SnackParcelable, ctx: Context): Snack {
            val realm = Realm.getInstance(ctx)

            var beforeSugar: BloodSugar? = null
            if (parcelable.getBeforeSugarParcelable() != null) {
                beforeSugar = BloodSugarFactory.BloodSugarFromParcelable(parcelable.getBeforeSugarParcelable()!!, ctx)
            }

            realm.beginTransaction()
            val snack = SnackForSnackId(parcelable.snackId, realm, true)!!
            snack.setInsulin(parcelable.getInsulin())
            snack.setCarbs(parcelable.getCarbs())
            snack.setCorrection(parcelable.isCorrection())
            if (beforeSugar != null) {
                snack.setBeforeSugar(beforeSugar!!)
            }
            snack.setDate(parcelable.getDate())
            realm.commitTransaction()

            return snack
        }

        protected fun SnackFromParseObject(parseObject: ParseObject?, realm: Realm?): Snack? {
            if (parseObject == null || realm == null) {
                Log.e(LOG_TAG, "Can't create Snack from Parse object, null")
                return null
            }
            val SnackId = parseObject.getString(Snack.SnackIdFieldName)
            if (SnackId == null || SnackId.isEmpty()) {
                Log.e(LOG_TAG, "Can't create Snack from Parse object, no id")
            }
            val carbs = parseObject.getInt(Snack.CarbsFieldName)
            val insulin = parseObject.getDouble(Snack.InsulinFieldName).toFloat()
            val correction = parseObject.getBoolean(Snack.CorrectionFieldName)
            val SnackDate = parseObject.getDate(Snack.SnackDateFieldName)
            val beforeSugar = BloodSugarFactory.BloodSugarFromParseObject(parseObject.getParseObject(Snack.BeforeSugarFieldName), realm)

            realm.beginTransaction()
            val snack = SnackForSnackId(SnackId, realm, true)!!
            if (carbs >= 0 && carbs != snack.getCarbs()) {
                snack.setCarbs(carbs)
            }
            if (insulin >= 0 && snack.getInsulin() != insulin) {
                snack.setInsulin(insulin)
            }
            if (beforeSugar != null && !BloodSugarFactory.AreBloodSugarsEqual(snack.getBeforeSugar(), beforeSugar)) {
                snack.setBeforeSugar(beforeSugar)
            }
            if (snack.isCorrection() != correction) {
                snack.setCorrection(correction)
            }
            if (SnackDate != null) {
                snack.setDate(SnackDate)
            }
            realm.commitTransaction()

            return snack
        }

        /**
         * Fetches or creates a ParseObject representing the provided Snack
         * @param snack
         * *
         * @param action Returns the ParseObject, and true if the object was created and should be saved.
         */
        internal fun ParseObjectFromSnack(snack: Snack?, beforeSugarObject: ParseObject?, action: Action2<ParseObject, Boolean>?) {
            if (action == null) {
                Log.e(LOG_TAG, "Unable to create Parse object from Snack, action null")
                return
            }
            if (snack?.snackId?.isEmpty() ?: true) {
                Log.e(LOG_TAG, "Unable to create Parse object from Snack, Snack null or no id")
                action.call(null, false)
                return
            }

            val parseQuery = ParseQuery.getQuery<ParseObject>(Snack.ParseClassName)
            parseQuery.whereEqualTo(Snack.SnackIdFieldName, snack!!.snackId)

            parseQuery.findInBackground(object : FindCallback<ParseObject>() {
                override fun done(parseObjects: List<ParseObject>, e: ParseException) {
                    val parseObject: ParseObject
                    var created = false
                    if (parseObjects.isEmpty()) {
                        parseObject = ParseObject(Snack.ParseClassName)
                        created = true
                    } else {
                        parseObject = parseObjects.get(0)
                    }
                    parseObject.put(Snack.SnackIdFieldName, snack.snackId)
                    if (beforeSugarObject != null) {
                        parseObject.put(Snack.BeforeSugarFieldName, beforeSugarObject)
                    }
                    parseObject.put(Snack.CorrectionFieldName, snack.isCorrection())
                    parseObject.put(Snack.CarbsFieldName, snack.getCarbs())
                    parseObject.put(Snack.InsulinFieldName, snack.getInsulin())
                    parseObject.put(Snack.SnackDateFieldName, snack.getDate())
                    action.call(parseObject, created)
                }
            })
        }

        private fun SnackForSnackId(id: String?, realm: Realm, create: Boolean): Snack? {
            if (create && (id == null || id.isEmpty())) {
                val Snack = realm.createObject<Snack>(javaClass<Snack>())
                Snack.snackId = UUID.randomUUID().toString()
                return Snack
            }

            val query = realm.where<Snack>(javaClass<Snack>())

            query.equalTo(Snack.SnackIdFieldName, id)
            var result: Snack? = query.findFirst()

            if (result == null && create) {
                result = realm.createObject<Snack>(javaClass<Snack>())
                result!!.snackId = id
            }

            return result
        }
    }
}
