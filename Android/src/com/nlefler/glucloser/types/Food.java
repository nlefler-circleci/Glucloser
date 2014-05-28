package com.nlefler.glucloser.types;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import com.nlefler.glucloser.util.database.upgrade.Tables;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;

public class Food implements Serializable {
	private static final long serialVersionUID = -5496857563408300668L;

	private static final String LOG_TAG = "Glucloser_Food";

	public static final String NAME_DB_COLUMN_KEY = "name";
	public static final String CARBS_DB_COLUMN_KEY = "carbs";
	public static final String IMAGE_DB_COLUMN_KEY = "photo";
	public static final String CORRECTION_DB_COLUMN_KEY = "correction";
	public static final String DATE_EATEN_DB_COLUMN_NAME = "dateEaten";

    @Key
    @AutoIncrement
    @Column(DatabaseUtil.ID_COLUMN_NAME)
    private int id;
    public int getId() {
        return id;
    }

    @Key
    @Column("parseId")
	public String parseId;

    @Key
    @Column(NAME_DB_COLUMN_KEY)
	public String name;

    @Column(CARBS_DB_COLUMN_KEY)
	public int carbs;

    @Column(DATE_EATEN_DB_COLUMN_NAME)
	public Date dateEaten;

    @Column(CORRECTION_DB_COLUMN_KEY)
	public boolean isCorrection;

    @Column(DatabaseUtil.CREATED_AT_COLUMN_NAME)
	public Date createdAt;
    @Column(DatabaseUtil.UPDATED_AT_COLUMN_NAME)
	public Date updatedAt;
    @Column(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME)
	public boolean needsUpload;
    @Column(DatabaseUtil.DATA_VERSION_COLUMN_NAME)
	public int dataVersion;
	
	public Food() {
		this.parseId = UUID.randomUUID().toString();

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
			ParseQuery query = new ParseQuery(Tables.FOOD_DB_NAME);
			ret = populateParseObject(query.get(parseId));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.FOOD_DB_NAME));
		}
		
		return ret;
	}

	public static Food fromMap(Map<String, Object> map) {
		Food food = new Food();

		food.parseId = (String)map.get(DatabaseUtil.PARSE_ID_COLUMN_NAME);
		food.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		food.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		food.name = (String)map.get(NAME_DB_COLUMN_KEY);
		food.carbs = (Integer)map.get(CARBS_DB_COLUMN_KEY);
		food.isCorrection = (Boolean)map.get(CORRECTION_DB_COLUMN_KEY);

		try {
			food.dateEaten = DatabaseUtil.parseDateFormat.parse((String)map.get(Food.DATE_EATEN_DB_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			food.createdAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			food.updatedAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return food;
	};

	@Override
	public boolean equals(Object o) {
		if (o instanceof Food) {
			Food f = (Food)o;
			return
					this.name.equals(f.name) &&
					this.carbs == f.carbs &&
					this.isCorrection == f.isCorrection;
		}
		return false;
	}

	public Calendar getDateEaten() {
		return (Calendar)this.dateEaten.clone();
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
