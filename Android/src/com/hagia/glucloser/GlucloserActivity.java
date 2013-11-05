package com.hagia.glucloser;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

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

    private enum DrawerItem {
        Home, AddMeal, History, Stats, EditPlaces, Invalid;

        public static String fragmentNameForItem(DrawerItem item) {
            switch (item) {
                case Home:
                    return HomeFragment.class.getName();
                case AddMeal:
                    return AddMealFragment.class.getName();
                case History:
                    return HistoryFragment.class.getName();
                case Stats:
                    return StatsFragment.class.getName();
                case EditPlaces:
                    return EditPlacesFragment.class.getName();
                case Invalid:
                default:
                    Log.e(LOG_TAG, "Invalid drawer item");
                    return "DrawerItemInvalid";
            }
        }

        public static DrawerItem itemForFragmentName(String name) {
            for (DrawerItem item : values()) {
                if (name.equals(fragmentNameForItem(item))) {
                    return item;
                }
            }
            Log.e(LOG_TAG, "Invalid name for drawer item");
            return Invalid;
        }
    };

    private DrawerLayout _drawerLayout;
    private ActionBarDrawerToggle _drawerToggle;
    private ListView _drawerList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize crash reporting
        Crashlytics.start(this);

		setContentView(R.layout.main);

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

        String[] drawerItemNames = getResources().getStringArray(R.array.action_bar_action_list);
        _drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItemNames));
        _drawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


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
				} else {
                    String fragmentName = getFragmentManager().getBackStackEntryAt(0).getName();
                    forceUpdateSelectedDrawerItem(DrawerItem.itemForFragmentName(fragmentName));
                }
			}
		});

        if (savedInstanceState == null) {
            selectDrawerItem(DrawerItem.Home);
            forceUpdateSelectedDrawerItem(DrawerItem.Home);
        }

        handleIntent(getIntent());
		// Turn off syncing on startup. Without any other clients
		// it serves no purpose.
		//DatabaseUtil.instance().startNetworkSyncServiceUsingContext(this);
		
	}

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
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

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)menu.findItem(R.id.search_bar).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
	}

	@Override
	protected void onStop() {
		LocationUtil.shutdown();
		DatabaseUtil.syncIfNeeded(this);

		super.onStop();
	}

	@Override
	protected void onResume() {
		LocationUtil.initialize(
				(LocationManager) this.getSystemService(Context.LOCATION_SERVICE),
				this.getApplicationContext());

		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

		switch (item.getItemId()) {
		case R.id.sync_item:
			DatabaseUtil.instance().startNetworkSyncServiceUsingContext(this.getApplicationContext());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    public void pushFragment(Fragment fragment, Bundle args) {
        if (args != null) {
            fragment.setArguments(args);
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container,
                fragment, fragment.getClass().getName());
        transaction.addToBackStack(fragment.getClass().getName());
        transaction.commit();
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            boolean needToPushHome = false;
            HomeFragment homeFragment = null;
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                String tag = getFragmentManager().getBackStackEntryAt(0).getName();
                Fragment topFragment = getFragmentManager().findFragmentByTag(tag);
                if (topFragment instanceof HomeFragment) {
                    homeFragment = (HomeFragment)topFragment;
                } else {
                    needToPushHome = true;
                }
            } else {
                needToPushHome = true;
            }

            if (needToPushHome) {
                homeFragment = new HomeFragment();
                pushFragment(homeFragment, null);
            }

            homeFragment.search(intent.getStringExtra(SearchManager.QUERY));
        } else {
            Log.e(LOG_TAG, "Got unrecongnized intent: " + intent.getAction());
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        private DrawerItem[] itemValues = DrawerItem.values();

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectDrawerItem(itemValues[position]);
        }
    }

    private void forceUpdateSelectedDrawerItem(DrawerItem drawerItem) {
        // TODO: Is there a better way to update the drawer ui?
        _drawerList.setSelection(drawerItem.ordinal());
        _drawerLayout.closeDrawer(Gravity.LEFT);
        _drawerToggle.syncState();
    }

    private void selectDrawerItem(DrawerItem drawerItem) {
        Fragment fragment = null;

        switch (drawerItem)
        {
            default:
            case Home:
                fragment = new HomeFragment();
                break;
            case AddMeal:
                fragment = new AddMealFragment();
                break;
            case History:
                fragment = new HistoryFragment();
                break;
            case Stats:
                fragment = new StatsFragment();
                break;
            case EditPlaces:
                fragment = new EditPlacesFragment();
                break;
        }

        pushFragment(fragment, null);

        _drawerLayout.closeDrawer(Gravity.LEFT);
    }
}

