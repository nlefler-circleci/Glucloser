package com.nlefler.glucloser.model.sync;

import se.emilsjolander.sprinkles.annotations.Table;

/**
 * Created by lefler on 6/17/14.
 */

@Table(SyncUpEvent.SyncUpEventTableName)
public class SyncUpEvent extends SyncEvent {
    protected static final String SyncUpEventTableName = "syncup";
}
