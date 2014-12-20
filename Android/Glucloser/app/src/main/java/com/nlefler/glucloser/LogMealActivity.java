package com.nlefler.glucloser;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import com.nlefler.glucloser.foursquare.FoursquarePlaceHelper;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.observables.AndroidObservable;
import rx.schedulers.Schedulers;


public class LogMealActivity extends ActionBarActivity implements Observer<List<NLFoursquareVenue>> {

    private FoursquarePlaceHelper foursquareHelper;
    private Subscription closestPlacesSubscription;
    private Scheduler subscriptionScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_meal);
        Fragment fragment = new PlaceholderFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }

        foursquareHelper = new FoursquarePlaceHelper(this);
        subscriptionScheduler = Schedulers.newThread();
        closestPlacesSubscription = AndroidObservable.bindFragment(fragment,
                foursquareHelper.closestVenues())
                .subscribeOn(subscriptionScheduler)
                .subscribe(this);
    }

    @Override
    protected void onDestroy() {
        closestPlacesSubscription.unsubscribe();
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

    /** Observer<T> */
    @Override
    public void onCompleted() {
        this.closestPlacesSubscription.unsubscribe();
    }

    @Override
    public void onError(Throwable e) {
        Log.e("E", e.toString());
    }

    @Override
    public void onNext(List<NLFoursquareVenue> nlFoursquareVenues) {
        Log.d("D", nlFoursquareVenues.size() + "");
        TextView textView = (TextView)this.findViewById(R.id.first_place);
        textView.setText(nlFoursquareVenues.get(0).name);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_log_meal, container, false);
            return rootView;
        }
    }
}
