package com.hagia.pump.util.database.pushers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class NoOpPusher extends SyncPusher {

	@Override
	public List<Map<String, Object>> getRecordsSinceDate(Date sinceDate) {
		return new ArrayList<Map<String, Object>>();
	}

	@Override
	public Date pushRecords(List<Map<String, Object>> objects) {
		return null;
	}

}
