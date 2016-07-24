package com.shovelgrill.kancollebattery.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qwerty on 21. 7. 2016.
 */
public class ShipsDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sunk_your_waifu.db";
    private static final String SQL_CREATE_SHIP_TABLE =
            "CREATE TABLE " + Ship.Entry.TABLE_NAME + " (" +
                    Ship.Entry._ID + " INTEGER PRIMARY KEY, " +
                    Ship.Entry.COLUMN_URL_NAME + " TEXT UNIQUE NOT NULL, " + // URL name is unique
                    Ship.Entry.COLUMN_NAME + " TEXT) ";
    private static final String SQL_CREATE_IMAGE_SET_TABLE =
            "CREATE TABLE " + ImageSet.Entry.TABLE_NAME + " (" +
                    ImageSet.Entry._ID + " INTEGER PRIMARY KEY, " +
                    ImageSet.Entry.COLUMN_WIKI_ID + " INTEGER UNIQUE, " +
                    ImageSet.Entry.COLUMN_SHIP_ID + " INTEGER REFERENCES " + Ship.Entry.TABLE_NAME + ", " +
                    ImageSet.Entry.COLUMN_IMG_URL + " TEXT, " +
                    ImageSet.Entry.COLUMN_IMG_URL_DMG + " TEXT, " +
                    ImageSet.Entry.COLUMN_DOWNLOADED + " INTEGER NOT NULL, " +
                    ImageSet.Entry.COLUMN_NAME + " TEXT) ";

    private static final String SQL_DROP_SHIP_TABLE =
            "DROP TABLE IF EXISTS "+Ship.Entry.TABLE_NAME;

    private static final String SQL_DROP_IMAGE_SET_TABLE =
            "DROP TABLE IF EXISTS "+ImageSet.Entry.TABLE_NAME;

    private static final String SQL_SELECT_IMAGE_SETS_WITH_SHIP_NAMES =
            "SELECT " +
                    "i." + ImageSet.Entry._ID + ", " +
                    "i." + ImageSet.Entry.COLUMN_WIKI_ID + ", " +
                    "i." + ImageSet.Entry.COLUMN_SHIP_ID + ", " +
                    "i." + ImageSet.Entry.COLUMN_IMG_URL + ", " +
                    "i." + ImageSet.Entry.COLUMN_IMG_URL_DMG + ", " +
                    "i." + ImageSet.Entry.COLUMN_DOWNLOADED + ", " +
                    "i." + ImageSet.Entry.COLUMN_NAME + ", " +
                    "s." + Ship.Entry.COLUMN_URL_NAME + " AS " + ImageSet.Entry.COLUMN_SHIP_NAME + " " +
                    "FROM " + ImageSet.Entry.TABLE_NAME + " i " +
                    "JOIN " + Ship.Entry.TABLE_NAME + " s " +
                    "ON i." + ImageSet.Entry.COLUMN_SHIP_ID + "=s." + Ship.Entry._ID;


    public ShipsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SHIP_TABLE);
        db.execSQL(SQL_CREATE_IMAGE_SET_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_SHIP_TABLE);
        db.execSQL(SQL_DROP_IMAGE_SET_TABLE);
    }

    public void addShip(Ship ship) {
        getWritableDatabase().insert(Ship.Entry.TABLE_NAME, null, ship.toContentValues());
    }

    public List<Ship> getShips() {
        ArrayList<Ship> ships = new ArrayList<>();
        Cursor cursor = getWritableDatabase().query(Ship.Entry.TABLE_NAME, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ships.add(new Ship(cursor));

            } while (cursor.moveToNext());

        }

        return ships;
    }

    public void deleteAllShips() {
        getWritableDatabase().execSQL("DELETE FROM " + Ship.Entry.TABLE_NAME);
    }

    public void addImageSet(ImageSet image_set) {
        getWritableDatabase().insert(ImageSet.Entry.TABLE_NAME, null, image_set.toContentValues());
    }

    public List<ImageSet> getImageSets() {
        ArrayList<ImageSet> imageSets = new ArrayList<>();
        //Cursor cursor = getWritableDatabase().query(ImageSet.Entry.TABLE_NAME, null, null, null, null, null, null);
        Cursor cursor = getWritableDatabase().rawQuery(SQL_SELECT_IMAGE_SETS_WITH_SHIP_NAMES, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                imageSets.add(new ImageSet(cursor));

            } while (cursor.moveToNext());

        }
        return imageSets;
    }

    public void deleteAllImageSets() {
        getWritableDatabase().execSQL("DELETE FROM " + ImageSet.Entry.TABLE_NAME);
    }
}
