package com.nlefler.glucloser;

import android.app.Application;
import android.os.Debug;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

/**
 * Created by Nathan Lefler on 12/12/14.
 */
public class GlucloserApplication extends Application {
    private static String LOG_TAG = "GlucloserApplication";

    private static GlucloserApplication _sharedApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        _sharedApplication = this;

        if (!Debug.isDebuggerConnected()) {
            ParseCrashReporting.enable(this);
        }

        Parse.initialize(this, this.getString(R.string.parse_app_id),
                this.getString(R.string.parse_client_key));

        this.subscribeToPush();
    }

    public static GlucloserApplication SharedApplication() {
        return _sharedApplication;
    }

    private void subscribeToPush() {
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(LOG_TAG, "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e(LOG_TAG, "failed to subscribe for push", e);
                }
            }
        });

        ParsePush.subscribeInBackground("foursquareCheckin", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the checkin channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }
}
