package com.nlefler.glucloser;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nlefler.glucloser.actions.LogMealAction;
import com.nlefler.glucloser.dataSource.PlaceFactory;
import com.nlefler.glucloser.models.Meal;
import com.nlefler.glucloser.models.MealDetailDelegate;
import com.nlefler.glucloser.models.Place;
import com.nlefler.glucloser.models.PlaceSelectionDelegate;
import com.nlefler.glucloser.ui.MealDetailsFragment;
import com.nlefler.glucloser.ui.PlaceSelectionFragment;


public class LogMealActivity
        extends ActionBarActivity
        implements PlaceSelectionDelegate, MealDetailDelegate {
    private static String LOG_TAG = "LogMealActivity";

    private LogMealAction logMealAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_meal);
        PlaceSelectionFragment fragment = new PlaceSelectionFragment();

        if (savedInstanceState == null) {
            this.logMealAction = new LogMealAction();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Place place = PlaceFactory.PlaceFromCheckInData(this, extras);
            if (place != null) {
                this.placeSelected(place);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_meal, menu);
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

    /** PlaceSelectionDelegate */
    public void placeSelected(Place place) {
        this.logMealAction.setPlace(place);
        switchToMealEditFragment();
    }

    /** MealDetailDelegate */
    public void mealUpdated(Meal meal) {
        this.logMealAction.setMeal(meal);
        finishLoggingMeal();
    }

    /** Helpers */
    private void switchToMealEditFragment() {
        MealDetailsFragment fragment = new MealDetailsFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void finishLoggingMeal() {
        this.logMealAction.log();
        finish();
    }
}
