package com.nlefler.glucloser.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseGeoPoint;

public class LocationUtil {
	private static final String LOG_TAG = "Glucloser_Location_Util";

    private static boolean isInitialized = false;

	private static Location lastKnownLocation = null;
	private static LocationManager locationManager = null;
	private static LocationListener networkLocationListener = null, gpsLocationListener = null;
	private static Set<LocationListener> clients = null;
    private static Set<LocationListener> pendingClients = new HashSet<LocationListener>();

	private static Geocoder geoCoder = null;

	public static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;
	public static final String GPS_PROVIDER = LocationManager.GPS_PROVIDER;
	public static final String NO_PROVIDER = "NOPROVIDER";
	
	private static final long minTime = 5000;
	private static final float minDistance = 5;

	public synchronized static void initialize(LocationManager lm, Context context) {
		if (locationManager != null) {
			return;
		}
		locationManager = lm;
		clients = new HashSet<LocationListener>();
		geoCoder = new Geocoder(context);
		lastKnownLocation = lm.getLastKnownLocation(NETWORK_PROVIDER);

		gpsLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				if (isBetterLocation(location, lastKnownLocation)) {
					Log.i(LOG_TAG, "Location updated with GPS (" +
							location.getLatitude() + ", " + location.getLongitude() + ")");
					lastKnownLocation = location;
				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
				if (status == LocationProvider.TEMPORARILY_UNAVAILABLE ||
						status == LocationProvider.OUT_OF_SERVICE) {
					addLocationListener(NETWORK_PROVIDER, networkLocationListener);
				} else if (status == LocationProvider.AVAILABLE) {
					removeLocationListener(networkLocationListener);
				}
			}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		networkLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				if (isBetterLocation(location, lastKnownLocation)) {
					Log.i(LOG_TAG, "Location updated with network (" + 
							location.getLatitude() + ", " + location.getLongitude() + ")");
					lastKnownLocation = location;
				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

        isInitialized = true;

		addLocationListener(NETWORK_PROVIDER, networkLocationListener);
		addLocationListener(GPS_PROVIDER, gpsLocationListener);

		for (LocationListener client : pendingClients) {
			addLocationListener(client);
		}
	}

	public synchronized static void shutdown() {
        isInitialized = false;
        pendingClients.clear();

		if (clients == null) {
			return;
		}

		if (locationManager != null) {
			for (LocationListener client : clients) {
				locationManager.removeUpdates(client);
			}
            clients.clear();
		}
	}

	public static boolean haveValidLocation() {
		return lastKnownLocation != null;
	}

	public static Location getLastKnownLocation() {
		return lastKnownLocation;
	}

	public static List<Address> getAddressFromLocation(Location loc, int maxResults) {
		return getAddressFromLocation(loc.getLatitude(), loc.getLongitude(), maxResults);
	}

	public static List<Address> getAddressFromLocation(double latitude,
			double longitude, int maxResults) {
		try {
			return geoCoder.getFromLocation(latitude, longitude, maxResults);
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
			return new ArrayList<Address>();
		}
	}

	public static ParseGeoPoint getParseGeoPointForLocation(Location loc) {
		if (loc == null) {
			Log.i(LOG_TAG, "Tried to create ParseGeoPoint from null Location");
			return null;
		}
		ParseGeoPoint point = new ParseGeoPoint();
		point.setLatitude(loc.getLatitude());
		point.setLongitude(loc.getLongitude());

		return point;
	}

	public static Location getLocationForParseGeoPoint(ParseGeoPoint point) {
		Location l = new Location(NO_PROVIDER);
		l.setLatitude(point.getLatitude());
		l.setLongitude(point.getLongitude());

		return l;
	}

	public synchronized static void addLocationListener(LocationListener listener) {
        if (!isInitialized) {
            pendingClients.add(listener);
            return;
        }

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(true);

		locationManager.requestLocationUpdates(minTime, minDistance, criteria, listener, null);
		clients.add(listener);
	}

	private synchronized static void addLocationListener(String provider, LocationListener listener) {
		if (locationManager.isProviderEnabled(provider)) {
			locationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
			clients.add(listener);
		} else {
			addLocationListener(listener);
		}
	}

	public synchronized static void removeLocationListener(LocationListener listener) {
        if (!isInitialized) {
            pendingClients.remove(listener);
            return;
        }

		if (locationManager != null && listener != null) {
			locationManager.removeUpdates(listener);
			clients.remove(listener);
		}
	}

	// TWO_MINUTES, isBetterLocation, isSameProvider are from
	// http://developer.android.com/guide/topics/location/strategies.html
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	private static boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
