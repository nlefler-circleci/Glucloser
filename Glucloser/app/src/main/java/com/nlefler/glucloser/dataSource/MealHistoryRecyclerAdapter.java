package com.nlefler.glucloser.dataSource;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nlefler.glucloser.R;
import com.nlefler.glucloser.models.Meal;

import java.util.List;

/**
 * Created by Nathan Lefler on 12/25/14.
 */
public class MealHistoryRecyclerAdapter extends RecyclerView.Adapter<MealHistoryRecyclerAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected Meal meal;
        protected TextView placeName;
        protected TextView carbsValue;
        protected TextView insulinValue;
        protected View.OnClickListener clickListener;

        public ViewHolder(View itemView) {
            super(itemView);

            this.placeName = (TextView)itemView.findViewById(R.id.meal_detail_card_place_name);
            this.carbsValue = (TextView)itemView.findViewById(R.id.meal_detail_card_carbs_value);
            this.insulinValue = (TextView)itemView.findViewById(R.id.meal_detail_card_insulin_value);
            this.clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            };
            itemView.setOnClickListener(this.clickListener);
        }
    }

    private List<Meal> meals;

    public MealHistoryRecyclerAdapter(List<Meal> meals) {
        this.meals = meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.meal_detail_card, viewGroup, false);

        // Setup view

        return new ViewHolder(view);
    }

    // Replaces the contents of a view (invoked by the view holder)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        if (i >= this.meals.size()) {
            return;
        }

        Meal meal = this.meals.get(i);
        viewHolder.meal = meal;
        if (meal.getPlace() != null) {
            viewHolder.placeName.setText(meal.getPlace().getName());
        }
        viewHolder.carbsValue.setText(String.valueOf(meal.getCarbs()));
        viewHolder.insulinValue.setText(String.valueOf(meal.getInsulin()));
    }

    @Override
    public int getItemCount() {
        return this.meals.size();
    }
}
