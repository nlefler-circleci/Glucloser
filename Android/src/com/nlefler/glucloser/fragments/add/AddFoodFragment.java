package com.nlefler.glucloser.fragments.add;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.nlefler.glucloser.thirdparty.zxing.IntentResult;
import com.nlefler.glucloser.types.Barcode;
import com.nlefler.glucloser.types.Food;
import com.nlefler.glucloser.types.Tag;
import com.nlefler.glucloser.util.BarcodeUtil;
import com.nlefler.hnotificationcenter.NotificationCenter;
import com.nlefler.glucloser.R;
import com.nlefler.glucloser.thirdparty.zxing.IntentIntegrator;
import com.nlefler.glucloser.util.FoodUtil;
import com.nlefler.glucloser.util.database.save.SaveManager;

public class AddFoodFragment extends Fragment {
	public static final String FOOD_KEY = "food";

	private final static String LOG_TAG = "Pump_Add_Food_Activity";

	private Food food;

	private AutoCompleteTextView foodNameInput;
	ArrayAdapter<String> foodNameAdapter;
	List<String> foodNamesForAutoComplete = new ArrayList<String>();

	private EditText carbValueInput;
	private ArrayList<Tag> tagList = new ArrayList<Tag>();

	private LinearLayout tagListLayout;
	private TextView addTagListItem;

	private CheckBox correctionCheckbox;

	private Button scanButton;
	private Button photoButton;
	private Button suggestButton;
	private Button saveButton;

	private Bitmap foodImage = null;
	private static final int CAMERA_REQUEST = 34341;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setupFoodNameAdapter();

