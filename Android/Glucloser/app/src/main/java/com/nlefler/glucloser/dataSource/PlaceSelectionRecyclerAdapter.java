package com.nlefler.glucloser.dataSource;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nlefler.glucloser.R;
import com.nlefler.glucloser.models.Place;
import com.nlefler.glucloser.models.PlaceSelectionDelegate;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;

import java.util.List;

/**
 * Created by Nathan Lefler on 12/20/14.
 */
public class PlaceSelectionRecyclerAdapter extends RecyclerView.Adapter<PlaceSelectionRecyclerAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected NLFoursquareVenue venue;
        protected TextView placeName;
        protected TextView placeDistance;
        protected View.OnClickListener clickListener;

        public ViewHolder(View itemView, final PlaceSelectionDelegate delegate) {
            super(itemView);

            this.placeName = (TextView)itemView.findViewById(R.id.place_selection_place_detail_name);
            this.placeDistance = (TextView)itemView.findViewById(R.id.place_selection_place_detail_distance);
            this.clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (delegate != null) {
                        delegate.placeSelected(getPlace());
                    }
                }
            };
            itemView.setOnClickListener(this.clickListener);
        }

        private Place getPlace() {
            return PlaceFactory.FromFoursquareVenue(this.venue);
        }
    }

    private List<NLFoursquareVenue> venues;
    private PlaceSelectionDelegate delegate;

    public PlaceSelectionRecyclerAdapter(PlaceSelectionDelegate delegate, List<NLFoursquareVenue> venues) {
        this.delegate = delegate;
        this.venues = venues;
    }

    public void setVenues(List<NLFoursquareVenue> venues) {
        this.venues = venues;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.place_selection_list_item, viewGroup, false);

        // Setup view

        return new ViewHolder(view, delegate);
    }

    // Replaces the contents of a view (invoked by the view holder)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        if (i >= this.venues.size()) {
            return;
        }

        NLFoursquareVenue venue = this.venues.get(i);
        viewHolder.venue = venue;
        viewHolder.placeName.setText(venue.name);
        // TODO: Localized format string
        viewHolder.placeDistance.setText(String.valueOf(venue.location.distance) + " meters");
    }

    @Override
    public int getItemCount() {
        return this.venues.size();
    }
}
