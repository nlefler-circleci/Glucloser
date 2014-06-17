package com.nlefler.glucloser.model;

import android.database.Cursor;

import com.nlefler.glucloser.util.database.DatabaseUtil;

import java.util.Date;
import java.util.UUID;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.typeserializers.TypeSerializer;

/**
 * Created by Nathan Lefler on 5/28/14.
 */
public class GlucloserBaseModel extends Model {

    @Key
    @AutoIncrement
    @Column(DatabaseUtil.ID_COLUMN_NAME)
    private int id;
    public int getId() {
        return id;
    }

    @Key
    @Column(DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME)
    public String glucloserId;

    @Key
    @Column(DatabaseUtil.PARSE_ID_COLUMN_NAME)
    public String parseId;

    @Column(DatabaseUtil.CREATED_AT_COLUMN_NAME)
    public Date createdAt;

    @Column(DatabaseUtil.UPDATED_AT_COLUMN_NAME)
    public Date updatedAt;

    @Column(DatabaseUtil.NEEDS_UPLOAD_COLUMN_NAME)
    public boolean needsUpload;

    @Column(DatabaseUtil.DATA_VERSION_COLUMN_NAME)
    public int dataVersion;

    public GlucloserBaseModel() {
        this.glucloserId = UUID.randomUUID().toString();
        this.parseId = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.dataVersion = 1;
    }

    public boolean updateFieldsAndSave() {
        this.updatedAt = new Date();
        this.needsUpload = true;

        return super.save();
    }
}
