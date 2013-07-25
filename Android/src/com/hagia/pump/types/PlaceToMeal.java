package com.hagia.pump.types;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hagia.pump.util.MealUtil;
import com.hagia.pump.util.PlaceUtil;
import com.hagia.pump.util.database.DatabaseUtil;
import com.hagia.pump.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class PlaceToMeal implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String PLACE_DB_COLUMN_KEY = "place";
	public static final String MEAL_DB_COLUMN_KEY = "meal";
	
	public Place place;
	public Meal meal;
	public String id;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;
	
	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(PlaceToMeal.PLACE_DB_COLUMN_KEY, String.class);
		put(PlaceToMeal.MEAL_DB_COLUMN_KEY, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};
	
	public PlaceToMeal() {
		this.id = UUID.randomUUID().toString();
		this.dataVersion = 1;
	}
	
	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(PLACE_DB_COLUMN_KEY, place.toParseObject());
		pobj.put(MEAL_DB_COLUMN_KEY, meal.toParseObject());
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.PLACE_TO_MEAL_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.PLACE_TO_MEAL_DB_NAME));
		}
		
		return ret;
	}
	
	public static PlaceToMeal fromMap(Map<String, Object> map) {
		PlaceToMeal placeToMeal = new PlaceToMeal();
		
		placeToMeal.place = PlaceUtil.getPlaceById((String)map.get(PLACE_DB_COLUMN_KEY));
		placeToMeal.meal = MealUtil.getMealById((String)map.get(MEAL_DB_COLUMN_KEY));
		
		placeToMeal.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);

		placeToMeal.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		placeToMeal.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		
		try {
			placeToMeal.createdAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			placeToMeal.updatedAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return placeToMeal;
	}
}
