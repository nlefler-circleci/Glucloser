package com.hagia.glucloser.fragments.history;


import android.app.Fragment;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hagia.glucloser.R;

public class HistoryFragment extends Fragment {
	private static final String LOG_TAG = "Pump_History_Activity";
	
	private View parentView = null;
	private HistoryListAdapter listAdapter;
	private ListView mealsListView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectNetwork()
        .penaltyLog()
        .build());
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.history_view, container, false);
		
		parentView = view;
		
		return view;
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {		
		super.onActivityCreated(savedInstanceState);
		
		listAdapter = new HistoryListAdapter(getActivity(),
				getActivity().getLayoutInflater());
		mealsListView = (ListView)parentView.findViewById(R.id.history_view_list_layout);
		mealsListView.setAdapter(listAdapter);
	}

}
