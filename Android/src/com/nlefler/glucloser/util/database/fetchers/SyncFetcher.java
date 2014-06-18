package com.nlefler.glucloser.util.database.fetchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.nlefler.glucloser.model.GlucloserBaseModel;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQuery.CachePolicy;

public class SyncFetcher {
	private static final String LOG_TAG = "Glucloser_Sync_Fetcher";
	// 1K is the highest limit supported by Parse but this uses a ton of memory
	// within the Parse SDK
	private static final int PARSE_FETCH_LIMIT = 1000;
	
	private boolean hasMoreRecords = true;
    private Class modelClass;

    public SyncFetcher(Class modelClass) {
        this.modelClass = modelClass;
    }

	public boolean hasMoreRecords() {
		return hasMoreRecords;
	}
	
	public Date importRecordsSince(Date sinceDate) {
        Log.i(LOG_TAG, "Starting fetch from Parse");

        List<ParseObject> parseObjects =
                fetchParseObjectsForModelSinceDate(modelClass, sinceDate, LOG_TAG);

        Date lastDate = null;
        for (ParseObject object : parseObjects) {
            GlucloserBaseModel model = GlucloserBaseModel.fromParseObject(modelClass, object);
            if (model.save()) {
                if (lastDate == null || model.updatedAt.compareTo(lastDate) == 1) {
                    lastDate = model.updatedAt;
                }
            }
        }

        return lastDate;
    }
	
	protected List<ParseObject> fetchParseObjectsForModelSinceDate(Class modelClass,
			Date sinceDate, String logTag) {
		
		ParseQuery syncQuery = new ParseQuery(DatabaseUtil.tableNameForModel(modelClass));
		syncQuery.setCachePolicy(CachePolicy.NETWORK_ONLY);
		
		if (sinceDate != null) {
			syncQuery.whereGreaterThan(DatabaseUtil.UPDATED_AT_COLUMN_NAME, sinceDate);
		}
		syncQuery.orderByAscending(DatabaseUtil.UPDATED_AT_COLUMN_NAME);
		syncQuery.setLimit(PARSE_FETCH_LIMIT);

		List<ParseObject> results = new ArrayList<ParseObject>();
		try {
			results = syncQuery.find();
			Log.i(LOG_TAG, "Got " + results.size() + " records from Parse");

			hasMoreRecords = !results.isEmpty();
		} catch (ParseException e) {
			Log.e(logTag, "Caught error while fetching records from Parse: " + e.getLocalizedMessage());
			e.printStackTrace();
			hasMoreRecords = false;
		}
		
		return results;
	}
}
