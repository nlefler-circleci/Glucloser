package com.nlefler.glucloser.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.nlefler.glucloser.R;
import com.nlefler.glucloser.actions.LogMealAction;
import com.nlefler.glucloser.dataSource.BloodSugarFactory;
import com.nlefler.glucloser.dataSource.MealFactory;
import com.nlefler.glucloser.dataSource.PlaceFactory;
import com.nlefler.glucloser.models.BloodSugar;
import com.nlefler.glucloser.models.Meal;
import com.nlefler.glucloser.models.MealDetailDelegate;
import com.nlefler.glucloser.models.MealParcelable;
import com.nlefler.glucloser.models.Place;
import com.nlefler.glucloser.models.PlaceParcelable;

import org.w3c.dom.Text;

import java.util.Date;

import io.realm.Realm;

/**
 * Created by Nathan Lefler on 12/24/14.
 */
public class MealDetailsFragment extends Fragment implements View.OnClickListener {
    private static String LOG_TAG = "MealDetailsFragment";

    public static final String MealDetailMealBundleKey = "MealDetailMealBundleKey";
    public static final String MealDetailPlaceBundleKey = "MealDetailPlaceBundleKey";

    private Meal meal;
    private Place place;

    private TextView placeNameField;
    private EditText carbValueField;
    private EditText insulinValueField;
    private EditText beforeSugarValueField;
    private CheckBox correctionValueBox;
    private Button saveButton;

    public MealDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        this.meal = getMealFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras());
        this.place = getPlaceFromBundle(bundle, getArguments(), getActivity().getIntent().getExtras());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_meal_edit_details, container, false);

        this.placeNameField = (TextView)rootView.findViewById(R.id.meal_edit_detail_place_name);
        this.carbValueField = (EditText)rootView.findViewById(R.id.meal_edit_detail_carb_value);
        this.insulinValueField = (EditText)rootView.findViewById(R.id.meal_edit_detail_insulin_value);
        this.beforeSugarValueField = (EditText)rootView.findViewById(R.id.meal_edit_detail_blood_sugar_before_value);
        this.correctionValueBox = (CheckBox)rootView.findViewById(R.id.meal_edit_detail_correction_value);
        this.saveButton = (Button)rootView.findViewById(R.id.meal_edit_detail_save_button);
        this.saveButton.setOnClickListener(this);

        if (this.place != null && this.place.getName() != null) {
            this.placeNameField.setText(this.place.getName());
        }

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
        String beforeSugarString = this.beforeSugarValueField.getText().toString();
        int beforeSugarValue = -1;
        if (beforeSugarString != null && !beforeSugarString.isEmpty()) {
            beforeSugarValue = Integer.valueOf(beforeSugarString);
        }
        Date mealDate = new Date();

        Realm realm  = Realm.getInstance(getActivity());

        BloodSugar beforeSugar = BloodSugarFactory.BloodSugar(getActivity());
        realm.beginTransaction();
        beforeSugar.setValue(beforeSugarValue);
        beforeSugar.setDate(mealDate);
        realm.commitTransaction();

        realm.beginTransaction();
        this.meal.setCarbs(Integer.valueOf(this.carbValueField.getText().toString()));
        this.meal.setInsulin(Float.valueOf(this.insulinValueField.getText().toString()));
        this.meal.setCorrection(this.correctionValueBox.isSelected());
        if (beforeSugarValue >= 0) {
            this.meal.setBeforeSugar(beforeSugar);
        }
        this.meal.setMealDate(mealDate);
        realm.commitTransaction();

        ((MealDetailDelegate)getActivity()).mealUpdated(this.meal);
    }

    private Meal getMealFromBundle(Bundle savedInstanceState, Bundle args, Bundle extras) {
        Parcelable mealParcelable = null;
        for (Bundle bundle : new Bundle[] {savedInstanceState, args, extras}) {
            if (bundle == null) {
                continue;
            }

            if (bundle.getParcelable(MealDetailMealBundleKey) != null) {
                mealParcelable = bundle.getParcelable(MealDetailMealBundleKey);
                if (mealParcelable != null) {
                    return MealFactory.MealFromParcelable((MealParcelable) mealParcelable, getActivity());
                }
            }
        }

        return null;
    }

    private Place getPlaceFromBundle(Bundle savedInstanceState, Bundle args, Bundle extras) {
        Parcelable placeParcelable = null;
        for (Bundle bundle : new Bundle[] {savedInstanceState, args, extras}) {
            if (bundle == null) {
                continue;
            }

            if (bundle.getParcelable(MealDetailPlaceBundleKey) != null) {
                placeParcelable = bundle.getParcelable(MealDetailPlaceBundleKey);
                if (placeParcelable != null) {
                    return PlaceFactory.PlaceFromParcelable((PlaceParcelable) placeParcelable, getActivity());
                }
            }
        }

        return null;
    }
}
