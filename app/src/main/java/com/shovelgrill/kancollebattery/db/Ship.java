package com.shovelgrill.kancollebattery.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

public class Ship {

    public static final int ID_NOT_INITIALIZED = -1;

    public int id = ID_NOT_INITIALIZED;
    public String url_name;
    public String name;

    public static abstract class Entry implements BaseColumns {
        public static final String TABLE_NAME = "waifus";
        public static final String COLUMN_URL_NAME = "url_name";
        public static final String COLUMN_NAME = "canon_name";
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (id!=ID_NOT_INITIALIZED) {
            values.put(Entry._ID,id);
        }
        values.put(Entry.COLUMN_URL_NAME,url_name);
        values.put(Entry.COLUMN_NAME,name);
        return values;
    }

    public Ship () {}

    public Ship (Cursor cursor) {
        this.id = cursor.getInt(cursor.getColumnIndex(Entry._ID));
        this.url_name = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_URL_NAME));
        this.name = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME));

    }

}
