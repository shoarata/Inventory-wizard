package com.example.android.inventorywizard.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.inventorywizard.data.ItemContract.*;

/**
 * Created by arata on 10/12/2016.
 */

public class ItemDbHelper extends SQLiteOpenHelper {
    //name for the database file
    private final static String DATA_BASE_NAME = "inventory.db";
    // constant to hold the version of the db
    private final static int DATA_BASE_VERSION = 1;
    //sql to create the item table
    private final static String CREATE_TABLE = "CREATE TABLE " + ItemEntry.TABLE_NAME +"(" + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ItemEntry.COLUMN_NAME + " TEXT NOT NULL, " + ItemEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, " +
            ItemEntry.COLUMN_QUANTITY+ " INTEGER NOT NULL DEFAULT 0, " + ItemEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, " +
            ItemEntry.COLUMN_IMAGE_URI + " TEXT NOT NULL DEFAULT ''," +
            ItemEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL);";
    private final static String DELETE_TABLE = "DELETE TABLE " + DATA_BASE_NAME;
    public ItemDbHelper(Context context){
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_TABLE);
        onCreate(db);
    }
}
