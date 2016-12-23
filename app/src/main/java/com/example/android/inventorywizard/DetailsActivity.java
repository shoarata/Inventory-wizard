package com.example.android.inventorywizard;

import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
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
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.R.attr.name;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.view.View.GONE;
import static com.example.android.inventorywizard.data.ItemContract.*;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    /**constant for pick image intent action **/
    private final static int PICKIMAGE = 1;
    /** uri for the current item**/
    Uri mCurrentItemUri;
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
    /** hold the selected image URI **/
    Uri mNewImageUri;
    /** imagepicker button**/
    Button mImagePicker;
    /**sell button**/
    Button mSellButton;
    /** receive shipment button **/
    Button mReceiveShipment;
    /** order button **/
    Button mOrderMore;
    /** Contents of the current item **/
    ContentValues mValues;
    /** tells whether a field of the item has changed **/
    private boolean mItemHasChanged = false;
    /** change listener to be attached to the item's field **/
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };
    /** LOG TAG**/
    private final static String LOG_TAG = DetailsActivity.class.getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        mValues = new ContentValues();
        // find fields in the item form
        mName = (EditText) findViewById(R.id.edit_text_name);
        mPrice = (EditText) findViewById(R.id.edit_text_price);
        mQuantity = (EditText) findViewById(R.id.edit_text_quantity);
        mEmail= (EditText) findViewById(R.id.edit_text_supplier_email);
        mSupplierName = (EditText) findViewById(R.id.edit_text_supplier_name);
        mImage = (ImageView)findViewById(R.id.detail_image_view);
        //set up change listener for the inputs
        mName.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mEmail.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        //preventing the user from changing the quantity manually
        mQuantity.setFocusable(false);
        //find buttons
        mImagePicker     = (Button) findViewById(R.id.image_picker_button);
        mSellButton      = (Button) findViewById(R.id.detail_sell);
        mReceiveShipment = (Button) findViewById(R.id.detail_receive_shipment);
        mOrderMore       = (Button) findViewById(R.id.detail_order_more);
        //get intent
        Intent intent = getIntent();
        //get Uri of selected item
        mCurrentItemUri = intent.getData();
        //set title according to the activity mode
        if(mCurrentItemUri == null){
            setTitle(getString(R.string.add_item_title));
            invalidateOptionsMenu();
            mSellButton.setVisibility(GONE);
            mReceiveShipment.setVisibility(GONE);
            mOrderMore.setVisibility(GONE);
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
        //set up receive shipment button
        mReceiveShipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReceiveShipmentDialog();
            }
        });
        //set up order more button
        mOrderMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeOrder();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(mCurrentItemUri == null){
            MenuItem delete = menu.findItem(R.id.action_delete);
            // we're adding new item thus we hide the delete option
            delete.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to save Item
            case R.id.action_save:
                saveItem();
                return true;
            // Respond to delete Item
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            //if up back button is pressed
            case android.R.id.home:
                //if item hasn't changed then go to parent normally
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }

                //otherwise show dialog warning the user that the changes made won't be saved
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };

                // show dialog
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /** save new Item **/
    //qty default to 0
    private void saveItem(){
        // if nothing was inserted then do nothing and go back
        if(allFieldsEmpty()){
            finish();
            return;
        }
        // create contentvalue var
        String name;
        if(mName.getText().toString().isEmpty()){
            Toast.makeText(DetailsActivity.this,getString(R.string.name_needed),Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            name = mName.getText().toString();
        }
        int price;
        if(mPrice.getText().toString().isEmpty()){
            price             = 0;
        }
        else {
            price             = Integer.parseInt(mPrice.getText().toString());
        }
        int quantity          = 0;
        String suppliersName;
        if(mSupplierName.getText().toString().isEmpty()) {
            suppliersName = getString(R.string.no_supplier);
        }
        else{
            suppliersName = mSupplierName.getText().toString();
        }

        String suppliersEmail;
        if(mEmail.getText().toString().isEmpty()){
            suppliersEmail = getString(R.string.no_email);
        }
        else{
            suppliersEmail = mEmail.getText().toString();
        }

        String imageUri;
        if(mNewImageUri == null){
            imageUri          = "";
        }
        else {
            imageUri = mNewImageUri.toString();
        }

        ContentValues values  = new ContentValues();
        values.put(ItemEntry.COLUMN_NAME,name);
        values.put(ItemEntry.COLUMN_PRICE,price);
        values.put(ItemEntry.COLUMN_SUPPLIER_NAME,suppliersName);
        values.put(ItemEntry.COLUMN_SUPPLIER_EMAIL,suppliersEmail);
        values.put(ItemEntry.COLUMN_IMAGE_URI,imageUri);
        //if current item uri null -> new Item
        if(mCurrentItemUri == null){
            values.put(ItemEntry.COLUMN_QUANTITY,quantity);
            Uri newItemUri  = getContentResolver().insert(ItemEntry.CONTENT_URI,values);
            if(newItemUri == null){
                Toast.makeText(DetailsActivity.this, getString(R.string.error_inserting_new_item),Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(DetailsActivity.this, getString(R.string.item_saved),Toast.LENGTH_SHORT).show();
            }
            finish();
        }
        //otherwise -> editing an existing item
        else{
            int numItemsUpdated= getContentResolver().update(mCurrentItemUri,values,null,null);
            if(numItemsUpdated > 0){
                (Toast.makeText(this,getString(R.string.item_succesfuly_edited),Toast.LENGTH_SHORT)).show();
            }
            else{
                (Toast.makeText(this,R.string.error_editing_item,Toast.LENGTH_SHORT)).show();
            }
            finish();
        }

    }
    /** tells if all the fields are empty **/
    private boolean allFieldsEmpty(){
        return mName.getText().toString().isEmpty() && mPrice.getText().toString().isEmpty() && mQuantity.getText().toString().isEmpty()&& mEmail.getText().toString().isEmpty()&& mSupplierName.getText().toString().isEmpty();
    }
    /** shows confirmation dialog before deleting **/
    private void showDeleteConfirmationDialog(){
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //delete item confirmed
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //cancel, do not delete
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if(mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        showUnsavedChangesDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
    }

    /**
     * show dialog to confirm that the changes will not be saved
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    /** delete current item **/
    private void deleteItem(){
        int numItemsDeleted = getContentResolver().delete(mCurrentItemUri,null,null);
        if(numItemsDeleted == 1){
            Toast.makeText(DetailsActivity.this,getString(R.string.successfuly_deleted),Toast.LENGTH_SHORT);
        }
        else{
            Toast.makeText(DetailsActivity.this,getString(R.string.something_went_wrong),Toast.LENGTH_SHORT);
        }
        finish();
    }

    /** call intent to email the provider for more items **/
    private void placeOrder(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.how_many));

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // get order quantity
                int orderQty = Integer.parseInt(input.getText().toString());
                // get name of provider
                String providerName = mValues.getAsString(ItemEntry.COLUMN_SUPPLIER_NAME);
                // get provider email
                String providerEmail = mValues.getAsString(ItemEntry.COLUMN_SUPPLIER_EMAIL);
                // create body
                String bodyText = getString(R.string.body_pre) + " " + orderQty + " " + getString(R.string.body_post);

                //start intent
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"+providerEmail));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT,bodyText);
                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(DetailsActivity.this,getString(R.string.something_went_wrong),Toast.LENGTH_SHORT);
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
    /** open sell dialog to indicate how many items you want to sell**/
    private void openSellDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.how_many));

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
                int currentQty = mValues.getAsInteger(ItemEntry.COLUMN_QUANTITY);
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
    /** open receive shipment dialog to indicate how many items you are receiving **/
    private void openReceiveShipmentDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.how_many));

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // increment item quantity
                // get current quantity
                int currentQty = mValues.getAsInteger(ItemEntry.COLUMN_QUANTITY);
                //calculate new quatity
                int newQuantity = currentQty + Integer.parseInt(input.getText().toString());
                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_QUANTITY,newQuantity);
                //update item quantity
                int numUpdated = getContentResolver().update(mCurrentItemUri,values,null,null);
                if(numUpdated>0){
                    (Toast.makeText(DetailsActivity.this,getString(R.string.items_received),Toast.LENGTH_SHORT)).show();
                }
                else{
                    (Toast.makeText(DetailsActivity.this,getString(R.string.something_went_wrong),Toast.LENGTH_SHORT)).show();
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
    /** when the user is done selecting an image, this will store the image URI in member variable for later usage **/
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case PICKIMAGE:
                if(resultCode == RESULT_OK){
                    mNewImageUri = imageReturnedIntent.getData();
                    mImage.setVisibility(View.VISIBLE);
                    // the get bitmapfromuri method requires that the layout of the detailsactivity is already drawn specifically the image view
                    // ongloballayout listener will take care of calling getbitmapfrom uri when the layout is drawn
                    ViewTreeObserver viewTreeObserver =  mImage.getViewTreeObserver();
                    viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            mImage.setImageBitmap(getBitmapFromUri(mNewImageUri));
                        }
                    });
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
            String imageUri = cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE_URI));
            if(imageUri.isEmpty()){
                mNewImageUri = null;
            }
            else {
                mNewImageUri = Uri.parse(imageUri);
            }
            //copy values to mValues for later usage
            mValues.put(ItemEntry.COLUMN_QUANTITY,cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_QUANTITY)));
            mValues.put(ItemEntry.COLUMN_SUPPLIER_NAME,cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_NAME)));
            mValues.put(ItemEntry.COLUMN_SUPPLIER_EMAIL,cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_SUPPLIER_EMAIL)));

            if (!cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_IMAGE_URI)).isEmpty()) {
                mImage.setVisibility(View.VISIBLE);
                // the get bitmapfromuri method requires that the layout of the detailsactivity is already drawn specifically the image view
                // ongloballayout listener will take care of calling getbitmapfrom uri when the layout is drawn
                ViewTreeObserver viewTreeObserver =  mImage.getViewTreeObserver();
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mImage.setImageBitmap(getBitmapFromUri(mNewImageUri));
                    }
                });
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
