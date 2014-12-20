package com.nlefler.glucloser.foursquare;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.LocationRequest;
import com.nlefler.glucloser.R;
import com.nlefler.nlfoursquare.Common.NLFoursquareEndpointParametersBuilder;
import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse;
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;
import com.nlefler.nlfoursquare.Model.Venue.Search.NLFoursquareVenueSearchResponse;
import com.nlefler.nlfoursquare.Search.NLFoursquareVenueSearch;
import com.nlefler.nlfoursquare.Search.NLFoursquareVenueSearchParametersBuilder;

import java.util.ArrayList;
import java.util.List;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by Nathan Lefler on 12/12/14.
 */
public class FoursquarePlaceHelper {
    private static String FOURSQUARE_ENDPOINT = "https://api.foursquare.com/v2";

    private RestAdapter restAdapter;
    private NLFoursquareClientParameters clientParameters;
    private ReactiveLocationProvider locationProvider;
    private Subscription locationSubscription;
    private Location lastLocation;

    public FoursquarePlaceHelper(Context ctx) {
        String appId = ctx.getString(R.string.foursquare_app_id);
        String appSecret = ctx.getString(R.string.foursquare_app_secret);
        this.clientParameters = new NLFoursquareClientParameters(appId, appSecret);

        this.locationProvider = new ReactiveLocationProvider(ctx);


        this.locationSubscription = locationProvider.getUpdatedLocation(createLocationRequest())
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        lastLocation = location;
                    }
                });

        this.restAdapter = new RestAdapter.Builder()
                .setEndpoint(FOURSQUARE_ENDPOINT)
                .build();
    }

    @Override
    public void finalize() {
        this.locationSubscription.unsubscribe();
    }

    public Observable<List<NLFoursquareVenue>> closestVenues() {
        return Observable.create(
                new Observable.OnSubscribe<List<NLFoursquareVenue>>() {
                    @Override
                    public void call(final Subscriber<? super List<NLFoursquareVenue>> subscriber) {
                        if (lastLocation != null) {
                            closestVenuesHelper(lastLocation, subscriber);
                        }
                        else {
                            LocationSubscription subscription = new LocationSubscription();
                            Action1<Location> action = new Action1<Location>() {
                                        @Override
                                        public void call(Location location) {
                                            locationSubscription.unsubscribe();
                                            closestVenuesHelper(location, subscriber);
                                        }
                                    };
                            subscription.subscribe(action);
                        }
                    }
                }
        );
    }

    private void closestVenuesHelper(Location location,
                                     final Subscriber<? super List<NLFoursquareVenue>> subscriber) {
         NLFoursquareVenueSearchParametersBuilder parametersBuilder =
                new NLFoursquareVenueSearchParametersBuilder();
        parametersBuilder.latLon(location.getLatitude(),
                location.getLongitude());

        NLFoursquareVenueSearch venueSearch = restAdapter.create(NLFoursquareVenueSearch.class);
        venueSearch.search(parametersBuilder.buildWithClientParameters(clientParameters),
                new Callback<NLFoursquareResponse<NLFoursquareVenueSearchResponse>>() {
                    @Override
                    public void success(NLFoursquareResponse<NLFoursquareVenueSearchResponse> foursquareResponse,
                                        Response response) {
                        subscriber.onNext(foursquareResponse.response.venues);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        subscriber.onNext(new ArrayList<NLFoursquareVenue>());
                    }
                });
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setFastestInterval(250).setInterval(1000);
        locationRequest.setNumUpdates(1).setSmallestDisplacement(10.0f);

        return locationRequest;
    }

    private class LocationSubscription {
        protected Subscription subscription;

        protected void subscribe(Action1<Location> action) {
            locationProvider.getUpdatedLocation(createLocationRequest())
                    .subscribe(action);
        }

        protected void unsubscribe() {
           if (this.subscription != null) {
               this.subscription.unsubscribe();
           }
        }
    }
}
