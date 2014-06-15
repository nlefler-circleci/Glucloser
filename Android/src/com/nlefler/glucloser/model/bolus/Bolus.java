package com.nlefler.glucloser.model.bolus;

import com.nlefler.glucloser.model.GlucloserBaseModel;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;


@Table(Bolus.BOLUS_DB_NAME)
public class Bolus extends GlucloserBaseModel {
	private static final String LOG_TAG = "Glucloser_Bolus";

	public enum BolusType {
	    BolusTypeUnknown,
	    BolusTypeNormal,
	    BolusTypeDualNormal,
	    BolusTypeDualSquare,
	    BolusTypeSquare
	};

    protected static final String BOLUS_DB_NAME = "bolus";

	private static final String TYPE_NORMAL = "Normal";
	private static final String TYPE_DUAL_NORMAL = "Dual/Normal";
	private static final String TYPE_DUAL_SQUARE = "Dual/Square";
	private static final String TYPE_SQUARE = "Square";
	private static final Map<String, BolusType> typeConversionMap = new HashMap<String, BolusType>() {{
		put(TYPE_NORMAL, BolusType.BolusTypeNormal);
		put(TYPE_DUAL_NORMAL, BolusType.BolusTypeDualNormal);
		put(TYPE_DUAL_SQUARE, BolusType.BolusTypeDualSquare);
		put(TYPE_SQUARE, BolusType.BolusTypeSquare);
	}};
	private static final Map<BolusType, String> typeToReadableMap = new HashMap<BolusType, String>() {{
		put(BolusType.BolusTypeNormal, "Normal");
		put(BolusType.BolusTypeDualNormal, "Dual (Normal)");
		put(BolusType.BolusTypeDualSquare, "Dual (Square)");
		put(BolusType.BolusTypeSquare, "Square");
	}};

	public BolusType type;
	public double units;
	public double duration;
	public Date timeStarted;

	public Bolus(String type, double units, double duration, Date started) {
		type = typeConversionMap.containsKey(type) ? typeConversionMap.get(type) : BolusType.BolusTypeUnknown;
		units = units;
		duration = duration;
		timeStarted = started;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Bolus) {
			Bolus b = (Bolus)o;
			return (this.type.equals(b.type)) && (this.units == b.units) && (this.length == b.length);
		}
		return false;
	}

	public String getTypeForDisplay() {
		if (typeToReadableMap.containsKey(type)) {
			return typeToReadableMap.get(type);
		} else {
			return "Unknown Type";
		}
	}

	public String getUnitsForDisplay() {
		return String.valueOf(units) + "U";
	}

	public String getLengthForDisplay() {
		int hours = (int)(length / (60 * 60));
		length -= hours * 60 * 60;
		int minutes = (int)(length / 60);
		length -= minutes * 60;
		int seconds = (int)(length);

		String ret = "";
		if (hours > 0) {
			ret += String.valueOf(hours) + " hours";
		}
		if (minutes > 0) {
			ret += " " + String.valueOf(minutes) + " minutes";
		}
		if (seconds > 0) {
			ret += " " + String.valueOf(seconds) + " seconds";
		}
		
		return ret;
	}
}
