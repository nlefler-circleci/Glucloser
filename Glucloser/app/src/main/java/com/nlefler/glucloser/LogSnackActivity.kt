package com.nlefler.glucloser

import android.support.v7.app.ActionBarActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import com.nlefler.glucloser.actions.LogSnackAction
import com.nlefler.glucloser.models.BolusEventDetailDelegate
import com.nlefler.glucloser.models.BolusEventParcelable
import com.nlefler.glucloser.models.SnackParcelable
import com.nlefler.glucloser.ui.MealDetailsFragment


public class LogSnackActivity : ActionBarActivity(), BolusEventDetailDelegate {

    private var logSnackAction: LogSnackAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ActionBarActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_snack)

        val fragment = MealDetailsFragment()
        val args = Bundle()
        args.putParcelable(MealDetailsFragment.MealDetailBolusEventParcelableBundleKey, SnackParcelable())
        fragment.setArguments(args)

        this.logSnackAction = LogSnackAction()
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.log_snack_activity_container, fragment).commit()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_snack, menu)
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

        return super<ActionBarActivity>.onOptionsItemSelected(item)
    }

    /** MealDetailDelegate  */
    override fun bolusEventDetailUpdated(bolusEventParcelable: BolusEventParcelable) {
        this.logSnackAction!!.snackParcelable = bolusEventParcelable as SnackParcelable
        this.logSnackAction!!.log()
        finish()
    }

    companion object {
        private val LOG_TAG = "LogSnackActivity"
    }
}
