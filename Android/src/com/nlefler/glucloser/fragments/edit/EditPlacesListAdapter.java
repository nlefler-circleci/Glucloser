package com.nlefler.glucloser.fragments.edit;

import android.database.DataSetObserver;
import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.R;
import com.nlefler.glucloser.util.LocationUtil;
import com.nlefler.glucloser.model.place.PlaceUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Nathan Lefler on 7/27/13.
 */
public class EditPlacesListAdapter implements ListAdapter {
   	private static final String LOG_TAG = "Pump_Edit_Places_List_Adapter";

	private AsyncTask<Void, List<Place>, List<Place>> placesDataSource;
	private List<Place> placeResults = new ArrayList<Place>();
    private LayoutInflater layoutInflater = null;
    private Set<DataSetObserver> dataObservers = null;

    public EditPlacesListAdapter(LayoutInflater inflater) {
        layoutInflater = inflater;
        dataObservers = new HashSet<DataSetObserver>();
        placesDataSource = constructPlacesDataSource();
        placesDataSource.execute();
    }

	public void cancel() {
		if (placesDataSource != null) {
			placesDataSource.cancel(true);
            placesDataSource = null;
		}
	}

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        dataObservers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        dataObservers.remove(observer);
    }

    @Override
    public boolean isEmpty() {
        return placeResults.isEmpty();
    }

	@Override
	public int getCount() {
        return placeResults.size();
	}

	@Override
	public Object getItem(int position) {
        return placeResults.get(position);
	}

	@Override
	public long getItemId(int position) {
        return placeResults.get(position).hashCode();
	}

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        return getViewForPlace((Place)getItem(position), convertView, parent);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

	private AsyncTask<Void, List<Place>, List<Place>> constructPlacesDataSource() {
		return new AsyncTask<Void, List<Place>, List<Place>>() {
			@Override
			protected List<Place> doInBackground(Void... params) {
                return PlaceUtil.getAllPlacesSortedByName();
			}

			@Override
			protected void onPostExecute(List<Place> result) {
				placeResults = result;
				notifyDataSetChanged();
			}

		};
	}

    private void notifyDataSetChanged() {
        if (dataObservers != null) {
            for (DataSetObserver observer : dataObservers) {
                observer.onChanged();
            }
        }
    }

	private View getViewForPlace(Place place, View convertView, ViewGroup parent) {
		long start = System.currentTimeMillis();
		LinearLayout theView = null;

		if (place == null) {
			return convertView;
		}

		// TODO: Probably not unique
		double requestId = place.name.hashCode() +
				place.location.getLatitude() +
				place.location.getLongitude();

		if (convertView == null || convertView.getId() != R.layout.place_line_item) {
			theView = (LinearLayout)layoutInflater.inflate(R.layout.place_line_item, null);
		} else {
			if (((Double)convertView.getTag()) == requestId) {
				return convertView;
			}
            theView = (LinearLayout) convertView;
		}
		theView.setTag(requestId);

		TextView nameView = (TextView)theView.findViewById(R.id.place_line_item_place_name);
		TextView addressView = (TextView)theView.findViewById(R.id.place_line_item_place_address);

		nameView.setText(place.name);

		String readableAddress = place.readableAddress;
		if (readableAddress == null || readableAddress.equals("")) {
			List<Address> addresses = LocationUtil.getAddressFromLocation(place.location, 1);
			if (!addresses.isEmpty()) {
				// TODO drill up until we find a non-null address line value
				addressView.setText(addresses.get(0).getAddressLine(0));
				// TODO This is a temporary workaround
				PlaceUtil.updateReadableLocation(place);
				PlaceUtil.savePlace(place);
			}
		}

		Log.d(LOG_TAG, "Place view took " + (System.currentTimeMillis() - start));

		return theView;
	}
}
