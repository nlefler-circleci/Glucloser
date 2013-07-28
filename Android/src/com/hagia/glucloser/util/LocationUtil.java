package com.hagia.glucloser.util;

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

import com.hagia.glucloser.types.Place;
import com.parse.ParseGeoPoint;

public class LocationUtil {
	private static final String LOG_TAG = "Pump_Location_Util";

	private static Location currentLocation = null;
	private static LocationManager locationManager = null;
	private static LocationListener networkLocationListener = null, gpsLocationListener = null;
	private static Set<LocationListener> clients = null;

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
		currentLocation = lm.getLastKnownLocation(NETWORK_PROVIDER);

		gpsLocationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				if (isBetterLocation(location, currentLocation)) {
					Log.i(LOG_TAG, "Location updated with GPS (" +
							location.getLatitude() + ", " + location.getLongitude() + ")");
					currentLocation = location;
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
				if (isBetterLocation(location, currentLocation)) {
					Log.i(LOG_TAG, "Location updated with network (" + 
							location.getLatitude() + ", " + location.getLongitude() + ")");
					currentLocation = location;
				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {}

			public void onProviderDisabled(String provider) {}
		};

		addLocationListener(NETWORK_PROVIDER, networkLocationListener);
		addLocationListener(GPS_PROVIDER, gpsLocationListener);

		for (LocationListener client : clients) {
			addLocationListener(client);
		}
	}

	public synchronized static void shutdown() {
		if (clients == null) {
			return;
		}

		if (locationManager != null) {
			Log.i(LOG_TAG, "Removing updates for " + clients.size() + " clients");
			for (LocationListener client : clients) {
				locationManager.removeUpdates(client);
			}
		}
	}

	public static boolean haveValidLocation() {
		return currentLocation != null;
	}

	public static Location getCurrentLocation() {
		return currentLocation;
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

	/**
	 * Get the closest known @ref Place.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @return The closest @ref Place, or null if there isn't one
	 */
	public static Place getClosestPlace() {
		return getClosestPlace(getCurrentLocation());
	}

	private static Place closestPlace = null;
	private static Place secondClosest = null;
	private static Location lastLocationForClosestPlace = null;
	private static double distanceToRecomputeClosestPlace = Double.MIN_VALUE;
	/**
	 * Get the closest known @ref Place to the provided location.
	 * 
	 * @note This method is synchronous. It should not be called on
	 * the main thread.
	 * 
	 * @param location The location to base the search from
	 * 
	 * @return The closest @ref Place, or null if there isn't one
	 */
	public static Place getClosestPlace(Location location) {
		if (closestPlace != null && location != null  && lastLocationForClosestPlace != null &&
				location.distanceTo(lastLocationForClosestPlace) < distanceToRecomputeClosestPlace) {
			Log.v(LOG_TAG, "Short circuit closest place. Distance from last location (" +
					currentLocation.distanceTo(lastLocationForClosestPlace) + ") is less than " +
					"the distance required to recompute (" + distanceToRecomputeClosestPlace + "). " +
					"Returning " + closestPlace.name);
			return closestPlace;
		}

		List<Place> nearby = PlaceUtil.getPlacesNear(location);
		if (nearby == null || nearby.isEmpty()) {
			return null;
		}

		closestPlace = nearby.get(0);
		if (nearby.size() > 1) {
			secondClosest = nearby.get(1);
		}
		lastLocationForClosestPlace = getCurrentLocation();
		if (lastLocationForClosestPlace != null &&
				secondClosest != null) {
			double distanceToClosest = lastLocationForClosestPlace.distanceTo(closestPlace.location);
			double distanceToSecondClosest = lastLocationForClosestPlace.distanceTo(secondClosest.location);

			distanceToRecomputeClosestPlace = distanceToClosest + ((distanceToSecondClosest - distanceToClosest) / 2);
		}

		return nearby.get(0);
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
		// Register the listener with the Location Manager to receive location updates

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(true);

		locationManager.requestLocationUpdates(minTime, minDistance, criteria, listener, null);
		clients.add(listener);
	}

	private synchronized static void addLocationListener(String provider, LocationListener listener) {
		// Register the listener with the Location Manager to receive location updates
		if (locationManager.isProviderEnabled(provider)) {
			locationManager.requestLocationUpdates(provider, minTime, minDistance, listener);
			clients.add(listener);
		} else {
			addLocationListener(listener);
		}
	}

	public synchronized static void removeLocationListener(LocationListener listener) {
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
