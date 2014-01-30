package com.nlefler.glucloser.types;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nlefler.glucloser.util.database.Tables;
import com.nlefler.glucloser.util.BarcodeUtil;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class Food implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5496857563408300668L;

	private static final String LOG_TAG = "Pump_Food";

	public static final String NAME_DB_COLUMN_KEY = "name";
	public static final String CARBS_DB_COLUMN_KEY = "carbs";
	public static final String IMAGE_DB_COLUMN_KEY = "photo";
	public static final String CORRECTION_DB_COLUMN_KEY = "correction";
	public static final String DATE_EATEN_DB_COLUMN_NAME = "dateEaten";

	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(NAME_DB_COLUMN_KEY, String.class);
		put(CARBS_DB_COLUMN_KEY, Integer.class);
		put(IMAGE_DB_COLUMN_KEY, Byte[].class);
		put(CORRECTION_DB_COLUMN_KEY, Boolean.class);
		put(DATE_EATEN_DB_COLUMN_NAME, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};

	public String id;
	public String name;
	public int carbs;
	public Bitmap foodImage;
	public Date dateEaten;
	public boolean isCorrection;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;
	
	private Barcode barCode;
	
	public Food() {
		this.id = UUID.randomUUID().toString();

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

		if (this.foodImage != null) {
			byte[] data = getImage();
			ParseFile imageFile = new ParseFile("food_photo_" + UUID.randomUUID(), data);

			pobj.put(IMAGE_DB_COLUMN_KEY, imageFile);
		}

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.FOOD_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.FOOD_DB_NAME));
		}
		
		return ret;
	}

	public static Food fromMap(Map<String, Object> map) {
		Food food = new Food();

		food.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);
		food.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		food.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		food.name = (String)map.get(NAME_DB_COLUMN_KEY);
		food.carbs = (Integer)map.get(CARBS_DB_COLUMN_KEY);
		food.isCorrection = (Boolean)map.get(CORRECTION_DB_COLUMN_KEY);
		
		if (map.get(IMAGE_DB_COLUMN_KEY) != null) {
			food.setImageFromBytes((byte[]) map.get(IMAGE_DB_COLUMN_KEY));
		}
		
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

	public void setImageFromBytes(byte[] data) {
		this.foodImage = BitmapFactory.decodeByteArray(data, 0, data.length);
	}
	
	public byte[] getImage() {
		if (this.foodImage == null) {
			return null;
		}
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();  
		this.foodImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
		
		return stream.toByteArray();
	}
	
	public Barcode getBarcode() {
		if (barCode != null) {
			return barCode;
		}
		
		barCode = BarcodeUtil.barCodeForFoodName(this.name);
		if (barCode == null) {
			barCode = new Barcode();
			barCode.foodName = this.name;
		}
		return this.barCode;
	}
	
	public void setBarcodeValue(String value) {
		getBarcode().barCode = value;
	}
	
}
