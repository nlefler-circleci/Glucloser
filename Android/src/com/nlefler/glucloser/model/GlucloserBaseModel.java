package com.nlefler.glucloser.model;

import android.database.Cursor;

import com.nlefler.glucloser.util.database.DatabaseUtil;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

    @Override
    public void beforeSave() {
        this.updatedAt = new Date();
        this.needsUpload = true;
    }

    public static <T extends GlucloserBaseModel> T fromParseObject(Class<T> modelClass,
                                        ParseObject parseObject) {
        try {
            T object = modelClass.newInstance();
            Field[] allFields = modelClass.getDeclaredFields();
            for (Field field : allFields) {
                Column column = (Column)field.getAnnotation(Column.class);
                if (column != null) {
                    String fieldName = column.value();
                    try {
                        field.set(object, parseObject.get(fieldName));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
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
		try {
			ParseQuery query = new ParseQuery(tableName);
            object = query.get(parseId);
			success = populateParseObject(object);
		} catch (ParseException e) {
            object = new ParseObject(tableName);
			success = populateParseObject(object);
		}

        if (success) {
            return object;
        }
        return null;
    }

    private boolean populateParseObject(ParseObject object) {
        Field[] allFields = this.getClass().getDeclaredFields();
        for (Field field : allFields) {
            Column column = (Column)field.getAnnotation(Column.class);
            if (column != null) {
                String fieldName = column.value();
                try {
                    Object fieldValue = field.getType().newInstance();
                    field.get(fieldValue);
                    object.put(fieldName, fieldValue);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    return false;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }
}
