package com.example.android.inventorywizard.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by arata on 10/12/2016.
 */

public class ItemContract {

    //authority
    public final static String CONTENT_AUTHORITY = "com.example.android.items";
    //base  Uri object
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //path for items table
    public static final  String PATH_ITEMS = "items";

    /**
     * class for constants to manage Item data
     */
    public static class ItemEntry{
        //MIME TYPE CONSTANTS
        public final static String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"+ PATH_ITEMS;

        public final static  String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"+ PATH_ITEMS + "/#";
        //Content Uri for the whole table
        public static final  Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_ITEMS);
        //table name
        public final static String TABLE_NAME = "item";
        // column constants
        public final static String _ID = "_id";
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_QUANTITY= "quantity";
        public final static String COLUMN_SUPPLIER_NAME = "supplierName";
        public final static String COLUMN_SUPPLIER_EMAIL = "supplierEmail";
        public final static String COLUMN_IMAGE_URI = "imageUri";
    }
}
