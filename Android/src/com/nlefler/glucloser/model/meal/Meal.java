package com.nlefler.glucloser.model.meal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import android.text.format.DateFormat;
import android.util.Log;

import com.nlefler.glucloser.model.GlucloserBaseModel;
import com.nlefler.glucloser.model.MealToFood;
import com.nlefler.glucloser.model.placetomeal.PlaceToMeal;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;


@Table(Meal.MEAL_DB_NAME)
public class Meal extends GlucloserBaseModel implements Serializable {
	private static final String LOG_TAG = "Glucloser_Meal";

    protected  static final String MEAL_DB_NAME = "meal";
	public static final String DATE_EATEN_DB_COLUMN_NAME = "dateEaten";

    public static String getDatabaseTableName() {
        return MEAL_DB_NAME;
    }

    @Key
    @AutoIncrement
    @Column(DatabaseUtil.ID_COLUMN_NAME)
    private int id;
    public int getId() {
        return id;
    }

    @Key
    @Column(DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME)
    public String glucloserId;

    @Key
    @Column(DatabaseUtil.PARSE_ID_COLUMN_NAME)
	public String parseId;

    // TODO: Relationship
	public PlaceToMeal placeToMeal;
	public List<MealToFood> mealToFoods;

    @Column(DATE_EATEN_DB_COLUMN_NAME)
	public Date dateEaten;

    @Column(DatabaseUtil.CREATED_AT_COLUMN_NAME)
	public Date createdAt;
    @Column(DatabaseUtil.UPDATED_AT_COLUMN_NAME)
	public Date updatedAt;
    @Column(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME)
	public boolean needsUpload;
    @Column(DatabaseUtil.DATA_VERSION_COLUMN_NAME)
	public int dataVersion;

	public Meal() {
		this.parseId = UUID.randomUUID().toString();
        this.glucloserId = UUID.randomUUID().toString();

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
			ParseQuery query = new ParseQuery(MEAL_DB_NAME);
			ret = populateParseObject(query.get(parseId));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(MEAL_DB_NAME));
		}
		
		return ret;
	}
	
	public static Meal fromMap(Map<String, Object> map) {
		Meal meal = new Meal();

		meal.parseId = (String)map.get(DatabaseUtil.PARSE_ID_COLUMN_NAME);

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
