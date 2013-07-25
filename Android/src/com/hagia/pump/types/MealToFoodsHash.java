package com.hagia.pump.types;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hagia.pump.util.MealUtil;
import com.hagia.pump.util.database.DatabaseUtil;
import com.hagia.pump.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MealToFoodsHash {
	public static final String MEAL_DB_COLUMN_KEY = "meal";
	public static final String FOODS_HASH_DB_COLUMN_KEY = "foodsHash";
	
	public Meal meal;
	public String foodsHash;
	public String id;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;
	
	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(MealToFoodsHash.MEAL_DB_COLUMN_KEY, String.class);
		put(MealToFoodsHash.FOODS_HASH_DB_COLUMN_KEY, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};
	
	public MealToFoodsHash() {
		this.id = UUID.randomUUID().toString();
		this.dataVersion = 1;
	}
	
	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(MEAL_DB_COLUMN_KEY, meal.toParseObject());
		pobj.put(FOODS_HASH_DB_COLUMN_KEY, foodsHash);
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.MEAL_TO_FOODS_HASH_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.MEAL_TO_FOODS_HASH_DB_NAME));
		}
		
		return ret;
	}
	
	public static MealToFoodsHash fromMap(Map<String, Object> map) {
		MealToFoodsHash mealToFoodsHash = new MealToFoodsHash();
		
		mealToFoodsHash.meal = MealUtil.getMealById((String)map.get(MEAL_DB_COLUMN_KEY));
		mealToFoodsHash.foodsHash = (String)map.get(FOODS_HASH_DB_COLUMN_KEY);
		
		mealToFoodsHash.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);

		mealToFoodsHash.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		mealToFoodsHash.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		
		try {
			mealToFoodsHash.createdAt = DatabaseUtil.parseDateFormat.parse(
					(String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mealToFoodsHash.updatedAt = DatabaseUtil.parseDateFormat.parse(
					(String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mealToFoodsHash;
	}
}
