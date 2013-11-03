package com.hagia.glucloser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.hagia.glucloser.fragments.add.AddMealFragment;
import com.hagia.glucloser.fragments.edit.EditPlacesFragment;
import com.hagia.glucloser.fragments.history.HistoryFragment;
import com.hagia.glucloser.fragments.home.HomeFragment;
import com.hagia.glucloser.fragments.stats.StatsFragment;
import com.hagia.glucloser.util.LocationUtil;
import com.hagia.glucloser.util.database.DatabaseUtil;
import com.hagia.glucloser.util.database.save.SaveManager;

import com.parse.Parse;

public class GlucloserActivity extends Activity {
	private static final String LOG_TAG = "Glucloser_MainActivity";
	public static final int LOG_LEVEL = Log.VERBOSE;

    // TODO: Why do I need this
	private static GlucloserActivity instance;

	private ActionBar _actionBar;
    private DrawerLayout _drawerLayout;
    private ActionBarDrawerToggle _drawerToggle;
    private ListView _drawerList;

	private enum NavigationItem {
		HomeItem,
		AddMealItem,
		HistoryItem,
		StatsItem
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		if (instance == null) {
			instance = this;
		}

		setContentView(R.layout.main);

		String[] navigationItemNames = getResources().getStringArray(R.array.action_bar_action_list);
        _drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        _drawerToggle = new ActionBarDrawerToggle(this, _drawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open_action, R.string.drawer_close_action){
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle("Draw Open");
                invalidateOptionsMenu();
            }
        };
        _drawerLayout.setDrawerListener(_drawerToggle);
        _drawerList = (ListView) findViewById(R.id.drawer_list);

        _drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, navigationItemNames));
        _drawerList.setOnItemClickListener(new DrawerItemClickListener());

        _actionBar = getActionBar();
        _actionBar.setDisplayHomeAsUpEnabled(true);
        _actionBar.setHomeButtonEnabled(true);

        // Initialize crash reporting
        Crashlytics.start(this);

		Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_api_key));

		DatabaseUtil.initialize(this);

		LocationUtil.initialize(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this.getApplicationContext());
		
		SaveManager.initialize();

		getFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
			
			@Override
			public void onBackStackChanged() {
				if (getFragmentManager().getBackStackEntryCount() == 0) {
					finish();
				}
			}
		});

		// Turn off syncing on startup. Without any other clients
		// it serves no purpose.
		//DatabaseUtil.instance().startNetworkSyncServiceUsingContext(this);
		
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        _drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _drawerToggle.onConfigurationChanged(newConfig);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.glucloser_main_activity_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onStop() {
		LocationUtil.shutdown();
		DatabaseUtil.syncIfNeeded();

		super.onStop();
	}

	@Override
	protected void onResume() {
		LocationUtil.initialize(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this.getApplicationContext());

		super.onResume();
	}

	public static GlucloserActivity getPumpActivity() {
		return instance;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

		switch (item.getItemId()) {
		case R.id.sync:
			DatabaseUtil.instance().startNetworkSyncServiceUsingContext(this.getApplicationContext());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void popFragmentStack() {
		FragmentManager manager = getFragmentManager();
		manager.popBackStack();
	}
	
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            Fragment fragment = null;
            String tag = "tag";

            switch (position)
            {
                default:
                case 0:
                    fragment = new HomeFragment();
                    break;
                case 1:
                    fragment = new AddMealFragment();
                    break;
                case 2:
                    fragment = new HistoryFragment();
                    break;
                case 3:
                    fragment = new StatsFragment();
                    break;
                case 4:
                    fragment = new EditPlacesFragment();
                    break;
            }

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_fragment_container,
                    fragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
        }
    }

    public static void showFragment(Fragment f, String s)
    {
        return;
    }

    public static void selectNavigationItemWithBundle(int i, Bundle b)
    {
       return;
    }
}

