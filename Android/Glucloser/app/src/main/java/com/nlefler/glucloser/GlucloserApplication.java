package com.nlefler.glucloser;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseCrashReporting;

/**
 * Created by Nathan Lefler on 12/12/14.
 */
public class GlucloserApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseCrashReporting.enable(this);
        Parse.initialize(this, this.getString(R.string.parse_app_id),
                this.getString(R.string.parse_client_key));
    }
}
