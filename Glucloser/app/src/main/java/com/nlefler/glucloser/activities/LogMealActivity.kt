package com.nlefler.glucloser.activities

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.nlefler.glucloser.R
import com.nlefler.glucloser.actions.LogMealAction
import com.nlefler.glucloser.dataSource.PlaceFactory
import com.nlefler.glucloser.models.*
import com.nlefler.glucloser.ui.BolusEventDetailsFragment
import com.nlefler.glucloser.ui.PlaceSelectionFragment

public class LogMealActivity : AppCompatActivity(), PlaceSelectionDelegate, BolusEventDetailDelegate, FoodDetailDelegate {

    private var logMealAction: LogMealAction = LogMealAction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_meal)
        val fragment = PlaceSelectionFragment()

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.log_meal_activity_container, fragment).commit()
        }

        val intent = getIntent()
        val extras = intent.getExtras()
        if (extras != null) {
            val placeParcelable = PlaceFactory.PlaceParcelableFromCheckInData(extras)
            if (placeParcelable != null) {
                this.placeSelected(placeParcelable)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_meal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    /** PlaceSelectionDelegate  */
    override fun placeSelected(placeParcelable: PlaceParcelable) {
        this.logMealAction.setPlaceParcelable(placeParcelable)
        switchToMealEditFragment(placeParcelable)
    }

    /** MealDetailDelegate  */
    override fun bolusEventDetailUpdated(bolusEventParcelable: BolusEventParcelable) {
        this.logMealAction.setMealParcelable(bolusEventParcelable as MealParcelable)
        finishLoggingMeal()
    }

    /** FoodDetailDelegate */
    override fun foodDetailUpdated(foodParcelable: FoodParcelable) {
        this.logMealAction.addFoodParcelable(foodParcelable)
    }

    /** Helpers  */
    private fun switchToMealEditFragment(placeParcelable: PlaceParcelable) {
        val fragment = BolusEventDetailsFragment()

        val args = Bundle()
        args.putString(BolusEventDetailsFragment.BolusEventDetailPlaceNameBundleKey, placeParcelable.getName())
        args.putParcelable(BolusEventDetailsFragment.BolusEventDetailBolusEventParcelableBundleKey, MealParcelable())
        fragment.setArguments(args)

        getSupportFragmentManager().beginTransaction().replace(R.id.log_meal_activity_container, fragment).addToBackStack(null).commit()
    }

    private fun finishLoggingMeal() {
        this.logMealAction.log()
        finish()
    }

    companion object {
        private val LOG_TAG = "LogMealActivity"
    }
}