		Bundle state = savedInstanceState != null ?
				savedInstanceState : 
					(getArguments() != null ? getArguments() : new Bundle());
		loadStateFromBundle(state);

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.add_food_view, container, false);

		// Setup views
		extractUIElementsFromView(view);

		Bundle state = savedInstanceState != null ?
				savedInstanceState : 
					(getArguments() != null ? getArguments() : new Bundle());
		loadStateFromBundle(state);

		configureUIElements();

		return view;
	}

	@Override
	public void onResume() {
		InputMethodManager manager = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.showSoftInput(foodNameInput, InputMethodManager.SHOW_FORCED);

		super.onResume();
	}

	@Override
	public void onPause() {
		InputMethodManager manager = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(foodNameInput.getApplicationWindowToken(), 0);

		super.onPause();	
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		saveStateToBundle(bundle);

		super.onSaveInstanceState(bundle);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_REQUEST) {  
			if (resultCode == Activity.RESULT_OK) {
				foodImage = (Bitmap) data.getExtras().get("data"); 
			}
		} else if (scanResult != null) {
			handleBarcode(scanResult.getContents());
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean validateData() {
		String foodName = foodNameInput.getText().toString();
		String carbValue = carbValueInput.getText().toString();

		return !(foodName.equals("") || carbValue.equals("") || Integer.valueOf(carbValue) < 0);
	}

	private void handleBarcode(String barcodeValue) {
		if (barcodeValue == null || barcodeValue.equals("")) {
			return;
		}

		Log.i(LOG_TAG, "Handling barcode with value " + barcodeValue);

		// First see if this barcode has already been assigned to a food
		Barcode barCode = BarcodeUtil.barCodeForBarcodeValue(barcodeValue);

		// If so, populate the views with that food
		if (barCode != null && barCode.foodName != null) {
			Log.i(LOG_TAG, "Barcode already known, matched to food named " + barCode.foodName);
			populateFieldsForFoodName(barCode.foodName);
			return;
		}

		// Otherwise this is a new barcode
		populateFood();
		Log.i(LOG_TAG, "New barcode, linking to food named " + food.name);
		food.setBarcodeValue(barcodeValue);
		food.getBarcode().needsUpload = true;
	}

	private void populateFood() {
		String foodName = foodNameInput.getText().toString();
		int carbValue = 0;
		String carbString = carbValueInput.getText().toString();
		if (!carbString.equals("")) {
			carbValue = Integer.valueOf(carbString);
		}
		boolean correction = correctionCheckbox.isChecked();

		food.name = foodName;
		food.carbs = carbValue;
		food.isCorrection = correction;
		food.getBarcode().foodName = foodName;
	}

	private void saveAndExit() {
		populateFood();
        if (!food.name.isEmpty() && food.carbs >= 0)
        {
		    NotificationCenter.getInstance().postNotificationWithArguments(SaveManager.SAVE_FOOD_NOTIFICATION, food);
        }

        getFragmentManager().popBackStack();
	}

	private void startCameraForFoodPhoto() {
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
		startActivityForResult(cameraIntent, CAMERA_REQUEST); 
	}

	private void getCarbsAndTagsSuggestionForFoodAndPopulateView() {
		String currentFood = foodNameInput.getText().toString();

		//final ProgressDialog spinner = ProgressDialog.show(getActivity(), "", "Loading...", true);
		TextView carbInputPointer = carbValueInput;

		new AsyncTask<String, Void, Float>() {

			@Override
			protected Float doInBackground(String... currentFood) {
				return FoodUtil.getAverageCarbsForFoodNamed(currentFood[0]);
			}

			@Override
			protected void onPostExecute(Float average) {
				if (average != null && average > 0) {
					carbValueInput.setText(String.valueOf(average));
				} else {
//					if (spinner.isShowing()) {
//						spinner.hide();
//					}
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

					builder.setTitle("No Data");
					builder.setMessage("Not enough data to suggest a carb value");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}

					});

					AlertDialog alert = builder.create();
					alert.show();
				}
			}

		}.execute(currentFood);

		//		new AsyncTask<String, Void, List<Tag>>() {
		//
		//			@Override
		//			protected List<Tag> doInBackground(String... currentFood) {
		//				return FoodUtil.getAllTagsForFoodNamed(currentFood[0]);
		//			}
		//
		//			@Override
		//			protected void onPostExecute(List<Tag> tags) {
		//				for (Tag tag : tags) {
		//					addViewForTag(tag);
		//				}
		//				if (spinner.isShowing()) {
		//					spinner.hide();
		//				}
		//				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		//
		//				builder.setTitle("No Data");
		//				builder.setMessage("Not enough data to suggest tags");
		//				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		//
		//					@Override
		//					public void onClick(DialogInterface dialog, int which) {
		//						dialog.dismiss();
		//					}
		//
		//				});
		//
		//				AlertDialog alert = builder.create();
		//				alert.show();
		//			}
		//
		//		}.execute(currentFood);
	}

	// Bundle Management Methods
	private void loadStateFromBundle(Bundle bundle) {
		if (bundle.containsKey(FOOD_KEY)) {
			food = (Food)bundle.getSerializable(FOOD_KEY);
		} else {
			food = new Food();
		}
	}

	private void saveStateToBundle(Bundle bundle) {
		// Food
		populateFood();
		if (food != null) {
			bundle.putSerializable(FOOD_KEY, food);
		}
	}

	// View Setup Methods
	private void setupFoodNameAdapter() {
		foodNameAdapter = new ArrayAdapter<String>(getActivity(), R.layout.autocomplete_layout, foodNamesForAutoComplete);
		foodNameAdapter.setNotifyOnChange(true);
		new AsyncTask<Void, Void, List<String>>() {

			@Override
			protected List<String> doInBackground(Void... params) {
				return FoodUtil.getAllFoodNames();
			}

			@Override
			protected void onPostExecute(List<String> names) {
				Log.i(LOG_TAG, "Updating autocomplete list with food names");
				Set<String> knownStrings = new HashSet<String>();
				knownStrings.addAll(foodNamesForAutoComplete);

				for (String s : names) {
					if (!knownStrings.contains(s)) {
						foodNamesForAutoComplete.add(s);
						knownStrings.add(s);
					}
				}
				foodNameAdapter.notifyDataSetChanged();	
			}

		}.execute();
	}

	private void addViewForTag(Tag tag) {
		tagList.add(tag);
		TextView newTagName = (TextView)getActivity().getLayoutInflater().inflate(R.layout.tag_list_item, null);
		newTagName.setText(tag.name);
		tagListLayout.addView(newTagName, tagListLayout.getChildCount() - 1);
	}

	private void populateFieldsForFoodName(String foodName) {
		if (foodName == null || foodName.equals("")) {
			Log.i(LOG_TAG, "Can't populate fields with foodName of " + foodName);
			return;
		}

		new AsyncTask<String, Void, Void>() {

			@Override
			protected Void doInBackground(String... params) {
				String name = params[0];

				foodNameInput.setText(name);

				String carbValue = String.valueOf(FoodUtil.getAverageCarbsForFoodNamed(name));
				carbValueInput.setText(carbValue);

				List<Tag> tags = FoodUtil.getAllTagsForFoodNamed(name);
				tagList.clear();
				tagListLayout.removeAllViews();
				for (Tag tag : tags) {
					addViewForTag(tag);
				}

				return null;
			}

		}.execute(foodName);
	}

	private void extractUIElementsFromView(View parentView) {
		foodNameInput = (AutoCompleteTextView)parentView.findViewById(R.id.add_food_view_food_name_input);

		carbValueInput = (EditText)parentView.findViewById(R.id.add_food_view_carbs_input);

		tagListLayout = (LinearLayout)parentView.findViewById(R.id.add_food_view_tags_list_layout);
//		addTagListItem = (TextView)parentView.findViewById(R.id.add_food_view_tags_list_add_tag_label);

		correctionCheckbox = (CheckBox)parentView.findViewById(R.id.add_food_view_correction_checkbox);

		scanButton = (Button)parentView.findViewById(R.id.add_food_view_scan_button);
		photoButton = (Button)parentView.findViewById(R.id.add_food_view_photo_button);
		suggestButton = (Button)parentView.findViewById(R.id.add_food_view_suggest_carbs_button);
		saveButton = (Button) parentView.findViewById(R.id.add_food_view_save_button);
	}

	private void configureUIElements() {
		// Food Name
		foodNameInput.setThreshold(0);
		foodNameInput.setAdapter(foodNameAdapter);
		foodNameInput.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		foodNameInput.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					carbValueInput.requestFocus();
					return true;
				}
				return false;
			}
		});
		if (food != null && food.name != null) {
			foodNameInput.setText(food.name);
		}
		foodNameInput.requestFocus();

		// Carb Value
		carbValueInput.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					correctionCheckbox.requestFocus();
					return true;
				} else if (actionId == EditorInfo.IME_ACTION_DONE) {
					saveAndExit();
					return true;
				}
				return false;
			}
		});
		if (food != null && food.carbs >= 0) {
			carbValueInput.setText(String.valueOf(food.carbs));
		}

		// Tag List
		//		addTagListItem.setClickable(true);
		//		addTagListItem.setOnClickListener(new OnClickListener() {
		//			@Override
		//			public void onClick(View arg0) {
		//				AddTagFragment fragment = new AddTagFragment();
		//
		//				GlucloserActivity.getPumpActivity().showFragment(fragment, "ADDTAG");
		//			}
		//
		//		});
		for (Tag tag : tagList) {
			addViewForTag(tag);
		}

		// Correction
		if (food != null) {
			correctionCheckbox.setChecked(food.isCorrection);
		}

		// Barcode scan
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegrator integrator = new IntentIntegrator(getActivity(), (Fragment)AddFoodFragment.this);
				integrator.initiateScan();
			}
		});

		// Save
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();

				if (id == R.id.add_food_view_save_button) {
					saveAndExit();
				} else if (id == R.id.add_food_view_photo_button) {
					startCameraForFoodPhoto();
				} else if (id == R.id.add_food_view_suggest_carbs_button) {
					getCarbsAndTagsSuggestionForFoodAndPopulateView();
				}
			}
		});
	}
}
