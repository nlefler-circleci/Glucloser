package com.nlefler.glucloser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.nlefler.glucloser.dataSource.MealHistoryRecyclerAdapter;
import com.nlefler.glucloser.foursquare.FoursquareAuthManager;
import com.nlefler.glucloser.models.Meal;
import com.nlefler.glucloser.ui.DividerItemDecoration;
import com.parse.ParseAnalytics;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    private static final String LOG_TAG = "MainActivity";

    private String[] navBarItems;
    private DrawerLayout navDrawerLayout;
    private ListView navDrawerListView;
    private ActionBarDrawerToggle navDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new HistoryListFragment())
                    .commit();
        }

        this.navBarItems = new String[] {
                getString(R.string.nav_drawer_item_home),
                getString(R.string.nav_drawer_item_foursquare_login)
        };
        this.navDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        this.navDrawerListView = (ListView)findViewById(R.id.left_drawer);
        this.navDrawerListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, this.navBarItems));
        this.navDrawerListView.setOnItemClickListener(this);

        this.navDrawerToggle = new ActionBarDrawerToggle(this, this.navDrawerLayout,
                R.string.nav_drawer_open, R.string.nav_drawer_closed);
        this.navDrawerLayout.setDrawerListener(this.navDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        this.navDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.navDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (this.navDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when user clicks on button in add card
     */
    public void launchAddMealActivity(View view) {
    }

    /** OnClickListener */
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        if (position == 1) {
            FoursquareAuthManager.SharedManager().startAuthRequest(this);
        }
    }

    /** Foursquare Connect Intent */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case FoursquareAuthManager.FOURSQUARE_CONNECT_INTENT_CODE: {
                FoursquareAuthManager.SharedManager().gotAuthResponse(this, resultCode, data);
                break;
            }
            case FoursquareAuthManager.FOURSQUARE_TOKEN_EXCHG_INTENT_CODE:
            {
                FoursquareAuthManager.SharedManager().gotTokenExchangeResponse(this, resultCode, data);
                break;
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class HistoryListFragment extends Fragment {
        private static String LOG_TAG = "PlaceholderFragment";

        private RecyclerView mealHistoryListView;
        private RecyclerView.LayoutManager mealHistoryLayoutManager;
        private MealHistoryRecyclerAdapter mealHistoryAdapter;

        public HistoryListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            this.mealHistoryListView = (RecyclerView)rootView.findViewById(R.id.meal_history_list);

            this.mealHistoryLayoutManager = new LinearLayoutManager(getActivity());
            this.mealHistoryListView.setLayoutManager(this.mealHistoryLayoutManager);

            this.mealHistoryAdapter = new MealHistoryRecyclerAdapter(new ArrayList<Meal>());
            this.mealHistoryListView.setAdapter(this.mealHistoryAdapter);
            this.mealHistoryListView.addItemDecoration(new DividerItemDecoration(getActivity(), null));

            FloatingActionButton logMealButton = (FloatingActionButton) rootView.findViewById(R.id.fab_log_meal_item);
            logMealButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), LogMealActivity.class);
                    view.getContext().startActivity(intent);
                }
            });
            FloatingActionButton logSnackButton = (FloatingActionButton) rootView.findViewById(R.id.fab_log_meal_item);
            logSnackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), LogMealActivity.class);
                    view.getContext().startActivity(intent);
                }
            });

            updateMealHistory();

            return rootView;
        }

        private void updateMealHistory() {
            Realm realm = Realm.getInstance(getActivity());
            RealmQuery<Meal> query = realm.where(Meal.class);
            RealmResults<Meal> results = query.findAll();
            results.sort(Meal.MealDateFieldName, false);
            this.mealHistoryAdapter.setMeals(results);
        }
    }
}
