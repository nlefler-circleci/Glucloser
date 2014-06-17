package com.nlefler.glucloser.model.sync;

import com.nlefler.glucloser.model.GlucloserBaseModel;

import se.emilsjolander.sprinkles.annotations.Table;

/**
 * Created by lefler on 6/17/14.
 */

@Table(SyncDownEvent.SyncDownEventTableName)
public class SyncDownEvent extends SyncEvent {
    protected static final String SyncDownEventTableName = "syncdown";
}
