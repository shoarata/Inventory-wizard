package com.example.android.inventorywizard.data;

import android.content.ClipData;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventorywizard.R;
import com.example.android.inventorywizard.data.ItemContract.ItemEntry;

/**
 * Created by arata on 10/12/2016.
 */

public class ItemProvider extends ContentProvider {
     /** Log Tag **/
     private static final String LOG_TAG =  ItemProvider.class.getSimpleName();
    /** database holder**/
    SQLiteDatabase db;
    /** Database helper*/
    ItemDbHelper mDbHelper;
    /**constant for when the uri is talking about all the items in the database*/
    private static final int ITEMS = 100;
    /**constant for when the uri is talking about one item in the database*/
    private static final int ITEM = 101;

    /**
     * uri matcher that will identify which action is the uri talking about
     */
    private final  static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        // set up the patterns for the urimatcher to recognize when the uri is talking about ITEMS
        uriMatcher.addURI(ItemContract.CONTENT_AUTHORITY,ItemContract.PATH_ITEMS,ITEMS);
        // set up the patterns for the urimatcher to recognize when the uri is talking about ITEM
        uriMatcher.addURI(ItemContract.CONTENT_AUTHORITY,ItemContract.PATH_ITEMS+"/#",ITEM);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projections, String selection, String[] selectionArgs, String s1) {
        // get database
        db = mDbHelper.getReadableDatabase();
        //cursor that will hold the query result
        Cursor cursor;

        int match = uriMatcher.match(uri);

        switch (match){
            case ITEMS:
                cursor = db.query(ItemEntry.TABLE_NAME,projections,selection,selectionArgs,null,null,s1);
                break;
            case ITEM:
                // make selection and selectionargs for just one item
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = db.query(ItemEntry.TABLE_NAME,projections,selection,selectionArgs,null,null,s1);
                break;
            default:
                throw new IllegalArgumentException("invalid Uri " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = uriMatcher.match(uri);
        switch (match){
            case ITEMS:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("not supported insertion " + uri);
        }
    }
    /**
     * Insert an item into the database with the given content values. Return the new content URI
     * for that row in the database.
     */
    private Uri insertItem(Uri uri,ContentValues values){
        //sanity check
        String name = values.getAsString(ItemEntry.COLUMN_NAME);
        if(name.isEmpty()){
            throw new IllegalArgumentException("item requires a name");
        }
        int price= values.getAsInteger(ItemEntry.COLUMN_PRICE);
        if(price < 0){
            throw new IllegalArgumentException("item requires a reasonable price");
        }
        int quantity = values.getAsInteger(ItemEntry.COLUMN_QUANTITY);
        if(quantity < 0){
            throw new IllegalArgumentException("item requires a reasonable quantity");
        }
        String supplierName = values.getAsString(ItemEntry.COLUMN_SUPPLIER_NAME);
        if(supplierName.isEmpty()){
            throw new IllegalArgumentException("item requires a valid name for supplier");
        }
        db = mDbHelper.getWritableDatabase();
        long id = db.insert(ItemEntry.TABLE_NAME,null,values);

        if(id <0){
            Log.e(LOG_TAG,getContext().getString(R.string.error_inserting_new_item));
            return null;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        db = mDbHelper.getWritableDatabase();
        int numRowsDeleted;
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                numRowsDeleted =  db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM:

                // Delete a single row given by the ID in the URI
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                numRowsDeleted =  db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if(numRowsDeleted > 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                getContext().getContentResolver().notifyChange(uri,null);
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM:
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //check if fields have reasonable values
        if(values.containsKey(ItemEntry.COLUMN_NAME) && values.getAsString(ItemEntry.COLUMN_NAME)== null){
            throw new IllegalArgumentException("Item requires name");
        }
        if(values.containsKey(ItemEntry.COLUMN_PRICE) && values.getAsInteger(ItemEntry.COLUMN_PRICE)== null){
            throw new IllegalArgumentException("Item requires price");
        }
        if(values.containsKey(ItemEntry.COLUMN_QUANTITY) && values.getAsInteger(ItemEntry.COLUMN_QUANTITY)== null){
            throw new IllegalArgumentException("Item requires quantity");
        }
        if(values.containsKey(ItemEntry.COLUMN_SUPPLIER_NAME) && values.getAsString(ItemEntry.COLUMN_SUPPLIER_NAME)== null){
            throw new IllegalArgumentException("Item requires supplier's name");
        }
        if(values.containsKey(ItemEntry.COLUMN_SUPPLIER_EMAIL) && values.getAsString(ItemEntry.COLUMN_SUPPLIER_EMAIL)== null){
            throw new IllegalArgumentException("Item requires supplier's email");
        }


        int numOfRowsUpdated = db.update(ItemEntry.TABLE_NAME,values,selection,selectionArgs);
        if(numOfRowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numOfRowsUpdated;
    }
}
