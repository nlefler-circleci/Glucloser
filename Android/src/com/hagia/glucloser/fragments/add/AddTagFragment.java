package com.hagia.glucloser.fragments.add;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.hagia.glucloser.GlucloserActivity;
import com.hagia.glucloser.types.Tag;
import com.hagia.hnotificationcenter.NotificationCenter;
import com.hagia.glucloser.R;
import com.hagia.glucloser.util.TagUtil;
import com.hagia.glucloser.util.database.save.SaveManager;

public class AddTagFragment extends Fragment {

	public static final String TAG_KEY = "tag";

	private static final String LOG_TAG = "Pump_Add_Tag_Activity";

	private Tag tag;

	private AutoCompleteTextView tagNameInput;
	private ArrayAdapter<String> tagNameAdapter;
	private List<String> tagNamesForAutoComplete;
	private Button saveButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		tagNamesForAutoComplete = new ArrayList<String>();
		tagNameAdapter = new ArrayAdapter<String>(getActivity(), R.layout.autocomplete_layout, tagNamesForAutoComplete);
		tagNameAdapter.setNotifyOnChange(true);

		new AsyncTask<Void, Void, List<String>>() {			
			@Override
			protected List<String> doInBackground(Void... params) {
				return TagUtil.getAllTagNames();
			}

			protected void onPostExecute(List<String> result) {
				Log.i(LOG_TAG, "Updating autocomplete list with food names");
				tagNamesForAutoComplete.clear();
				tagNamesForAutoComplete.addAll(result);
				tagNameAdapter.notifyDataSetChanged();
			}
		}.execute();

		// Setup state
		Bundle state = savedInstanceState != null ?
				savedInstanceState : 
					(getArguments() != null ? getArguments() : new Bundle());
		populateStateFromBundle(state);

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.add_tag_view, container, false);

		tagNameInput = (AutoCompleteTextView)view.findViewById(R.id.add_tag_view_tag_input);
		tagNameInput.setThreshold(0);
		tagNameInput.setAdapter(tagNameAdapter);
		tagNameInput.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> view, View childView, int arg2,
					long arg3) {
				String text = ((TextView)childView).getText().toString();
				tagNameInput.setText(text);
			}
		});

		saveButton = (Button) view.findViewById(R.id.add_tag_view_save_button);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int id = v.getId();

				if (id == R.id.add_tag_view_save_button) {	
					if (validateData()) {
						String tagName = tagNameInput.getText().toString();

						tag.name = tagName;

						NotificationCenter.getInstance().postNotificationWithArguments(SaveManager.SAVE_TAG_NOTIFICATION, tag);
					}

					GlucloserActivity.getPumpActivity().popFragmentStack();
				}
			}

		});

		// Setup state
		Bundle state = savedInstanceState != null ?
				savedInstanceState : 
					(getArguments() != null ? getArguments() : new Bundle());
		populateStateFromBundle(state);

		return view;
	}

	@Override
	public void onResume() {
		InputMethodManager manager = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.showSoftInput(tagNameInput, InputMethodManager.SHOW_FORCED);

		super.onResume();
	}

	@Override
	public void onPause() {
		InputMethodManager manager = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(tagNameInput.getApplicationWindowToken(), 0);

		super.onPause();
	}

	private void populateStateFromBundle(Bundle bundle) {
		if (bundle.containsKey(TAG_KEY)) {
			tag = (Tag)bundle.getSerializable(TAG_KEY);
			tagNameInput.setText(tag.name);
		} else {
			tag = new Tag();
		}
	}

	private boolean validateData() {
		return !tagNameInput.getText().equals("");
	}
}
