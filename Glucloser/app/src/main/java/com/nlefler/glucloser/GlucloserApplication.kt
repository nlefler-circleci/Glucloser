package com.nlefler.glucloser

import android.app.Application
import android.os.Debug
import android.util.Log

import com.parse.Parse
import com.parse.ParseAnalytics
import com.parse.ParseCrashReporting
import com.parse.ParseException
import com.parse.ParsePush
import com.parse.SaveCallback

/**
 * Created by Nathan Lefler on 12/12/14.
 */
public class GlucloserApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        _sharedApplication = this

        if (!Debug.isDebuggerConnected()) {
            ParseCrashReporting.enable(this)
        }

        Parse.initialize(this, this.getString(R.string.parse_app_id), this.getString(R.string.parse_client_key))

        this.subscribeToPush()
    }

    private fun subscribeToPush() {
        ParsePush.subscribeInBackground("", object : SaveCallback() {
            override fun done(e: ParseException?) {
                if (e == null) {
                    Log.d(LOG_TAG, "successfully subscribed to the broadcast channel.")
                } else {
                    Log.e(LOG_TAG, "failed to subscribe for push", e)
                }
            }
        })

        ParsePush.subscribeInBackground("foursquareCheckin", object : SaveCallback() {
            override fun done(e: ParseException?) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the checkin channel.")
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e)
                }
            }
        })
    }

    companion object {
        private val LOG_TAG = "GlucloserApplication"

        private var _sharedApplication: GlucloserApplication? = null

        synchronized public fun SharedApplication(): GlucloserApplication {
            return _sharedApplication!!
        }
    }
}
