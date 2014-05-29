package com.nlefler.glucloser.model.bolus;

import com.nlefler.glucloser.model.GlucloserBaseModel;

import java.util.Date;

import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;


@Table(Bolus.BOLUS_DB_NAME)
public class Bolus extends GlucloserBaseModel {
	private static final String LOG_TAG = "Glucloser_Bolus";

    protected static final String BOLUS_DB_NAME = "bolus";

	public static final String TYPE_NORMAL = "Normal";
	public static final String TYPE_DUAL_NORMAL = "Dual/Normal";
	public static final String TYPE_DUAL_SQUARE = "Dual/Square";
	public static final String TYPE_SQUARE = "Square";

	public String type;
	public double units;
	public long length;
	public Date timeStarted;

	public Bolus(String t, double u, long l, Date started) {
		type = t;
		units = u;
		length = l;
		timeStarted = started;
	}

	public Bolus(String t, double u, String l, Date started) {
		type = t;
		units = u;
		timeStarted = started;

		String[] timeUnits = l.split(":");
		if (timeUnits.length < 3) {
			length = 0;
		} else {
			int hours = Integer.valueOf(timeUnits[0]);
			int minutes = Integer.valueOf(timeUnits[1]);
			int seconds = Integer.valueOf(timeUnits[2]);

			length = hours * 60 * 60;
			length += minutes * 60;
			length += seconds;
		}
		
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
		if (type.equals(TYPE_NORMAL)) {
			return "Normal";
		} else if (type.equals(TYPE_DUAL_NORMAL)) {
			return "Dual (Normal)";
		} else if (type.equals(TYPE_DUAL_SQUARE)) {
			return "Dual (Square)";
		} else if (type.equals(TYPE_SQUARE)) {
			return "Square";
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
