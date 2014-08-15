package com.nlefler.glucloser.model;

import android.database.Cursor;
import android.util.Log;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final static String LOG_TAG = "Glucloser_Base_Model";

    @Key
    @Column(DatabaseUtil.GLUCLOSER_ID_COLUMN_NAME)
    public String glucloserId;

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
        this.parseId = "";
        this.createdAt = new Date();
        this.dataVersion = 1;
    }

    @Override
    public void beforeSave() {
        this.updatedAt = new Date();
        this.needsUpload = true;

        for (Field field : allFields()) {
            field.setAccessible(true);
            try {
                if (field.get(this) == null) {
                    Log.e(LOG_TAG + " " + this.getClass().getName(), "Null field " + field.getName());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static <T extends GlucloserBaseModel> T fromParseObject(Class<T> modelClass,
                                        ParseObject parseObject) {
        try {
            T object = modelClass.newInstance();
            Field[] allFields = modelClass.getDeclaredFields();
            for (Field field : object.allFields()) {
                Column column = (Column)field.getAnnotation(Column.class);
                String fieldName = column.value();
                try {
                    field.setAccessible(true);
                    Object fieldValue = parseObject.get(fieldName);
                    if (fieldName.equals(DatabaseUtil.CREATED_AT_COLUMN_NAME)) {
                        fieldValue = parseObject.getCreatedAt();
                    } else if (fieldName.equals(DatabaseUtil.UPDATED_AT_COLUMN_NAME)) {
                        fieldValue = parseObject.getUpdatedAt();
                    } else if (fieldName.equals(DatabaseUtil.PARSE_ID_COLUMN_NAME)) {
                        fieldValue = parseObject.getObjectId();
                    }
                    field.set(object, fieldValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return object;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ParseObject toParseObject() {
        ParseObject object;
        boolean success = false;
        String tableName = DatabaseUtil.tableNameForModel(this.getClass());
        if (this.parseId == null || this.parseId.isEmpty()) {
            object = new ParseObject(tableName);
        }
        else {
            try {
                ParseQuery query = new ParseQuery(tableName);
                object = query.get(parseId);
            } catch (ParseException e) {
                object = new ParseObject(tableName);
            }
        }
        success = populateParseObject(object);

        if (success) {
            return object;
        }
        return null;
    }

    private boolean populateParseObject(ParseObject object) {
        for (Field field : allFields()) {
            Column column = (Column)field.getAnnotation(Column.class);
            String fieldName = column.value();
            if (fieldName.equals(DatabaseUtil.CREATED_AT_COLUMN_NAME) ||
                    fieldName.equals(DatabaseUtil.UPDATED_AT_COLUMN_NAME) ||
                    fieldName.equals(DatabaseUtil.PARSE_ID_COLUMN_NAME)) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(this);
                if (fieldValue == null) {
                    Log.e(LOG_TAG + " " + this.getClass().getName(), "Null value for " + fieldName);
                }
                object.put(fieldName, fieldValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    protected  Collection<Field> allFields() {
        Map<String, Field> fields = new HashMap<String, Field>();
        for (Field field : this.getClass().getFields()) {
            Column column = (Column)field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            if (!fields.containsKey(field.getName())) {
                fields.put(field.getName(), field);
            }
        }
        for (Field field : this.getClass().getDeclaredFields()) {
            Column column = (Column)field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            if (!fields.containsKey(field.getName())) {
                fields.put(field.getName(), field);
            }
        }
        return fields.values();
    }
}
