package com.nlefler.glucloser.model;

import java.io.Serializable;
import java.util.UUID;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table(MealToFood.MEAL_TO_FOOD_DB_NAME)
public class MealToFood extends GlucloserBaseModel implements Serializable {
	private static final long serialVersionUID = 1L;

    protected static final String MEAL_TO_FOOD_DB_NAME = "meal_to_food";
	public static final String MEAL_DB_COLUMN_KEY = "mealGlucloserId";
	public static final String FOOD_DB_COLUMN_KEY = "foodGlucloserId";

    @Key
    @Column(MEAL_DB_COLUMN_KEY)
	public String mealGlucloserId;

    @Key
    @Column(FOOD_DB_COLUMN_KEY)
	public String foodGlucloserId;
	
	public MealToFood() {
		this.parseId = UUID.randomUUID().toString();
		this.glucloserId = UUID.randomUUID().toString();
		this.dataVersion = 1;
	}

	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(MEAL_DB_COLUMN_KEY, mealGlucloserId);
		pobj.put(FOOD_DB_COLUMN_KEY, foodGlucloserId);
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);
		
		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(MEAL_TO_FOOD_DB_NAME);
			ret = populateParseObject(query.get(parseId));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(MEAL_TO_FOOD_DB_NAME));
		}
		
		return ret;
	}
}
