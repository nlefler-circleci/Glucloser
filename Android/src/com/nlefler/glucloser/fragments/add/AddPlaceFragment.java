package com.nlefler.glucloser.fragments.add;

import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nlefler.glucloser.types.Place;
import com.nlefler.glucloser.util.LocationUtil;
import com.nlefler.glucloser.R;
import com.nlefler.glucloser.util.database.save.SaveManager;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddPlaceFragment extends Fragment {
	public static final String PLACE_KEY = "place";

	private static final int GET_PLAY_SERVICES_REQUEST_CODE = 3251;

	private boolean isSetup = false;

	private LocationListener locationListener;
	private MapFragment mapFragment = null;
	private Place place = null;

	private AutoCompleteTextView placeNameInput;
	private TextView addressInput;
	private TextView latitudeInput;
	private TextView longitudeInput;

	private boolean lockLatitude = false;
	private boolean lockLongitude = false;
	private boolean lockAddress = false;
	private boolean lockMap = false;
	private boolean showingMap = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.add_place, container, false);

		// Setup
		setupMemberVars(view);

		Bundle state = savedInstanceState != null && !savedInstanceState.isEmpty() ?
				savedInstanceState : 
					(getArguments() != null ? getArguments() : new Bundle());
		populateFromState(state);

		setupLocationListener();
		setupViews(view);

		return view;
	}

	@Override
	public void onResume() {
		InputMethodManager manager = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.showSoftInput(placeNameInput, InputMethodManager.SHOW_FORCED);

		setupLocationListener();

		super.onResume();
	}

	@Override
	public void onPause() {
		InputMethodManager manager = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(placeNameInput.getApplicationWindowToken(), 0);

		LocationUtil.removeLocationListener(locationListener);

		super.onPause();
	}

	private void setupMemberVars(View parentView) {
		if (isSetup) {
			return;
		}
		placeNameInput = (AutoCompleteTextView) parentView.findViewById(R.id.add_place_view_name_input);
		addressInput = (TextView) parentView.findViewById(R.id.add_place_view_address_input);
		latitudeInput = (TextView) parentView.findViewById(R.id.add_place_view_latitude_input);
		longitudeInput = (TextView) parentView.findViewById(R.id.add_place_view_longitude_input);
	}

	private void setupViews(View parentView) {
		if (isSetup) {
			return;
		}
		placeNameInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					savePlace();
					return true;
				}
				return false;
			}
		});

		(new AsyncTask<Void, Void, Object[]>() {

			@Override
			protected Object[] doInBackground(Void... arg0) {
				return getLocationInfo();
			}

			@Override
			protected void onPostExecute(Object[] results) {
				if (!lockLatitude) {
					latitudeInput.setText(String.valueOf(place.location.getLatitude()));
				}
				if (!lockLongitude) {
					longitudeInput.setText(String.valueOf(place.location.getLongitude()));
				}
				if (!lockAddress) {
					addressInput.setText(place.readableAddress);
				}
				if (showingMap && !lockMap) {
					setupMap();
				}
			}

		}).execute();

		initMap();
		setupMap();

		Button saveButton = (Button)parentView.findViewById(R.id.add_place_view_save_button);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				savePlace();
			}
		});
		isSetup = true;
	}

	private void populateFromState(Bundle state) {
		if (state.containsKey(PLACE_KEY)) {
			place = (Place)state.getSerializable(PLACE_KEY);

			placeNameInput.setText(place.name);

			latitudeInput.setText(String.valueOf(place.location.getLatitude()));
			//lockLatitude = true;

			longitudeInput.setText(String.valueOf(place.location.getLongitude()));
			//lockLongitude = true;

			addressInput.setText(place.readableAddress);
			lockAddress = true;

			//lockMap = lockLatitude && lockLongitude;
		} else {
			place = new Place();
		}
	}

	private boolean validateData() {
		String name = placeNameInput.getText().toString();
		return !(name.equals("") || name.equals(R.string.add_place_string));
	}

	private void savePlace() {
		if (validateData()) {
			String placeName = placeNameInput.getText().toString();
			String address = addressInput.getText().toString();
			double latitude = Double.valueOf(latitudeInput.getText().toString());
			double longitude = Double.valueOf(longitudeInput.getText().toString());


			if (placeName != null && !placeName.isEmpty()) {
				place.name = placeName;
			}
			if (!address.isEmpty()) {
				place.readableAddress = address;
			}
			if (latitude != 0 && longitude != 0) {
				place.location.setLatitude(latitude);
				place.location.setLongitude(longitude);
			}
		}

		NotificationCenter.getInstance().postNotificationWithArguments(SaveManager.SAVE_PLACE_NOTIFICATION, place);

		getActivity().getFragmentManager().popBackStack();
	}

	private Object[] getLocationInfo() {
		Location loc = LocationUtil.getLastKnownLocation();
		double latitude = 0;
		double longitude = 0;
		String address = null;

		if (loc != null) {
			latitude = loc.getLatitude();
			longitude = loc.getLongitude();

			List<Address> addresses = LocationUtil.getAddressFromLocation(loc, 1);
			if (!addresses.isEmpty()) {
				address = addresses.get(0).getThoroughfare();
			}
		}

		return new Object[] {latitude, longitude, address};
	}

	private void setupLocationListener() {
		LocationUtil.initialize(
				(LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE),
				getActivity().getApplicationContext());

		final Handler latLonHandler = new Handler();
		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					latLonHandler.post(new Runnable() {

						@Override
						public void run() {
							Object[] info = getLocationInfo();
							if (!lockLatitude) {
								latitudeInput.setText(String.valueOf(info[0]));
							}
							if (!lockLongitude) {
								longitudeInput.setText(String.valueOf(info[1]));
							}
							if (!lockAddress && info[2] != null) {
								addressInput.setText((String)info[2]);
							}
							if (!lockMap) {
								setupMap();
							}
						}

					});
				}
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}

		};

		LocationUtil.addLocationListener(locationListener);
	}

	private void initMap() {
		int playServicesResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
		if (playServicesResult != ConnectionResult.SUCCESS) {
			GooglePlayServicesUtil.getErrorDialog(playServicesResult, getActivity(), GET_PLAY_SERVICES_REQUEST_CODE);
		} else {
			try {
				MapsInitializer.initialize(getActivity());

				GoogleMapOptions options = new GoogleMapOptions();
				options.compassEnabled(true);
				options.mapType(GoogleMap.MAP_TYPE_HYBRID);

				mapFragment = MapFragment.newInstance(options);

				FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
				transaction.add(R.id.add_place_view_map_layout, mapFragment);
				transaction.commit();
				showingMap = true;

				setupMap();
			} catch (GooglePlayServicesNotAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setupMap() {
		if (mapFragment == null) {
			return;
		}

		GoogleMap map = mapFragment.getMap();
		if (map == null) {
			return;
		}

		LatLng latLng = new LatLng(place.location.getLatitude(), place.location.getLongitude());
		CameraPosition cameraPosition = new CameraPosition(latLng, 16, 0, 0);
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

		MarkerOptions options = new MarkerOptions()
		.position(latLng);
		if (place.name != null) {
			options.title(place.name);
		}

		map.clear();
		map.addMarker(options);
		lockMap = true;

		final GoogleMap mapP = map;
		map.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {
				mapP.clear();
				mapP.addMarker(new MarkerOptions().position(point));

				place.location.setLatitude(point.latitude);
				place.location.setLongitude(point.longitude);

				latitudeInput.setText(String.valueOf(point.latitude));
				longitudeInput.setText(String.valueOf(point.longitude));

				(new AsyncTask<LatLng, Void, String>() {

					@Override
					protected String doInBackground(LatLng... params) {
						if (params.length == 0) {
							return "";
						}

						Location point = new Location(LocationUtil.NO_PROVIDER);
						point.setLatitude(params[0].latitude);
						point.setLongitude(params[0].longitude);

						List<Address> addresses = LocationUtil.getAddressFromLocation(point, 1);
						if (!addresses.isEmpty()) {
							return addresses.get(0).getThoroughfare();
						}

						return "";
					}

					@Override
					protected void onPostExecute(String foundAddress) {
						place.readableAddress = foundAddress;
						addressInput.setText(foundAddress);
					}

				}).execute(point);

				lockLatitude = true;
				lockLongitude = true;
				lockAddress = true;
				lockMap = true;
			}
		});
	}
}
