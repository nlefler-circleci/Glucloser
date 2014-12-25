package com.nlefler.glucloser.ui;

import android.os.Bundle;
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

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealDetailsFragment extends Fragment {
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

        if ((bundle != null && bundle.getParcelable(MealDetailMealBundleKey) != null)) {
            this.meal = MealFactory.MealFromParcelable((MealParcelable)bundle.getParcelable(MealDetailMealBundleKey));
        } else if (getArguments() != null && getArguments().getParcelable(MealDetailMealBundleKey) != null) {
            this.meal = MealFactory.MealFromParcelable((MealParcelable)bundle.getParcelable(MealDetailMealBundleKey));
        } else {
            this.meal = MealFactory.Meal();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_meal_edit_details, container, false);

        this.carbValueField = (EditText)rootView.findViewById(R.id.meal_edit_detail_carb_value);
        this.insulinValueField = (EditText)rootView.findViewById(R.id.meal_edit_detail_insulin_value);
        this.saveButton = (Button)rootView.findViewById(R.id.meal_edit_detail_save_button);

        return rootView;
    }

    public void saveMeal(View view) {
        if (!(getActivity() instanceof MealDetailDelegate)) {
            return;
        }

        this.meal.setCarbs(Integer.valueOf(this.carbValueField.getText().toString()));
        this.meal.setInsulin(Float.valueOf(this.insulinValueField.getText().toString()));

        ((MealDetailDelegate)getActivity()).mealUpdated(this.meal);
    }
}
