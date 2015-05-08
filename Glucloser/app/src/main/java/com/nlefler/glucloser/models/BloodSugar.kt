package com.nlefler.glucloser.models

import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.Ignore

/**
 * Created by Nathan Lefler on 1/4/15.
 */
public class BloodSugar : RealmObject() {
    public var id: String? = null
    public var value: Int = 0
    public var date: Date? = null

    companion object {
        Ignore
        public val ParseClassName: String = "BloodSugar"

        Ignore
        public val IdFieldName: String = "id"

        Ignore
        public val ValueFieldName: String = "value"

        Ignore
        public val DateFieldName: String = "date"
    }
}
