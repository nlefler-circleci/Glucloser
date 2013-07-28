package com.hagia.glucloser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.hagia.glucloser.R;
import com.hagia.glucloser.fragments.add.AddMealFragment;
import com.hagia.glucloser.fragments.edit.EditPlacesFragment;
import com.hagia.glucloser.fragments.history.HistoryFragment;
import com.hagia.glucloser.fragments.home.HomeFragment;
import com.hagia.glucloser.fragments.stats.StatsFragment;
import com.hagia.glucloser.util.LocationUtil;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.hagia.glucloser.util.database.save.SaveManager;

import com.parse.Parse;

import com.bugsense.trace.BugSenseHandler;

public class PumpActivity extends Activity {
	private static final String LOG_TAG = "Pump_Pump";
	public static final int LOG_LEVEL = Log.VERBOSE;

	private static PumpActivity instance;
	private ActionBar actionBar;
	private Bundle fragmentArgs;
	private Handler handler;

	public enum NavigationItem {
		HomeItem,
		AddMealItem,
		HistoryItem,
		StatsItem
	}
	private static String[] navigationItemNames;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		if (instance == null) {
			instance = this;
		}

		setContentView(R.layout.main);
		navigationItemNames = getResources().getStringArray(R.array.action_bar_action_list);

		handler = new Handler();
		
		// Initialize BugSense
		BugSenseHandler.initAndStartSession(this.getApplicationContext(), getString(R.string.bugsense_api_key));
		BugSenseHandler.setLogging(true);

		// Initialize parse
		Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_api_key));

		// Initialize Database
		DatabaseUtil.initialize(this);

		// Initialize Location services
		LocationUtil.initialize(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this.getApplicationContext());
		
		SaveManager.initialize();

		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

		SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, 
				R.array.action_bar_action_list, android.R.layout.simple_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(spinnerAdapter, new ActionBar.OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				boolean success = internalSelectNavigationItemWithBundle(
						getNavigationItemForPosition(itemPosition), fragmentArgs);
				fragmentArgs = null;

				return success;
			}
		});

		if (savedInstanceState != null) {
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
				
		getFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
			
			@Override
			public void onBackStackChanged() {
				// Update Action Bar
				if (getFragmentManager().getBackStackEntryCount() == 0) {
					finish();
				}
			}
		});

		// Turn off syncing on startup. Without any other clients
		// it serves no purpose.
		//DatabaseUtil.instance().startNetworkSyncServiceUsingContext(this);
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.pump_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPause() {
		Log.i(LOG_TAG, "Pause");

		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.i(LOG_TAG, "Stop");
		LocationUtil.shutdown();
		DatabaseUtil.syncIfNeeded();
		BugSenseHandler.closeSession(PumpActivity.this);

		super.onStop();
	}

	@Override
	protected void onResume() {
		Log.i(LOG_TAG, "Resume");

		LocationUtil.initialize(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this.getApplicationContext());

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		Log.i(LOG_TAG, "Destroy");

		super.onDestroy();
	}

	public static PumpActivity getPumpActivity() {
		return instance;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.sync:
			DatabaseUtil.instance().startNetworkSyncServiceUsingContext(PumpActivity.getPumpActivity().getApplicationContext());
			return true;
        case R.id.edit_places:
            showFragment(new EditPlacesFragment(), "Edit Places");
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void selectNavigationItemWithBundle(NavigationItem item,
			Bundle bundle) {
		int navigationPosition = 0;

		switch (item) {
		case HomeItem:
			navigationPosition = 0;
			break;
		case AddMealItem:
			navigationPosition = 1;
			break;
		case HistoryItem:
			navigationPosition = 2;
			break;
		case StatsItem:
			navigationPosition = 3;
			break;
		default:
			navigationPosition = 0;
		}

		fragmentArgs = bundle;
		actionBar.setSelectedNavigationItem(navigationPosition);
	}

	public void showFragment(Fragment fragment, String tag) {
		if (tag == null) {
			tag = "NOTAG";
		}

		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.main_fragment_container, 
				fragment, tag);
		transaction.addToBackStack(tag); 
		transaction.commit();
	}

	public void popFragmentStack() {
		FragmentManager manager = getFragmentManager();
		manager.popBackStack();
	}
	
	public void showModalMessage(final String title, final String message) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(PumpActivity.this);

				builder.setTitle(title);
				builder.setMessage(message);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				});

				AlertDialog alert = builder.create();
				alert.show();
			}
			
		});
	}

	private boolean internalSelectNavigationItemWithBundle(NavigationItem item,
			Bundle bundle) {
		Fragment fragment = null;
		int navigationPosition = 0;

		switch (item) {
		case HomeItem:
			fragment = new HomeFragment();
			navigationPosition = 0;
			break;
		case AddMealItem:
			fragment = new AddMealFragment();
			navigationPosition = 1;
			break;
		case HistoryItem:
			fragment = new HistoryFragment();
			navigationPosition = 2;
			break;
		case StatsItem:
			fragment = new StatsFragment();
			navigationPosition = 3;
			break;
		default:
			fragment = new HomeFragment();
			navigationPosition = 0;
		}

		if (bundle != null) {
			fragment.setArguments(bundle);
		}

		showFragment(fragment, navigationItemNames[navigationPosition]);

		return true;
	}

	private NavigationItem getNavigationItemForPosition(int position) {
		switch (position) {
		case 0:
			// Home
			return NavigationItem.HomeItem;
		case 1:
			// Add Meal
			return NavigationItem.AddMealItem;
		case 2:
			// History
			return NavigationItem.HistoryItem;
		case 3:
			// Stats
			return NavigationItem.StatsItem;
		default:
			return NavigationItem.HomeItem;
		}
	}
	
}
