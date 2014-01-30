package com.nlefler.glucloser.types;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import android.text.format.DateFormat;
import android.util.Log;

import com.nlefler.glucloser.util.MealUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.nlefler.glucloser.util.database.Tables;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class Meal implements Serializable {
	private static final String LOG_TAG = "Pump_Meal";

	public static final String DATE_EATEN_DB_COLUMN_NAME = "dateEaten";

	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(DATE_EATEN_DB_COLUMN_NAME, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};

	public String id;
	public PlaceToMeal placeToMeal;
	public List<MealToFood> mealToFoods;
	public Date dateEaten;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;

	public Meal() {
		this.id = UUID.randomUUID().toString();

		this.mealToFoods = new ArrayList<MealToFood>();

		this.dateEaten = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu"))).getTime();
	}

	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(DATE_EATEN_DB_COLUMN_NAME, this.dateEaten);
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}

	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.MEAL_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.MEAL_DB_NAME));
		}
		
		return ret;
	}
	
	public static Meal fromMap(Map<String, Object> map) {
		Meal meal = new Meal();

		meal.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);

		meal.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		meal.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		try {
			meal.dateEaten = DatabaseUtil.parseDateFormat.parse(
					(String)map.get(Meal.DATE_EATEN_DB_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			Log.e(LOG_TAG, "Unable to parse " + (String)map.get(Meal.DATE_EATEN_DB_COLUMN_NAME));
			e.printStackTrace();
		}


		try {
			meal.createdAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			meal.updatedAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return meal;
	}
	
	public Meal linkFoods() {
		this.mealToFoods.addAll(MealUtil.getFoodsForMeal(this));
		return this;
	}
	
	public Meal linkPlace() {
		this.placeToMeal = MealUtil.getPlaceForMeal(this);
		return this;
	}

	public static boolean verifyMeal(Meal meal) {
		return meal != null &&
				meal.placeToMeal != null &&
				meal.placeToMeal.meal != null &&
				meal.placeToMeal.place != null &&
				meal.placeToMeal.place != null &&
				meal.mealToFoods != null &&
				meal.id != null;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Meal) {
			Meal m = (Meal)o;

			return this.placeToMeal.equals(m.placeToMeal) &&
					// TODO: iterative equals?
					this.mealToFoods.equals(m.mealToFoods);
		}
		return false;
	}

	public void addFood(MealToFood f) {
		f.meal = this;
		this.mealToFoods.add(f);
	}

	public void removeFood(MealToFood f) {
		f.meal = null;
		this.mealToFoods.remove(f);
	}

	public Date getDateEaten() {
		return (Date) this.dateEaten.clone();
	}

	public String getDateEatenForDisplay() {
		TimeZone tz = TimeZone.getDefault();
		Calendar toTZ = Calendar.getInstance();
		toTZ.setTime(this.dateEaten);
		toTZ.setTimeZone(tz);

		return DateFormat.format("MMM dd, kk:mm", toTZ).toString();
	}
}
