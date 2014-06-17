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
import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.place.Place;
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

    protected static final String MEAL_DB_NAME = "meal";

    protected static final String PLACE_GLUCLOSER_ID_COLUMN_NAME = "placeGlucloserId";
	public static final String DATE_EATEN_DB_COLUMN_NAME = "dateEaten";

    @Column(DATE_EATEN_DB_COLUMN_NAME)
	public Date dateEaten;

    @Column(PLACE_GLUCLOSER_ID_COLUMN_NAME)
    protected String placeGlucloserId;

    private Place place;
    public List<Food> foods;

	public Meal() {
        super();

		this.dateEaten = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu"))).getTime();
        this.foods = new ArrayList<Food>();
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

    public Place getPlace () {
        return place;
    }

    public void setPlace(Place newPlace) {
        this.place = newPlace;
        this.placeGlucloserId = this.place.glucloserId;
    }

	public void addFood(Food food) {
		this.foods.add(food);
	}

	public void removeFood(Food food) {
		this.foods.remove(food);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meal)) return false;

        Meal meal = (Meal) o;

        if (dataVersion != meal.dataVersion) return false;
        if (dateEaten != null ? !dateEaten.equals(meal.dateEaten) : meal.dateEaten != null)
            return false;
        if (glucloserId != null ? !glucloserId.equals(meal.glucloserId) : meal.glucloserId != null)
            return false;
        if (parseId != null ? !parseId.equals(meal.parseId) : meal.parseId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = glucloserId != null ? glucloserId.hashCode() : 0;
        result = 31 * result + (parseId != null ? parseId.hashCode() : 0);
        result = 31 * result + (dateEaten != null ? dateEaten.hashCode() : 0);
        result = 31 * result + dataVersion;
        return result;
    }
}
