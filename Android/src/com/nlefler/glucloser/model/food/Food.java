package com.nlefler.glucloser.model.food;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import com.nlefler.glucloser.model.GlucloserBaseModel;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table(Food.FOOD_DB_NAME)
public class Food extends GlucloserBaseModel implements Serializable {
	private static final long serialVersionUID = -5496857563408300668L;

	private static final String LOG_TAG = "Glucloser_Food";

    protected static final String FOOD_DB_NAME = "food";
    
	protected static final String NAME_DB_COLUMN_KEY = "name";
	protected static final String CARBS_DB_COLUMN_KEY = "carbs";
	protected static final String CORRECTION_DB_COLUMN_KEY = "correction";
	protected static final String DATE_EATEN_DB_COLUMN_NAME = "dateEaten";
    protected static final String MEAL_GLUCLOSER_ID_COLUMN_NAME = "mealGlucloserId";

    @Key
    @Column(NAME_DB_COLUMN_KEY)
	public String name;

    @Column(CARBS_DB_COLUMN_KEY)
	public int carbs;

    @Column(DATE_EATEN_DB_COLUMN_NAME)
	public Date dateEaten;

    @Column(CORRECTION_DB_COLUMN_KEY)
	public boolean isCorrection;

    @Column(MEAL_GLUCLOSER_ID_COLUMN_NAME)
    public String mealGlucloserId;

	public Food() {
        super();

		this.carbs = -1;
		this.isCorrection = false;
		this.dateEaten = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu"))).getTime();
	}

	/**
	 * You need to link the returned object with meal and place
	 * @return A partial parse object
	 */
	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(NAME_DB_COLUMN_KEY, this.name);
		pobj.put(CARBS_DB_COLUMN_KEY, this.carbs);
		pobj.put(DATE_EATEN_DB_COLUMN_NAME, this.dateEaten);
		
		pobj.put(CORRECTION_DB_COLUMN_KEY, this.isCorrection);
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(FOOD_DB_NAME);
			ret = populateParseObject(query.get(parseId));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(FOOD_DB_NAME));
		}
		
		return ret;
	}

	public Calendar getDateEatenForDisplay() {
		TimeZone tz = TimeZone.getDefault();
		Calendar toTZ = (Calendar) this.dateEaten.clone();
		toTZ.setTimeZone(tz);

		return toTZ;
	}
	
	public void setNowAsDateEaten() {
		this.dateEaten = (Calendar.getInstance(TimeZone.getTimeZone("Etc/Zulu"))).getTime();
	}

}
