package com.nlefler.glucloser.types;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.nlefler.glucloser.util.FoodUtil;
import com.nlefler.glucloser.util.MealUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;

public class MealToFood implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String MEAL_DB_COLUMN_KEY = "meal";
	public static final String FOOD_DB_COLUMN_KEY = "food";

    @Key
    @AutoIncrement
    @Column(DatabaseUtil.ID_COLUMN_NAME)
    private int id;

    @Key
    @Column(DatabaseUtil.PARSE_ID_COLUMN_NAME)
    public String parseId;

    @Key
    @Column(MEAL_DB_COLUMN_KEY)
	public Meal meal;

    @Key
    @Column(FOOD_DB_COLUMN_KEY)
	public Food food;

    @Column(DatabaseUtil.CREATED_AT_COLUMN_NAME)
	public Date createdAt;
    @Column(DatabaseUtil.UPDATED_AT_COLUMN_NAME)
	public Date updatedAt;
    @Column(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME)
	public boolean needsUpload;
    @Column(DatabaseUtil.DATA_VERSION_COLUMN_NAME)
	public int dataVersion;
	
	public MealToFood() {
		this.parseId = UUID.randomUUID().toString();
		this.dataVersion = 1;
	}

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
			ret = populateParseObject(query.get(parseId));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.MEAL_TO_FOOD_DB_NAME));
		}
		
		return ret;
	}
	
	public static MealToFood fromMap(Map<String, Object> map) {
		MealToFood mtf = new MealToFood();
		
		mtf.meal = MealUtil.getMealById((String)map.get(MEAL_DB_COLUMN_KEY));
		mtf.food = FoodUtil.getFoodById((String)map.get(FOOD_DB_COLUMN_KEY));
		mtf.parseId = (String)map.get(DatabaseUtil.PARSE_ID_COLUMN_NAME);

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
