package com.nlefler.glucloser.types;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import android.location.Location;

import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.nlefler.glucloser.util.LocationUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;

public class Place extends Model implements Serializable {
	private static final long serialVersionUID = -7803318132865386549L;

	private static final String LOG_TAG = "Glucloser_Place";

    public static final String GLUCLOSER_ID_COLUMN_KEY = "glucloser_id";
    public static final String FOURSQUARE_ID_COLUMN_KEY = "foursquare_id";
	public static final String NAME_DB_COLUMN_KEY = "name";
	public static final String LOCATION_DB_COLUMN_KEY = "location";
	public static final String LATITUDE_DB_COLUMN_KEY = "location_latitude";
	public static final String LONGITUDE_DB_COLUMN_KEY = "location_longitude";
	public static final String READABLE_ADDRESS_COLUMN_KEY = "readable_address";
    public static final String LAST_VISITED_COLUMN_KEY = "last_visited";

    @Column(LATITUDE_DB_COLUMN_KEY)
	private double latitudeForSerializing;
    @Column(LONGITUDE_DB_COLUMN_KEY)
	private double longitudeForSerializing;

    @Key
    @AutoIncrement
    @Column(DatabaseUtil.ID_COLUMN_NAME)
    private int id;

    @Key
    @Column(DatabaseUtil.PARSE_ID_COLUMN_NAME)
	public String parseId;

    @Key
    @Column(GLUCLOSER_ID_COLUMN_KEY)
    public String glucloserId;

    @Key
    @Column(FOURSQUARE_ID_COLUMN_KEY)
    public String foursquareId;

    @Key
    @Column(NAME_DB_COLUMN_KEY)
	public String name;

	public transient Location location;

    @Column(LAST_VISITED_COLUMN_KEY)
	public Date lastVisited;

    @Column(READABLE_ADDRESS_COLUMN_KEY)
	public String readableAddress;

    @Column(DatabaseUtil.CREATED_AT_COLUMN_NAME)
	public Date createdAt;
    @Column(DatabaseUtil.UPDATED_AT_COLUMN_NAME)
	public Date updatedAt;
    @Column(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME)
	public boolean needsUpload;
    @Column(DatabaseUtil.DATA_VERSION_COLUMN_NAME)
	public int dataVersion;

	public Place() {
		this.parseId = UUID.randomUUID().toString();
        this.glucloserId = UUID.randomUUID().toString();
	}

    public Location getLocation() {
        Location l = new Location(LocationUtil.NO_PROVIDER);
        l.setLatitude(latitudeForSerializing);
        l.setLongitude(longitudeForSerializing);
        return l;
    }

	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(NAME_DB_COLUMN_KEY, this.name);
		pobj.put(LOCATION_DB_COLUMN_KEY,
				LocationUtil.getParseGeoPointForLocation(this.getLocation()));
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		if (readableAddress != null) {
			pobj.put(READABLE_ADDRESS_COLUMN_KEY, readableAddress);
		}

        if (glucloserId != null) {
            pobj.put(GLUCLOSER_ID_COLUMN_KEY, glucloserId);
        }
        if (foursquareId != null) {
            pobj.put(FOURSQUARE_ID_COLUMN_KEY, foursquareId);
        }

		return pobj;
	}

	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.PLACE_DB_NAME);
			ret = populateParseObject(query.get(parseId));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.PLACE_DB_NAME));
		}

		return ret;
	}

}
