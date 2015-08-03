package com.nlefler.glucloser.ui

import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.nlefler.glucloser.R
import com.nlefler.glucloser.dataSource.FoodFactory
import com.nlefler.glucloser.dataSource.FoodListRecyclerAdapter
import com.nlefler.glucloser.models.BolusEventParcelable
import com.nlefler.glucloser.models.Food
import com.nlefler.glucloser.models.HasPlace
import com.nlefler.glucloser.ui.DividerItemDecoration
import java.lang
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
public class HistoricalBolusDetailActivityFragment : Fragment() {
    private var placeName: String? = null
    private var bolusEventParcelable: BolusEventParcelable? = null

    private var placeNameField: TextView? = null
    private var carbValueField: TextView? = null
    private var insulinValueField: TextView? = null
    private var beforeSugarValueField: TextView? = null
    private var correctionValueBox: CheckBox? = null

    private var foodListView: RecyclerView? = null
    private var foodListLayoutManager: RecyclerView.LayoutManager? = null
    private var foodListAdapter: FoodListRecyclerAdapter? = null
    private var foods: MutableList<Food> = ArrayList<Food>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_historical_bolus_detail, container, false)

        this.placeNameField = rootView.findViewById(R.id.historical_bolus_detail_place_name) as TextView
        this.carbValueField = rootView.findViewById(R.id.historical_bolus_detail_total_carb_value) as TextView
        this.insulinValueField = rootView.findViewById(R.id.historical_bolus_detail_total_insulin_value) as TextView
        this.beforeSugarValueField = rootView.findViewById(R.id.historical_bolus_detail_blood_sugar_before_value) as TextView
        this.correctionValueBox = rootView.findViewById(R.id.historical_bolus_detail_correction_value) as CheckBox

        this.foodListView = rootView.findViewById(R.id.historical_bolus_detail_food_list) as RecyclerView

        this.foodListLayoutManager = LinearLayoutManager(getActivity())
        this.foodListView!!.setLayoutManager(this.foodListLayoutManager)

        this.foodListAdapter = FoodListRecyclerAdapter(this.foods)
        this.foodListView!!.setAdapter(this.foodListAdapter)
        this.foodListView!!.addItemDecoration(DividerItemDecoration(getActivity()))

        setupWithBundle(null)

        return rootView
    }

    public fun setupWithBundle(bundle: Bundle?) {
        this.bolusEventParcelable = getBolusEventParcelableFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras())
        if (this.bolusEventParcelable is HasPlace) {
            this.placeName = (this.bolusEventParcelable as HasPlace).getPlace().getName()
        }
        else {
            this.placeName = ""
        }
        setupWithBundleAndPlace()
    }

    private fun setupWithBundleAndPlace() {
        this.placeNameField?.setText(this.placeName ?: "")

        this.carbValueField?.setText(lang.String.valueOf(this.bolusEventParcelable?.getCarbs() ?: 0))
        this.insulinValueField?.setText(lang.String.valueOf(this.bolusEventParcelable?.getInsulin() ?: 0))
        this.beforeSugarValueField?.setText(lang.String.valueOf(this.bolusEventParcelable?.getBeforeSugarParcelable() ?: 0))
        this.correctionValueBox?.setChecked(this.bolusEventParcelable?.isCorrection() ?: false)

        val foodsList = ArrayList<Food>()
        for (foodParcelable in this.bolusEventParcelable?.getFoodParcelables()?.iterator()) {
            foodsList.add(FoodFactory.FoodFromParcelable(foodParcelable, getActivity()))
        }
        this.foodListAdapter?.setFoods(foodsList)
    }

    private fun getBolusEventParcelableFromBundle(savedInstanceState: Bundle?, args: Bundle?, extras: Bundle?): BolusEventParcelable? {
        for (bundle in arrayOf<Bundle?>(savedInstanceState, args, extras)) {
            if (bundle?.containsKey(HistoricalBolusEventBolusDetailParcelableBundleKey) ?: null != null) {
                return bundle!!.getParcelable<Parcelable>(HistoricalBolusEventBolusDetailParcelableBundleKey) as BolusEventParcelable?
            }
        }
        return null
    }

    companion object {
        public val HistoricalBolusEventBolusDetailParcelableBundleKey: String = "HistoricalBolusDetailBolusEventParcelableBundleKey"
    }
}
