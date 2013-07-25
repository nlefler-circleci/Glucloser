package com.hagia.pump.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hagia.pump.util.database.DatabaseUtil;
import com.hagia.pump.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class Tag implements Serializable {
	private static final long serialVersionUID = -1918165900828956293L;

	private static final String LOG_TAG = "Pump_Tag";

	public static final String NAME_DB_COLUMN_KEY = "name";

	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(NAME_DB_COLUMN_KEY, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};

	public String id;
	public String name;
	public List<TagToFood> foods;
	public List<TagToPlace> places;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;

	private boolean ready = false;
	
	public Tag() {
		this.id = UUID.randomUUID().toString();
		
		this.foods = new ArrayList<TagToFood>();
		this.places = new ArrayList<TagToPlace>();
	}

	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(NAME_DB_COLUMN_KEY, this.name);
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.TAG_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.TAG_DB_NAME));
		}
		
		return ret;
	}

	public static Tag fromMap(Map<String, Object> map) {
		Tag tag = new Tag();

		tag.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);
		tag.name = (String)map.get(NAME_DB_COLUMN_KEY);

		tag.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		tag.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		
		try {
			tag.createdAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			tag.updatedAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.UPDATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tag;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Tag) {
			Tag t = (Tag)o;

			return this.name.equals(t.name);
		}
		return false;
	}

	public void addFood(TagToFood food) {
		food.tag = this;
		this.foods.add(food);
	}

	public void removeFood(TagToFood food) {
		food.tag = null;
		this.foods.remove(food);
	}

	public void addPlace(TagToPlace place) {
		place.tag = this;
		this.places.add(place);
	}

	public void removePlace(TagToPlace place) {
		place.tag = null;
		this.places.remove(place);
	}

}
