package com.hagia.glucloser.fragments.edit;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.hagia.glucloser.detail.PlaceDetailActivity;
import com.hagia.glucloser.types.Place;

/**
 * Created by Nathan Lefler on 7/27/13.
 */
public class EditPlacesFragment extends ListFragment {
    private static final String LOG_TAG = "Pump_Edit_Places_Fragment";

    private EditPlacesListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        listAdapter = new EditPlacesListAdapter(getActivity().getLayoutInflater());
        setListAdapter(listAdapter);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Place place = (Place)getListAdapter().getItem(position);

        Intent detailIntent = new Intent(getActivity(), PlaceDetailActivity.class);
        detailIntent.putExtra(PlaceDetailActivity.PLACE_EXTRA_KEY, place);
        getActivity().startActivity(detailIntent);
    }
}
