package com.hagia.glucloser.types;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hagia.glucloser.util.database.Tables;
import com.hagia.glucloser.util.FoodUtil;
import com.hagia.glucloser.util.MealUtil;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MealToFood implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String MEAL_DB_COLUMN_KEY = "meal";
	public static final String FOOD_DB_COLUMN_KEY = "food";
	
	public Meal meal;
	public Food food;
	public String id;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;
	
	public MealToFood() {
		this.id = UUID.randomUUID().toString();
		this.dataVersion = 1;
	}
	
	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(MealToFood.MEAL_DB_COLUMN_KEY, String.class);
		put(MealToFood.FOOD_DB_COLUMN_KEY, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};
	
	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(MEAL_DB_COLUMN_KEY, meal.toParseObject());
		pobj.put(FOOD_DB_COLUMN_KEY, food.toParseObject());
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);
		
		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.MEAL_TO_FOOD_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.MEAL_TO_FOOD_DB_NAME));
		}
		
		return ret;
	}
	
	public static MealToFood fromMap(Map<String, Object> map) {
		MealToFood mtf = new MealToFood();
		
		mtf.meal = MealUtil.getMealById((String)map.get(MEAL_DB_COLUMN_KEY));
		mtf.food = FoodUtil.getFoodById((String)map.get(FOOD_DB_COLUMN_KEY));
		mtf.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);

		mtf.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		mtf.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		try {
			mtf.createdAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mtf.updatedAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mtf;
	}
}
