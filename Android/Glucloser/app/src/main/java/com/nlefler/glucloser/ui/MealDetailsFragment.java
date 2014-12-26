package com.nlefler.glucloser.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.nlefler.glucloser.R;
import com.nlefler.glucloser.actions.LogMealAction;
import com.nlefler.glucloser.dataSource.MealFactory;
import com.nlefler.glucloser.models.Meal;
import com.nlefler.glucloser.models.MealDetailDelegate;
import com.nlefler.glucloser.models.MealParcelable;

import io.realm.Realm;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealDetailsFragment extends Fragment implements View.OnClickListener {
    private static String LOG_TAG = "MealDetailsFragment";

    private static final String MealDetailMealBundleKey = "MealDetailMealBundleKey";

    private Meal meal;

    private EditText carbValueField;
    private EditText insulinValueField;
    private Button saveButton;

    public MealDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Parcelable mealParcelable = null;
        if ((bundle != null && bundle.getParcelable(MealDetailMealBundleKey) != null)) {
            mealParcelable = bundle.getParcelable(MealDetailMealBundleKey);
        } else if (getArguments() != null && getArguments().getParcelable(MealDetailMealBundleKey) != null) {
            mealParcelable = bundle.getParcelable(MealDetailMealBundleKey);
        }
        if (mealParcelable != null) {
            this.meal = MealFactory.MealFromParcelable((MealParcelable)mealParcelable, getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_meal_edit_details, container, false);

        this.carbValueField = (EditText)rootView.findViewById(R.id.meal_edit_detail_carb_value);
        this.insulinValueField = (EditText)rootView.findViewById(R.id.meal_edit_detail_insulin_value);
        this.saveButton = (Button)rootView.findViewById(R.id.meal_edit_detail_save_button);
        this.saveButton.setOnClickListener(this);

        return rootView;
    }

    /** OnClickListener */
    public void onClick(View view) {
        if (!(getActivity() instanceof MealDetailDelegate)) {
            return;
        }

        if (this.meal == null) {
            this.meal = MealFactory.Meal(getActivity());
        }
        Realm realm  = Realm.getInstance(getActivity());
        realm.beginTransaction();
        this.meal.setCarbs(Integer.valueOf(this.carbValueField.getText().toString()));
        this.meal.setInsulin(Float.valueOf(this.insulinValueField.getText().toString()));
        realm.commitTransaction();

        ((MealDetailDelegate)getActivity()).mealUpdated(this.meal);
    }
}
