package com.hagia.pump.util;

public class RequestIdUtil {
	private static long requestId = 0;
	
	public static long getNewId() {
		return requestId++;
	}
}
