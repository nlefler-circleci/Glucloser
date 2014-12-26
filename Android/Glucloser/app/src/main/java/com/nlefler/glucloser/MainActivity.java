package com.nlefler.glucloser;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nlefler.glucloser.dataSource.MealHistoryRecyclerAdapter;
import com.nlefler.glucloser.models.Meal;
import com.parse.ParseAnalytics;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        ParseAnalytics.trackAppOpened(getIntent());
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

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when user clicks on button in add card
     */
    public void launchAddMealActivity(View view) {
        Intent intent = new Intent(this, LogMealActivity.class);
        startActivity(intent);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static String LOG_TAG = "PlaceholderFragment";

        private RecyclerView mealHistoryListView;
        private RecyclerView.LayoutManager mealHistoryLayoutManager;
        private MealHistoryRecyclerAdapter mealHistoryAdapter;

        public PlaceholderFragment() {
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

            updateMealHistory();

            return rootView;
        }

        private void updateMealHistory() {
            Realm realm = Realm.getInstance(getActivity());
            RealmQuery<Meal> query = realm.where(Meal.class);
            RealmResults<Meal> results = query.findAll();
            Log.d(LOG_TAG, "Found meals " + results.size());
            this.mealHistoryAdapter.setMeals(results);
        }
    }
}
