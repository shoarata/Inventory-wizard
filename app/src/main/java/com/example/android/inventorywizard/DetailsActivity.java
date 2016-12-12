package com.example.android.inventorywizard;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

import static com.example.android.inventorywizard.data.ItemContract.*;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    /** uri for the current item**/
    Uri mCurrentItemUri;
    /** if any changes were made in the item form this will be true */
    private boolean mItemHasChanged = false;
    /**loader id**/
    private final static int LOADER_ID = 2;
    /**Edit text field for the name**/
    EditText mName;
    /**Edit text field for the price**/
    EditText mPrice;
    /**Edit text field for the quantity**/
    EditText mQuantity;
    /**Edit text field for the supplier's email**/
    EditText mEmail;
    /**Edit text field for the supplier's name**/
    EditText mSupplierName;
    /** image view for the producto photo **/
    ImageView mImage;
    /** imagepicker button**/
    Button mImagePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        // find fields in the item form
        mName = (EditText) findViewById(R.id.edit_text_name);
        mPrice = (EditText) findViewById(R.id.edit_text_price);
        mQuantity = (EditText) findViewById(R.id.edit_text_quantity);
        mEmail= (EditText) findViewById(R.id.edit_text_supplier_email);
        mSupplierName = (EditText) findViewById(R.id.edit_text_supplier_name);
        mImage = (ImageView)findViewById(R.id.detail_image_view);
        //find buttons
        mImagePicker = (Button) findViewById(R.id.image_picker_button);
        //get intent
        Intent intent = getIntent();
        //get Uri of selected item
        mCurrentItemUri = intent.getData();
        //set title according to the activity mode
        if(mCurrentItemUri == null){
            setTitle(getString(R.string.add_item_title));
            invalidateOptionsMenu();
        }
        else{
            setTitle(getString(R.string.edit_item_title));
            getLoaderManager().initLoader(LOADER_ID,null,this);
        }
        //setup imagePicker button
        mImagePicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    pickImage();
                }
                return false;
            }
        });

    }

    /**
     * set up image picker button to open image selector
     */
    private void pickImage(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    String path = locatePath(selectedImage);
                    ContentValues value = new ContentValues();
                    value.put(ItemEntry.COLUMN_IMAGE_PATH,path);
                    getContentResolver().update(mCurrentItemUri,value,null,null);
                }
                break;
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection= new String[]{
                ItemEntry._ID,
                ItemEntry.COLUMN_NAME,
                ItemEntry.COLUMN_PRICE,
                ItemEntry.COLUMN_QUANTITY,
                ItemEntry.COLUMN_SUPPLIER_EMAIL,
                ItemEntry.COLUMN_SUPPLIER_NAME,
                ItemEntry.COLUMN_IMAGE_PATH
        };
        return new CursorLoader(this,mCurrentItemUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //set input fields to current item values
        if(cursor.moveToFirst()) {
            mName.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_NAME)));
            mPrice.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_PRICE)));
            mQuantity.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY)));
            mEmail.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_EMAIL)));
            mSupplierName.setText(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_NAME)));
            if (!cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE_PATH)).isEmpty()) {

                File imgFile = new File(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE_PATH)));
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    mImage.setImageBitmap(myBitmap);
                }
                mImage.setVisibility(View.VISIBLE);
            }
        }
    }

    private String locatePath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
