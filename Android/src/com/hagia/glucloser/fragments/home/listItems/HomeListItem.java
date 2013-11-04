package com.hagia.glucloser.fragments.home.listItems;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hagia.glucloser.types.Place;

/**
 * Created by lefler on 11/3/13.
 */
public interface HomeListItem {
    public Place getPlace();
    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent);
    public long getItemId();
}
