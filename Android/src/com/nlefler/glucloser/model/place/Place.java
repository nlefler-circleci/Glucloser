package com.nlefler.glucloser.model.place;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import android.location.Location;

import com.nlefler.glucloser.model.GlucloserBaseModel;
import com.nlefler.glucloser.util.LocationUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table(Place.PLACE_DB_NAME)
public class Place extends GlucloserBaseModel implements Serializable {
	private static final long serialVersionUID = -7803318132865386549L;

	private static final String LOG_TAG = "Glucloser_Place";

    protected static final String PLACE_DB_NAME = "place";

    protected static final String FOURSQUARE_ID_COLUMN_KEY = "foursquare_id";
	protected static final String NAME_DB_COLUMN_KEY = "name";
	protected static final String LOCATION_DB_COLUMN_KEY = "location";
	protected static final String LATITUDE_DB_COLUMN_KEY = "location_latitude";
	protected static final String LONGITUDE_DB_COLUMN_KEY = "location_longitude";
	protected static final String READABLE_ADDRESS_COLUMN_KEY = "readable_address";
    protected static final String LAST_VISITED_COLUMN_KEY = "last_visited";

    @Column(LATITUDE_DB_COLUMN_KEY)
	private double latitudeForSerializing;
    @Column(LONGITUDE_DB_COLUMN_KEY)
	private double longitudeForSerializing;

    @Key
    @Column(FOURSQUARE_ID_COLUMN_KEY)
    public String foursquareId;

    @Key
    @Column(NAME_DB_COLUMN_KEY)
	public String name;

	private transient Location location;

    @Column(LAST_VISITED_COLUMN_KEY)
	public Date lastVisited;

    @Column(READABLE_ADDRESS_COLUMN_KEY)
	public String readableAddress;

	public Place() {
        super();

        lastVisited = new Date();
        foursquareId = "";
        name = "";
        readableAddress = "";
	}

    public Location getLocation() {
        if (location == null) {
            location = new Location(LocationUtil.NO_PROVIDER);
            location.setLatitude(latitudeForSerializing);
            location.setLongitude(longitudeForSerializing);
        }
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Place)) return false;

        Place place = (Place) o;

        if (dataVersion != place.dataVersion) return false;
        if (foursquareId != null ? !foursquareId.equals(place.foursquareId) : place.foursquareId != null)
            return false;
        if (!glucloserId.equals(place.glucloserId)) return false;
        if (lastVisited != null ? !lastVisited.equals(place.lastVisited) : place.lastVisited != null)
            return false;
        if (location != null ? !location.equals(place.location) : place.location != null)
            return false;
        if (!name.equals(place.name)) return false;
        if (readableAddress != null ? !readableAddress.equals(place.readableAddress) : place.readableAddress != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = glucloserId.hashCode();
        result = 31 * result + (foursquareId != null ? foursquareId.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (lastVisited != null ? lastVisited.hashCode() : 0);
        result = 31 * result + (readableAddress != null ? readableAddress.hashCode() : 0);
        result = 31 * result + dataVersion;
        return result;
    }
}
