package com.nlefler.glucloser.ui

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView

import com.nlefler.glucloser.R
import com.nlefler.glucloser.models.BloodSugarParcelable
import com.nlefler.glucloser.models.BolusEventDetailDelegate
import com.nlefler.glucloser.models.BolusEventParcelable

import java.util.Date

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealDetailsFragment : Fragment(), View.OnClickListener {

    private var placeName: String? = null
    private var bolusEventParcelable: BolusEventParcelable? = null

    private var carbValueField: EditText? = null
    private var insulinValueField: EditText? = null
    private var beforeSugarValueField: EditText? = null
    private var correctionValueBox: CheckBox? = null

    override fun onCreate(bundle: Bundle?) {
        super<Fragment>.onCreate(bundle)

        this.bolusEventParcelable = getBolusEventParcelableFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras())
        this.placeName = getPlaceNameFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_meal_edit_details, container, false)

        val placeNameField = rootView.findViewById(R.id.meal_edit_detail_place_name) as TextView
        this.carbValueField = rootView.findViewById(R.id.meal_edit_detail_carb_value) as EditText
        this.insulinValueField = rootView.findViewById(R.id.meal_edit_detail_insulin_value) as EditText
        this.beforeSugarValueField = rootView.findViewById(R.id.meal_edit_detail_blood_sugar_before_value) as EditText
        this.correctionValueBox = rootView.findViewById(R.id.meal_edit_detail_correction_value) as CheckBox
        val saveButton = rootView.findViewById(R.id.meal_edit_detail_save_button) as Button
        saveButton.setOnClickListener(this)

        if (this.placeName != null) {
            placeNameField.setText(this.placeName)
        }

        return rootView
    }

    /** OnClickListener  */
    override fun onClick(view: View) {
        if (getActivity() !is BolusEventDetailDelegate || this.bolusEventParcelable == null) {
            return
        }

        this.bolusEventParcelable!!.setDate(Date())

        val beforeSugarString = this.beforeSugarValueField!!.getText().toString()
        if (!beforeSugarString.isEmpty()) {
            val beforeSugarParcelable = BloodSugarParcelable()
            beforeSugarParcelable.setDate(this.bolusEventParcelable!!.getDate())
            beforeSugarParcelable.setValue(Integer.valueOf(beforeSugarString))
            this.bolusEventParcelable!!.setBeforeSugarParcelable(beforeSugarParcelable)
        }

        if (this.insulinValueField!!.getText() != null && this.insulinValueField!!.getText().length() > 0) {
            this.bolusEventParcelable!!.setInsulin(java.lang.Float.valueOf(this.insulinValueField!!.getText().toString()))
        }
        if (this.carbValueField!!.getText() != null && this.carbValueField!!.getText().length() > 0) {
            this.bolusEventParcelable!!.setCarbs(Integer.valueOf(this.carbValueField!!.getText().toString())!!)
        }
        this.bolusEventParcelable!!.setCorrection(this.correctionValueBox!!.isSelected())

        (getActivity() as BolusEventDetailDelegate).bolusEventDetailUpdated(this.bolusEventParcelable!!)
    }

    private fun getPlaceNameFromBundle(savedInstanceState: Bundle?, args: Bundle?, extras: Bundle?): String {
        for (bundle in array<Bundle?>(savedInstanceState, args, extras)) {
            if (bundle?.getParcelable<Parcelable>(MealDetailPlaceNameBundleKey) ?: null!= null) {
                return bundle?.getString(MealDetailPlaceNameBundleKey) ?: ""
            }
        }

        return ""
    }

    private fun getBolusEventParcelableFromBundle(savedInstanceState: Bundle?, args: Bundle?, extras: Bundle?): BolusEventParcelable? {
        for (bundle in array<Bundle?>(savedInstanceState, args, extras)) {
            if (bundle?.containsKey(MealDetailBolusEventParcelableBundleKey) ?: null != null) {
                return bundle!!.getParcelable<Parcelable>(MealDetailBolusEventParcelableBundleKey) as BolusEventParcelable?
            }
        }
        return null
    }

    companion object {
        private val LOG_TAG = "MealDetailsFragment"

        public val MealDetailPlaceNameBundleKey: String = "MealDetailPlaceNameBundleKey"
        public val MealDetailBolusEventParcelableBundleKey: String = "MealDetailBolusEventParcelableBundleKey"
    }
}
