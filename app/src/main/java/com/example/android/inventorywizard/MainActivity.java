package com.example.android.inventorywizard;

import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventorywizard.data.ItemContract;
import com.example.android.inventorywizard.data.ItemContract.ItemEntry;
import com.example.android.inventorywizard.data.ItemDbHelper;

import static android.R.attr.id;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    /** list to hold product info**/
    private ListView mListView;
    /**cursor adapter for each item in the list **/
    private ItemCursorAdapter mItemAdapter;
    /** Id for the Item Loader**/
    private final static int LOADER_ID = 1;
    /** fab button for adding item **/
    private FloatingActionButton mAddItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find listview
        mListView = (ListView)findViewById(R.id.list_view);
        //find add item button
        mAddItem = (FloatingActionButton) findViewById(R.id.add_product_button);
        //initialize ItemCursorAdapter, that will be filled later by the loader
        mItemAdapter = new ItemCursorAdapter(this,null);
        //attach adapter to list view
        mListView.setAdapter(mItemAdapter);
        //set list view empty view
        mListView.setEmptyView(findViewById(R.id.empty_view));

        // start loader
        getLoaderManager().initLoader(LOADER_ID,null,this);


        // open details from each item
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,DetailsActivity.class);
                intent.setData(ContentUris.withAppendedId(ItemEntry.CONTENT_URI,id));
                startActivity(intent);
            }
        });
        //open add item
        mAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,DetailsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * create a new cursorLoader
     * @param i
     * @param bundle
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection={
                ItemEntry._ID,
            ItemEntry.COLUMN_NAME,
            ItemEntry.COLUMN_PRICE,
            ItemEntry.COLUMN_QUANTITY,
                ItemEntry.COLUMN_SUPPLIER_EMAIL,
                ItemEntry.COLUMN_SUPPLIER_NAME
        };

        return new CursorLoader(this,ItemEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mItemAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemAdapter.swapCursor(null);
    }
}
