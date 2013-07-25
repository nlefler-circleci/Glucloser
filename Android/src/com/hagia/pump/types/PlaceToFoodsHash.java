package com.hagia.pump.types;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hagia.pump.util.PlaceUtil;
import com.hagia.pump.util.database.DatabaseUtil;
import com.hagia.pump.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class PlaceToFoodsHash {
	public static final String PLACE_DB_COLUMN_KEY = "place";
	public static final String FOODS_HASH_DB_COLUMN_KEY = "foodsHash";
	
	public Place place;
	public String foodsHash;
	public String id;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;
	
	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(PlaceToFoodsHash.PLACE_DB_COLUMN_KEY, String.class);
		put(PlaceToFoodsHash.FOODS_HASH_DB_COLUMN_KEY, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};
	
	public PlaceToFoodsHash() {
		this.id = UUID.randomUUID().toString();
		this.dataVersion = 1;
	}
	
	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(PLACE_DB_COLUMN_KEY, place.toParseObject());
		pobj.put(FOODS_HASH_DB_COLUMN_KEY, foodsHash);
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.PLACE_TO_FOODS_HASH_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.PLACE_TO_FOODS_HASH_DB_NAME));
		}
		
		return ret;
	}
	
	public static PlaceToFoodsHash fromMap(Map<String, Object> map) {
		PlaceToFoodsHash placeToFoodsHash = new PlaceToFoodsHash();
		
		placeToFoodsHash.place = PlaceUtil.getPlaceById((String)map.get(PLACE_DB_COLUMN_KEY));
		placeToFoodsHash.foodsHash = (String)map.get(FOODS_HASH_DB_COLUMN_KEY);
		
		placeToFoodsHash.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);

		placeToFoodsHash.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		placeToFoodsHash.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		
		try {
			placeToFoodsHash.createdAt = DatabaseUtil.parseDateFormat.parse(
					(String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			placeToFoodsHash.updatedAt = DatabaseUtil.parseDateFormat.parse(
					(String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return placeToFoodsHash;
	}
}
