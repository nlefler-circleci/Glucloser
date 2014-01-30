package com.nlefler.glucloser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.nlefler.glucloser.R;
import com.crashlytics.android.Crashlytics;
import com.nlefler.glucloser.util.database.DatabaseUtil;


public class NetworkSyncService extends Service {
	private static final String LOG_TAG = "Pump_Network_Sync_Service";

	private NotificationManager mNM;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = 434343;

	/**
	 * Class for clients to access.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with
	 * IPC.
	 */
	public class LocalBinder extends Binder {
		NetworkSyncService getService() {
			return NetworkSyncService.this;
		}
	}

	// This is the object that receives interactions from clients.  See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // Initialize crash reporting
        Crashlytics.start(this);
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);
		DatabaseUtil.stopSync();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOG_TAG, "Starting sync service");

		Thread taskThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (DatabaseUtil.instance() != null) {
					Notification notification;
					try {
						 notification = new Notification.Builder(NetworkSyncService.this)
						.setContentTitle("Glucloser is syncing...")
						.setTicker("Glucoser is syncing...")
						.setSmallIcon(R.drawable.launcher_icon)
						.setAutoCancel(true)
						.setOngoing(false)
						.getNotification();

						// Send the notification.
						startForeground(NOTIFICATION, notification);

						DatabaseUtil.instance().syncWithNetwork();

						stopForeground(true);

						// Set the icon, scrolling text and timestamp
						notification = new Notification.Builder(NetworkSyncService.this)
						.setContentTitle("Glucloser is up to date")
						.setTicker("Glucloser is up to date")
						.setSmallIcon(R.drawable.launcher_icon)
						.setAutoCancel(true)
						.setOngoing(false)
						.getNotification();

						// Send the notification.
						mNM.notify(NOTIFICATION, notification);
						mNM.cancel(NOTIFICATION);
					} catch (Exception e) {
                        Log.e(LOG_TAG, "Exception during sync: " + e.getMessage());
						notification = new Notification.Builder(NetworkSyncService.this)
						.setContentTitle("Glucloser was unable to sync")
						.setTicker("Glucloser was unable to sync. Please try later.")
						.setSmallIcon(R.drawable.launcher_icon)
						.setAutoCancel(true)
						.setOngoing(false)
						.getNotification();

						// Send the notification.
						mNM.notify(NOTIFICATION, notification);
						mNM.cancel(NOTIFICATION);
					} finally {
						stopSelf();
					}
				}
			}

		});
        if (isOnline())
        {
		    taskThread.start();
        }

		return super.onStartCommand(intent, flags, startId);
	}

	private boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
}
