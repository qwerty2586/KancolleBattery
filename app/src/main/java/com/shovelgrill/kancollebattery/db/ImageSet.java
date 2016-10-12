package com.shovelgrill.kancollebattery.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

public class ImageSet {

    public static final int ID_NOT_INITIALIZED = -1;

    public int id = ID_NOT_INITIALIZED;
    public int wiki_id;
    public int ship_id;
    public String image_url;
    public String image_url_dmg;
    public boolean downloaded = false;
    public String name;
    public String ship_name = ""; //get from join ONLY LOCAL

    public static abstract class Entry implements BaseColumns {
        public static final String TABLE_NAME = "waifu_image_sets";
        public static final String COLUMN_WIKI_ID = "wiki_id";
        public static final String COLUMN_SHIP_ID = "ship_id";
        public static final String COLUMN_IMG_URL = "image_url";
        public static final String COLUMN_IMG_URL_DMG = "image_url_dmg";
        public static final String COLUMN_DOWNLOADED = "downloaded";
        public static final String COLUMN_NAME = "long_name";
        public static final String COLUMN_SHIP_NAME = "ship_name";

    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (id!=ID_NOT_INITIALIZED) {
            values.put(Entry._ID,id);
        }
        values.put(Entry.COLUMN_WIKI_ID,wiki_id);
        values.put(Entry.COLUMN_SHIP_ID,ship_id);
        values.put(Entry.COLUMN_IMG_URL,image_url);
        values.put(Entry.COLUMN_IMG_URL_DMG,image_url_dmg);
        values.put(Entry.COLUMN_DOWNLOADED,downloaded ? 1 : 0);
        values.put(Entry.COLUMN_NAME,name);
        return values;
    }

    public ImageSet() {}

    public ImageSet(Cursor cursor) {
        this.id = cursor.getInt(cursor.getColumnIndex(Entry._ID));
        this.wiki_id = cursor.getInt(cursor.getColumnIndex(Entry.COLUMN_WIKI_ID));
        this.ship_id = cursor.getInt(cursor.getColumnIndex(Entry.COLUMN_SHIP_ID));
        this.image_url = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_IMG_URL));
        this.image_url_dmg = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_IMG_URL_DMG));
        this.downloaded = cursor.getInt(cursor.getColumnIndex(Entry.COLUMN_DOWNLOADED))==1 ? true : false;
        this.name = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_NAME));
        this.ship_name = cursor.getString(cursor.getColumnIndex(Entry.COLUMN_SHIP_NAME));


    }


    public static final String WAIFU_FOLDER = "nice_waifu_pics";
    public static String[] getFileName(int wiki_id) {
        return new String[]{""+wiki_id+"_img.png",""+wiki_id+"_dmg.png"};
    }
}
