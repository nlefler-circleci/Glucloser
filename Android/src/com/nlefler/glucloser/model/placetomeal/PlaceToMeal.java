package com.nlefler.glucloser.model.placetomeal;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.nlefler.glucloser.model.GlucloserBaseModel;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.meal.MealUtil;
import com.nlefler.glucloser.model.place.PlaceUtil;
import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.nlefler.glucloser.util.database.DatabaseUtil;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@Table(PlaceToMeal.PLACE_TO_MEAL_DB_NAME)
public class PlaceToMeal extends GlucloserBaseModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static final String PLACE_TO_MEAL_DB_NAME = "PlaceToMeal";
	
	public static final String PLACE_DB_COLUMN_KEY = "placeGlucloserId";
	public static final String MEAL_DB_COLUMN_KEY = "mealGlucloserId";

	@Key
	@Column(PLACE_DB_COLUMN_KEY)
	public String placeGlucloserId;

	@Key
	@Column(MEAL_DB_COLUMN_KEY)
	public String mealGlucloserId;
	
	public PlaceToMeal() {
		this.glucloserId = UUID.randomUUID().toString();
		this.dataVersion = 1;
	}
	
	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(PLACE_DB_COLUMN_KEY, placeGlucloserId);
		pobj.put(MEAL_DB_COLUMN_KEY, mealGlucloserId);
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(PLACE_TO_MEAL_DB_NAME);
			ret = populateParseObject(query.get(parseId));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(PLACE_TO_MEAL_DB_NAME));
		}
		
		return ret;
	}
}
