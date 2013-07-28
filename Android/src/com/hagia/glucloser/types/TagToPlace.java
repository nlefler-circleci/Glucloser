package com.hagia.glucloser.types;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hagia.glucloser.util.PlaceUtil;
import com.hagia.glucloser.util.TagUtil;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.hagia.glucloser.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TagToPlace {
	public static final String TAG_DB_COLUMN_KEY = "tag";
	public static final String PLACE_DB_COLUMN_KEY = "place";
	
	public Tag tag;
	public Place place;
	public String id;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;
	
	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(TagToPlace.TAG_DB_COLUMN_KEY, String.class);
		put(TagToPlace.PLACE_DB_COLUMN_KEY, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};
	
	public TagToPlace() {
		this.id = UUID.randomUUID().toString();
		this.dataVersion = 1;
	}
	
	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(TAG_DB_COLUMN_KEY, tag.toParseObject());
		pobj.put(PLACE_DB_COLUMN_KEY, place.toParseObject());
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.TAG_TO_PLACE_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.TAG_TO_PLACE_DB_NAME));
		}
		
		return ret;
	}
	
	public static TagToPlace fromMap(Map<String, Object> map) {
		TagToPlace tagToPlace = new TagToPlace();
		
		tagToPlace.tag = TagUtil.getTagById((String)map.get(TAG_DB_COLUMN_KEY));
		tagToPlace.place = PlaceUtil.getPlaceById((String)map.get(PLACE_DB_COLUMN_KEY));
		
		tagToPlace.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);
		tagToPlace.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		tagToPlace.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		
		try {
			tagToPlace.createdAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			tagToPlace.updatedAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tagToPlace;
	}
}
