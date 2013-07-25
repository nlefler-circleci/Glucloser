package com.hagia.pump.types;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hagia.pump.util.FoodUtil;
import com.hagia.pump.util.TagUtil;
import com.hagia.pump.util.database.DatabaseUtil;
import com.hagia.pump.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TagToFood {
	
	public static final String TAG_DB_COLUMN_KEY = "tag";
	public static final String FOOD_DB_COLUMN_KEY = "food";
	
	public Tag tag;
	public Food food;
	public String id;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;
	
	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(TagToFood.TAG_DB_COLUMN_KEY, String.class);
		put(TagToFood.FOOD_DB_COLUMN_KEY, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};
	
	public TagToFood() {
		this.id = UUID.randomUUID().toString();
		this.dataVersion = 1;
	}
	
	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(TAG_DB_COLUMN_KEY, tag.toParseObject());
		pobj.put(FOOD_DB_COLUMN_KEY, food.toParseObject());
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.TAG_TO_FOOD_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.TAG_TO_FOOD_DB_NAME));
		}
		
		return ret;
	}
	
	public static TagToFood fromMap(Map<String, Object> map) {
		TagToFood tagToFood = new TagToFood();
		
		tagToFood.tag = TagUtil.getTagById((String)map.get(TAG_DB_COLUMN_KEY));
		tagToFood.food = FoodUtil.getFoodById((String)map.get(FOOD_DB_COLUMN_KEY));
		
		tagToFood.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);
		tagToFood.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		tagToFood.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		
		try {
			tagToFood.createdAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			tagToFood.updatedAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tagToFood;
	}
}
