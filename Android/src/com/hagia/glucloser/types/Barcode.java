package com.hagia.glucloser.types;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hagia.glucloser.util.database.Tables;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class Barcode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5496857563408300668L;

	private static final String LOG_TAG = "Pump_Barcode";

	public static final String BARCODE_DB_COLUMN_KEY = "barCode";
	public static final String FOOD_NAME_DB_COLUMN_KEY = "foodName";

	public static final Map<String, Class> COLUMN_TYPES = new HashMap<String, Class>() {{
		put(BARCODE_DB_COLUMN_KEY, String.class);
		put(FOOD_NAME_DB_COLUMN_KEY, String.class);
		put(DatabaseUtil.OBJECT_ID_COLUMN_NAME, String.class);
		put(DatabaseUtil.CREATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.UPDATED_AT_COLUMN_NAME, String.class);
		put(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME, Boolean.class);
		put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, Integer.class);
	}};

	public String id;
	public String barCode;
	public String foodName;
	public Date createdAt;
	public Date updatedAt;
	public boolean needsUpload;
	public int dataVersion;
	
	public Barcode() {
		this.id = UUID.randomUUID().toString();

		this.barCode = null;
		this.foodName = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((barCode == null) ? 0 : barCode.hashCode());
		result = prime * result
				+ ((foodName == null) ? 0 : foodName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Barcode other = (Barcode) obj;
		if (barCode == null) {
			if (other.barCode != null)
				return false;
		} else if (!barCode.equals(other.barCode))
			return false;
		if (foodName == null) {
			if (other.foodName != null)
				return false;
		} else if (!foodName.equals(other.foodName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}



	/**
	 * @return A full parse object
	 */
	private ParseObject populateParseObject(ParseObject pobj) {
		pobj.put(BARCODE_DB_COLUMN_KEY, this.barCode);
		pobj.put(FOOD_NAME_DB_COLUMN_KEY, this.foodName);
		
		pobj.put(DatabaseUtil.DATA_VERSION_COLUMN_NAME, dataVersion);

		return pobj;
	}
	
	public ParseObject toParseObject() {
		ParseObject ret;
		try {
			ParseQuery query = new ParseQuery(Tables.BARCODE_TO_FOOD_NAME_DB_NAME);
			ret = populateParseObject(query.get(id));
		} catch (ParseException e) {
			ret = populateParseObject(new ParseObject(Tables.BARCODE_TO_FOOD_NAME_DB_NAME));
		}
		
		return ret;
	}

	public static Barcode fromMap(Map<String, Object> map) {
		Barcode barCode = new Barcode();

		barCode.id = (String)map.get(DatabaseUtil.OBJECT_ID_COLUMN_NAME);
		barCode.needsUpload = (Boolean)map.get(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME);
		barCode.dataVersion = (Integer)map.get(DatabaseUtil.DATA_VERSION_COLUMN_NAME);
		barCode.barCode = (String)map.get(BARCODE_DB_COLUMN_KEY);
		barCode.foodName = (String)map.get(FOOD_NAME_DB_COLUMN_KEY);
		
		try {
			barCode.createdAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			barCode.updatedAt = DatabaseUtil.parseDateFormat.parse((String)map.get(DatabaseUtil.CREATED_AT_COLUMN_NAME));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return barCode;
	}
}
