package com.nlefler.glucloser.fragments.home.listItems;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nlefler.glucloser.R;
import com.nlefler.glucloser.types.Food;
import com.nlefler.glucloser.types.Place;

import java.util.List;

/**
 * Created by lefler on 11/3/13.
 */
public class PopularMealListItem implements HomeListItem {
    private Place place;
    private List<Food> foods;

    public PopularMealListItem(Place place, List<Food> foods) {
        this.place = place;
        this.foods = foods;
    }

    public List<Food> getFoods() { return foods; }
    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public long getItemId() {
        return foods.hashCode();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup parentView) {
		if (place == null || foods == null) {
			return convertView;
		}
        LinearLayout theView = null;

		double requestId = place.hashCode() * 31 + foods.hashCode() * 31;

		if (convertView == null || convertView.getId() != R.layout.home_line_item) {
			theView = (LinearLayout)inflater.inflate(R.layout.home_line_item, null);
		} else {
			theView = (LinearLayout) convertView;
			if (((Double)theView.getTag()) == requestId) {
				return theView;
			}
		}

		LinearLayout mealItem = (LinearLayout)inflater.inflate(
				R.layout.popular_meal_line_item, null);
        theView.setTag(requestId);
		theView.addView(mealItem);

		TextView placeName = (TextView) mealItem.findViewById(R.id.popular_meal_place_name);
		TextView placeAddress = (TextView) mealItem.findViewById(R.id.popular_meal_place_address);
		LinearLayout foodsLayout = (LinearLayout) mealItem.findViewById(R.id.popular_meal_foods_container);

		placeName.setText(place.name);

		if (place.readableAddress != null) {
			placeAddress.setText(place.readableAddress);
		}

		for (Food food : foods) {
			RelativeLayout foodLayout = (RelativeLayout) inflater.inflate(
					R.layout.food_line_item, null);
			TextView foodName = (TextView) foodLayout.findViewById(R.id.food_line_item_food_name);
			TextView carbsValue = (TextView) foodLayout.findViewById(R.id.food_line_item_carbs_value);

			foodName.setText(food.name);
			carbsValue.setText(String.valueOf(food.carbs));
			foodsLayout.addView(foodLayout);
		}

		return theView;
    }
}
