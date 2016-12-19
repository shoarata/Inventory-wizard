package com.example.android.inventorywizard;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventorywizard.data.ItemContract.ItemEntry;

import static android.R.attr.id;

/**
 * Created by arata on 10/12/2016.
 */

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * inflates a new list item view to be used by the bindview method
     * @param context
     * @param cursor
     * @param viewGroup
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
       return LayoutInflater.from(context).inflate(R.layout.list_item,viewGroup,false);
    }

    /**
     * given a list item view populate it with the correct information extracted from the given cursor
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // find each part of the given list item
        TextView name    = (TextView) view.findViewById(R.id.list_item_name);
        TextView price   = (TextView) view.findViewById(R.id.list_item_price);
        TextView quantity = (TextView) view.findViewById(R.id.list_item_quantity);
        Button   sell    = (Button)   view.findViewById(R.id.list_item_sell_button);

        //set name
        name.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_NAME)));
        price.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_PRICE)) + " $");
        quantity.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY)) + " Left");

        // set up button action
        final int newQuatity = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY)) - 1;
        // getting all the values from the cursor to update the item quantity
        final int id =cursor.getInt(cursor.getColumnIndex(ItemEntry._ID));
        String nameupdate = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_NAME));
        int priceupdate = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_PRICE));
        String supplierEmail = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_EMAIL));
        String supplierName = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_NAME));
        final ContentValues values = new ContentValues();
        //put into Contentvalues variable
        values.put(ItemEntry.COLUMN_NAME,nameupdate);
        values.put(ItemEntry.COLUMN_PRICE,priceupdate);
        values.put(ItemEntry.COLUMN_QUANTITY,newQuatity);
        values.put(ItemEntry.COLUMN_SUPPLIER_EMAIL,supplierEmail);
        values.put(ItemEntry.COLUMN_SUPPLIER_NAME,supplierName);
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update current item entry with the new quantity
                if(newQuatity >=0) {
                    context.getContentResolver().update(ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id), values, null, null);
                }
            }
        });
    }
}
