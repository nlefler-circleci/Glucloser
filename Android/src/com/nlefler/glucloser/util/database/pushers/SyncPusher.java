package com.nlefler.glucloser.util.database.pushers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nlefler.glucloser.model.GlucloserBaseModel;
import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.ParseException;
import com.parse.ParseObject;

import se.emilsjolander.sprinkles.Query;

public class SyncPusher {
	private static final String LOG_TAG = "Glucloser_Sync_Pusher";

    private Class modelClass;

    public SyncPusher(Class<? extends GlucloserBaseModel> modelClass) {
        this.modelClass = modelClass;
    }

	public Date doSyncSinceDate(Date sinceDate) {
        String select = "SELECT * FROM " + DatabaseUtil.tableNameForModel(modelClass) +
                " WHERE " + DatabaseUtil.UPDATED_AT_COLUMN_NAME + " >= ? AND " +
                DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME + " = 1 ORDER BY " +
                DatabaseUtil.UPDATED_AT_COLUMN_NAME + " ASC";

        List<? extends GlucloserBaseModel> records = Query.many(modelClass, select, sinceDate).get().asList();
        Date lastSyncDate = null;
        for (GlucloserBaseModel record : records) {
            ParseObject parseObject = record.toParseObject();
            if (parseObject != null) {
                try {
                    parseObject.save();
                    record.needsUpload = false;
                    record.save();
                    lastSyncDate = record.updatedAt;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return lastSyncDate;
    }
}
