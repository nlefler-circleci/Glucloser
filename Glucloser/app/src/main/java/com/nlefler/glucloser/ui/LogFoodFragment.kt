package com.nlefler.glucloser.ui

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.nlefler.glucloser.R
import com.nlefler.glucloser.models.FoodDetailDelegate
import com.nlefler.glucloser.models.FoodParcelable

/**
 * Created by Nathan Lefler on 5/17/15.
 */
public class LogFoodFragment : Fragment(), View.OnClickListener {

    private var foodParcelable: FoodParcelable = FoodParcelable()

    private var foodNameField: EditText? = null
    private var carbValueField: EditText? = null

    override fun onCreate(bundle: Bundle?) {
        super<Fragment>.onCreate(bundle)

        this.foodParcelable = getFoodParcelableFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_log_food, container, false)

        this.foodNameField = rootView.findViewById(R.id.log_food_food_name_value) as EditText?
        this.carbValueField = rootView.findViewById(R.id.log_food_total_carb_value) as EditText?

        val saveButton = rootView.findViewById(R.id.log_food_save_button) as Button
        saveButton.setOnClickListener(this)

        return rootView
    }

    override fun onClick(view: View) {
        if (getActivity() !is FoodDetailDelegate) {
            return
        }

        this.foodParcelable.setFoodName(this.foodNameField!!.getText().toString())
        val carbString = this.carbValueField!!.getText().toString()
        if (carbString.length() > 0) {
            this.foodParcelable.setCarbs(Integer.valueOf(carbString))
        }

        (getActivity() as FoodDetailDelegate).foodDetailUpdated(this.foodParcelable)
        getActivity().finish()
    }

    private fun getFoodParcelableFromBundle(savedInstanceState: Bundle?, args: Bundle?, extras: Bundle?): FoodParcelable {
        for (bundle in array<Bundle?>(savedInstanceState, args, extras)) {
            if (bundle?.containsKey(FoodParcelableBundleKey) ?: null != null) {
                return bundle!!.getParcelable<Parcelable>(FoodParcelableBundleKey) as FoodParcelable
            }
        }
        return this.foodParcelable
    }

    companion object {
        private val FoodParcelableBundleKey: String = "FoodParcelableBundleKey"
    }
}