package com.hagia.glucloser.types;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import android.location.Location;
import android.util.Log;

import com.hagia.glucloser.util.database.Tables;
import com.hagia.glucloser.util.LocationUtil;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class Place implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7803318132865386549L;

	private static final String LOG_TAG = "Pump_Place";

	public static final String NAME_DB_COLUMN_KEY = "name";
	public static final String LOCATION_DB_COLUMN_KEY = "location";
	public static final String LATITUDE_DB_COLUMN_KEY = "location_latitude";
	public static final String LONGITUDE_DB_COLUMN_KEY = "location_longitude";
	public static final String READABLE_ADDRESS_COLUMN_KEY = "readable_address";

	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {
		{
			put(NAME_DB_COLUMN_KEY, String.class);
			put(LOCATION_DB_COLUMN_KEY, ParseGeoPoint.class);
			put(LATITUDE_DB_COLUMN_KEY, Double.class);
			put(LONGITUDE_DB_COLUMN_KEY, Double.class);
			put(READABLE_ADDRESS_COLUMN_KEY, String.class);
			put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
			put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
			put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
			put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
			put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
		}
	};

	private double latitudeForSerializing;
	private double longitudeForSerializing;

	public String id;
	public String name;
	public transient Location location;
	public List<TagToPlace> tagToPlaces;
	public Calendar lastVisited;
	public String readableAddress;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;

	public Place() {
		this.id = UUID.randomUUID().toString();

		this.tagToPlaces = new ArrayList<TagToPlace>();
		this.lastVisited = Calendar.getInstance();

		if (LocationUtil.haveValidLocation()) {
			this.location = LocationUtil.getCurrentLocation();
			this.latitudeForSerializing = this.location.getLatitude();
			this.longitudeForSerializing = this.location.getLongitude();
		}
	}

	private void readObject(ObjectInputStream in) {
		try {
			in.defaultReadObject();
			this.location = new Location(LocationUtil.NO_PROVIDER);
			this.location.setLatitude(this.latitudeForSerializing);
			this.location.setLongitude(this.longitudeForSerializing);
		} catch (NotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(NAME_DB_COLUMN_KEY, this.name);
		pobj.put(LOCATION_DB_COLUMN_KEY,
				LocationUtil.getParseGeoPointForLocation(this.location));
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		if (readableAddress != null) {
			pobj.put(READABLE_ADDRESS_COLUMN_KEY, readableAddress);
		}

		return pobj;
	}

	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.PLACE_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.PLACE_DB_NAME));
		}

		return ret;
	}

	public static Place fromMap(Map<String, Object> map) {
		Place place = new Place();

		place.id = (String) map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);
		place.needsUpload = (Boolean) map
				.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		place.dataVersion = (Integer) map
				.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		place.name = (String) map.get(Place.NAME_DB_COLUMN_KEY);
		place.location = new Location(LocationUtil.NO_PROVIDER);
		place.location.setLatitude((Double) map
				.get(Place.LATITUDE_DB_COLUMN_KEY));
		place.location.setLongitude((Double) map
				.get(Place.LONGITUDE_DB_COLUMN_KEY));
		place.latitudeForSerializing = place.location.getLatitude();
		place.longitudeForSerializing = place.location.getLongitude();
		place.readableAddress = (String) map
				.get(Place.READABLE_ADDRESS_COLUMN_KEY);
		place.lastVisited = Calendar.getInstance();
		try {
			place.lastVisited.setTime(DatabaseUtil.parseDateFormat
					.parse((String) map
							.get(DatabaseUtil.UPDATED_AT_COLUMN_NAME)));
		} catch (java.text.ParseException e) {
			Log.e(LOG_TAG,
					"Unable to parse last update time "
							+ (String) map
									.get(DatabaseUtil.UPDATED_AT_COLUMN_NAME));
			e.printStackTrace();
		}

		try {
			place.createdAt = DatabaseUtil.parseDateFormat.parse((String) map
					.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			place.updatedAt = DatabaseUtil.parseDateFormat.parse((String) map
					.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return place;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime
				* result
				+ ((location == null) ? 0
						: (int) (location.getLongitude() * location
								.getLatitude()));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Place other = (Place) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!(location.getLatitude() == other.location.getLatitude() && location
				.getLongitude() == other.location.getLongitude()))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public void addTag(TagToPlace tag) {
		tag.place = this;
		this.tagToPlaces.add(tag);
	}

	public void removeTag(TagToPlace tag) {
		tag.place = this;
		this.tagToPlaces.remove(tag);
	}

	public Calendar getLastVisited() {
		return (Calendar) this.lastVisited.clone();
	}

	public Calendar getLastVisitedForDisplay() {
		TimeZone tz = TimeZone.getDefault();
		Calendar toTZ = (Calendar) this.lastVisited.clone();
		toTZ.setTimeZone(tz);

		return toTZ;
	}
}
