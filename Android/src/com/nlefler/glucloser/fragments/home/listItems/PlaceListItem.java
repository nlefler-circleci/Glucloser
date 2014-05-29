package com.nlefler.glucloser.fragments.home.listItems;

import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nlefler.glucloser.R;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.util.LocationUtil;

import java.util.List;

/**
 * Created by lefler on 11/3/13.
 */
public class PlaceListItem implements HomeListItem {
    private Place place;
    private int count;

    public PlaceListItem(Place place, int count) {
        this.place = place;
        this.count = count;
    }

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public long getItemId() {
        return place.hashCode() * 31 + count * 31;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
		if (place == null) {
			return convertView;
		}

        LinearLayout theView = null;

		double requestId = place.id.hashCode() + count;

		if (convertView == null || convertView.getId() != R.layout.home_line_item) {
			theView = (LinearLayout)inflater.inflate(R.layout.home_line_item, null);
		} else {
			theView = (LinearLayout) convertView;
			if (((Double)theView.getTag()) == requestId) {
				return theView;
			}
		}
		LinearLayout placeItem = (LinearLayout)inflater.inflate(R.layout.place_line_item, null);
        theView.setTag(requestId);
		theView.addView(placeItem);

		TextView nameView = (TextView)placeItem.findViewById(R.id.place_line_item_place_name);
		TextView addressView = (TextView)placeItem.findViewById(R.id.place_line_item_place_address);
		TextView mealCountView = (TextView)placeItem.findViewById(R.id.place_line_item_place_meal_count);

		nameView.setText(place.name);

		String readableAddress = place.readableAddress;
		if (readableAddress == null || readableAddress.equals("")) {
			List<Address> addresses = LocationUtil.getAddressFromLocation(place.location, 1);
			if (!addresses.isEmpty()) {
				// TODO drill up until we find a non-null address line value
				addressView.setText(addresses.get(0).getAddressLine(0));
			}
		}

		if (count >= 0) {
			String mealsText = count > 1 ? count + " Meals" : count + " Meal";
			mealCountView.setText(mealsText);
		}

		return theView;
    }
}
