package com.nlefler.glucloser

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Outline
import android.os.Build
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarActivity
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.nlefler.glucloser.dataSource.MealHistoryRecyclerAdapter
import com.nlefler.glucloser.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.models.Meal
import com.nlefler.glucloser.ui.DividerItemDecoration
import com.parse.ParseAnalytics

import java.util.ArrayList

import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults


public class MainActivity : ActionBarActivity(), AdapterView.OnItemClickListener {

    private var navBarItems: Array<String>? = null
    private var navDrawerLayout: DrawerLayout? = null
    private var navDrawerListView: ListView? = null
    private var navDrawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ActionBarActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, HistoryListFragment()).commit()
        }

        this.navBarItems = array(getString(R.string.nav_drawer_item_home), getString(R.string.nav_drawer_item_foursquare_login))
        this.navDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        this.navDrawerListView = findViewById(R.id.left_drawer) as ListView
        this.navDrawerListView!!.setAdapter(ArrayAdapter(this, R.layout.drawer_list_item, this.navBarItems))
        this.navDrawerListView!!.setOnItemClickListener(this)

        this.navDrawerToggle = ActionBarDrawerToggle(this, this.navDrawerLayout, R.string.nav_drawer_open, R.string.nav_drawer_closed)
        this.navDrawerLayout!!.setDrawerListener(this.navDrawerToggle)

        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
        getSupportActionBar().setHomeButtonEnabled(true)

        ParseAnalytics.trackAppOpenedInBackground(getIntent())
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super<ActionBarActivity>.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        this.navDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super<ActionBarActivity>.onConfigurationChanged(newConfig)
        this.navDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu)
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
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (this.navDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }
        // Handle your other action bar items...

        return super<ActionBarActivity>.onOptionsItemSelected(item)
    }

    /** OnClickListener  */
    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (position == 1) {
            FoursquareAuthManager.SharedManager().startAuthRequest(this)
        }
    }

    /** Foursquare Connect Intent  */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FoursquareAuthManager.FOURSQUARE_CONNECT_INTENT_CODE -> {
                FoursquareAuthManager.SharedManager().gotAuthResponse(this, resultCode, data ?: Intent())
            }
            FoursquareAuthManager.FOURSQUARE_TOKEN_EXCHG_INTENT_CODE -> {
                FoursquareAuthManager.SharedManager().gotTokenExchangeResponse(this, resultCode, data ?: Intent())
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class HistoryListFragment : Fragment() {

        private var mealHistoryListView: RecyclerView? = null
        private var mealHistoryLayoutManager: RecyclerView.LayoutManager? = null
        private var mealHistoryAdapter: MealHistoryRecyclerAdapter? = null

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)

            this.mealHistoryListView = rootView.findViewById(R.id.meal_history_list) as RecyclerView

            this.mealHistoryLayoutManager = LinearLayoutManager(getActivity())
            this.mealHistoryListView!!.setLayoutManager(this.mealHistoryLayoutManager)

            this.mealHistoryAdapter = MealHistoryRecyclerAdapter(ArrayList<Meal>())
            this.mealHistoryListView!!.setAdapter(this.mealHistoryAdapter)
            this.mealHistoryListView!!.addItemDecoration(DividerItemDecoration(getActivity()))

            val floatingActionsMenu = rootView.findViewById(R.id.main_floating_action_menu) as FloatingActionsMenu
            val logMealButton = rootView.findViewById(R.id.fab_log_meal_item) as FloatingActionButton
            logMealButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val intent = Intent(view.getContext(), javaClass<LogMealActivity>())
                    view.getContext().startActivity(intent)
                    floatingActionsMenu.collapse()
                }
            })
            val logSnackButton = rootView.findViewById(R.id.fab_log_snack_item) as FloatingActionButton
            logSnackButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val intent = Intent(view.getContext(), javaClass<LogSnackActivity>())
                    view.getContext().startActivity(intent)
                    floatingActionsMenu.collapse()
                }
            })

            updateMealHistory()

            return rootView
        }

        private fun updateMealHistory() {
            val realm = Realm.getInstance(getActivity())
            val query = realm.where<Meal>(javaClass<Meal>())
            val results = query.findAll()
            results.sort(Meal.MealDateFieldName, false)
            this.mealHistoryAdapter!!.setMeals(results)
        }

        companion object {
            private val LOG_TAG = "PlaceholderFragment"
        }
    }

    companion object {
        private val LOG_TAG = "MainActivity"
    }
}
