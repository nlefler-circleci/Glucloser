package com.nlefler.glucloser.ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView

import com.nlefler.glucloser.R
import com.nlefler.glucloser.activities.LogBolusEventActivity
import com.nlefler.glucloser.activities.LogFoodActivity
import com.nlefler.glucloser.dataSource.FoodFactory
import com.nlefler.glucloser.dataSource.FoodListRecyclerAdapter
import com.nlefler.glucloser.models.*

import java.util.ArrayList
import java.util.Date

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class BolusEventDetailsFragment : Fragment(), FoodDetailDelegate {

    private var placeName: String? = null
    private var bolusEventParcelable: BolusEventParcelable? = null

    private var carbValueField: EditText? = null
    private var insulinValueField: EditText? = null
    private var beforeSugarValueField: EditText? = null
    private var correctionValueBox: CheckBox? = null

    private var foodListView: RecyclerView? = null
    private var foodListLayoutManager: RecyclerView.LayoutManager? = null
    private var foodListAdapter: FoodListRecyclerAdapter? = null
    private var foods: MutableList<Food> = ArrayList<Food>()

    override fun onCreate(bundle: Bundle?) {
        super<Fragment>.onCreate(bundle)

        this.bolusEventParcelable = getBolusEventParcelableFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras())
        this.placeName = getPlaceNameFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_bolus_event_edit_details, container, false)

        val placeNameField = rootView.findViewById(R.id.meal_edit_detail_place_name) as TextView
        this.carbValueField = rootView.findViewById(R.id.meal_edit_detail_total_carb_value) as EditText
        this.insulinValueField = rootView.findViewById(R.id.meal_edit_detail_total_insulin_value) as EditText
        this.beforeSugarValueField = rootView.findViewById(R.id.meal_edit_detail_blood_sugar_before_value) as EditText
        this.correctionValueBox = rootView.findViewById(R.id.meal_edit_detail_correction_value) as CheckBox

        val addFoodButton = rootView.findViewById(R.id.meal_edit_detail_add_food_button) as Button
        addFoodButton.setOnClickListener {v: View -> addFoodClicked() }

        val saveButton = rootView.findViewById(R.id.meal_edit_detail_save_button) as Button
        saveButton.setOnClickListener {v: View -> saveEventClicked() }

        if (this.placeName != null) {
            placeNameField.setText(this.placeName)
        }

        this.foodListView = rootView.findViewById(R.id.bolus_event_detail_food_list) as RecyclerView

        this.foodListLayoutManager = LinearLayoutManager(getActivity())
        this.foodListView!!.setLayoutManager(this.foodListLayoutManager)

        this.foodListAdapter = FoodListRecyclerAdapter(this.foods)
        this.foodListView!!.setAdapter(this.foodListAdapter)
        this.foodListView!!.addItemDecoration(DividerItemDecoration(getActivity()))

        return rootView
    }

    override fun foodDetailUpdated(foodParcelable: FoodParcelable) {
        this.foods.add(FoodFactory.FoodFromParcelable(foodParcelable, getActivity()))
        this.foodListAdapter?.setFoods(this.foods)
    }

    internal fun addFoodClicked() {
        val activity = getActivity()
        if (activity is LogBolusEventActivity) {
            activity.launchLogFoodActivity()
        }
    }

    internal fun saveEventClicked() {
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
        for (bundle in arrayOf<Bundle?>(savedInstanceState, args, extras)) {
            if (bundle?.getParcelable<Parcelable>(BolusEventDetailPlaceNameBundleKey) ?: null!= null) {
                return bundle?.getString(BolusEventDetailPlaceNameBundleKey) ?: ""
            }
        }

        return ""
    }

    private fun getBolusEventParcelableFromBundle(savedInstanceState: Bundle?, args: Bundle?, extras: Bundle?): BolusEventParcelable? {
        for (bundle in arrayOf<Bundle?>(savedInstanceState, args, extras)) {
            if (bundle?.containsKey(BolusEventDetailBolusEventParcelableBundleKey) ?: null != null) {
                return bundle!!.getParcelable<Parcelable>(BolusEventDetailBolusEventParcelableBundleKey) as BolusEventParcelable?
            }
        }
        return null
    }

    companion object {
        private val LOG_TAG = "BolusEventDetailsFragment"

        public val BolusEventDetailPlaceNameBundleKey: String = "MealDetailPlaceNameBundleKey"
        public val BolusEventDetailBolusEventParcelableBundleKey: String = "MealDetailBolusEventParcelableBundleKey"
    }
}
