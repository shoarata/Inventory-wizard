package com.example.android.inventorywizard;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.R.attr.dial;
import static android.R.attr.path;
import static android.R.attr.value;
import static com.example.android.inventorywizard.data.ItemContract.*;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    /**constant for pick image intent action **/
    private final static int PICKIMAGE = 1;
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
    /**sell button**/
    Button mSellButton;
    /** LOG TAG**/
    private final static String LOG_TAG = DetailsActivity.class.getSimpleName();
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
        mSellButton = (Button) findViewById(R.id.detail_sell);
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
        mImagePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
        //set up the sell button
        mSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSellDialog();
            }
        });
    }

    /** open sell dialog to indicate how many items you want to sell**/
    private void openSellDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How many?");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // decrease item quantity
                // get current quantity
                int currentQty = Integer.parseInt(mQuantity.getText().toString());
                //calculate new quatity
                int newQuantity = currentQty - Integer.parseInt(input.getText().toString());
                if(newQuantity>0){
                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_QUANTITY,newQuantity);
                    //update item quantity
                    int numUpdated = getContentResolver().update(mCurrentItemUri,values,null,null);
                    if(numUpdated>0){
                        (Toast.makeText(DetailsActivity.this,getString(R.string.succesful_sell),Toast.LENGTH_SHORT)).show();
                    }
                    else{
                        (Toast.makeText(DetailsActivity.this,getString(R.string.something_went_wrong),Toast.LENGTH_SHORT)).show();
                    }

                }
                else{
                    (Toast.makeText(DetailsActivity.this,getString(R.string.not_enough_items_alert),Toast.LENGTH_SHORT)).show();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    /**
     *  open image selector
     */
    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICKIMAGE);
    }
    /** when the user is done selecting an image, this will store the image URI in the data base for later usage **/
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case PICKIMAGE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    ContentValues value = new ContentValues();
                    value.put(ItemEntry.COLUMN_IMAGE_URI,selectedImage.toString());
                    getContentResolver().update(mCurrentItemUri,value,null,null);
                    mImage.setImageBitmap(getBitmapFromUri(selectedImage));
                }
                break;
        }
    }
    /** given a uri to an image, scale the image to the mImage width and height and return it as a bitmap**/
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
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
                ItemEntry.COLUMN_IMAGE_URI
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
            if (!cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE_URI)).isEmpty()) {
                final Uri imageUri = Uri.parse(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE_URI)));
                mImage.setVisibility(View.VISIBLE);
                // the get bitmapfromuri method requires that the layout of the detailsactivity is already drawn specifically the image view
                // ongloballayout listener will take care of calling getbitmapfrom uri when the layout is drawn
                ViewTreeObserver viewTreeObserver =  mImage.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mImage.setImageBitmap(getBitmapFromUri(imageUri));
                    }
                });
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
