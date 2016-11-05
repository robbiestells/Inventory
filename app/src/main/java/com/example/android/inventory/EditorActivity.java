package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import data.ProductContract;

/**
 * Created by rsteller on 11/3/2016.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private boolean mProductHasChanged = false;

    private Uri mCurrentProductUri;

    private EditText mNameEditText;
    private EditText mSupplierEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mImageEditText;

    private int productQuantity = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_editor);

        //get intent from MainActivity
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //find all views
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mImageEditText = (EditText) findViewById(R.id.edit_product_image);
        Button saleButton = (Button) findViewById(R.id.sale_button);
        Button shipmentButton = (Button) findViewById(R.id.shipment_button);
        Button contactButton = (Button) findViewById(R.id.contact_button);

        //if new activity, set title to Add Product and don't show buttons, else load data
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.addProduct));
            invalidateOptionsMenu();
            saleButton.setVisibility(View.GONE);
            shipmentButton.setVisibility(View.GONE);
            contactButton.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.editProduct));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //create sale button to decrease inventory by one
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productQuantity > 0) {
                    productQuantity--;
                    mQuantityEditText.setText(String.valueOf(productQuantity));
                }
            }
        });

        //create shipment button to increase inventory by one
        shipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productQuantity++;
                mQuantityEditText.setText(String.valueOf(productQuantity));
            }
        });

        //create conact button to send email intent
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_SUBJECT, "New inventory Order");
                intent.putExtra(Intent.EXTRA_TEXT, "I need more inventory!!!!");
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //only show delete button if editing product
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //on selecting save, save the product
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                //on selecting delete, show delete dialog
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //if user tries to delete, show a dialog confirming choice
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //delete product if Uri is not null, show toast
    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_fail), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void saveProduct() {
        //make sure all fields are filled out
        if (TextUtils.isEmpty(mNameEditText.getText()) || TextUtils.isEmpty(mSupplierEditText.getText())
                || TextUtils.isEmpty(mQuantityEditText.getText()) || TextUtils.isEmpty(mPriceEditText.getText()) ||
                TextUtils.isEmpty(mImageEditText.getText())) {
            Toast.makeText(this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show();
            return;
        }

        //get all data from fields
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        Integer quantityInteger = Integer.parseInt(mQuantityEditText.getText().toString());
        Float priceInteger = Float.parseFloat(mPriceEditText.getText().toString());
        String imageString = mImageEditText.getText().toString();

        //put all values into ContentValues
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceInteger);

        //set default quantity to 0
        int quantity = 0;
        if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
            quantity = Integer.parseInt(mQuantityEditText.getText().toString());
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);

        //if new product, insert values to new row and show Toast, otherwise, update product row
        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.insert_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_added), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(
                    mCurrentProductUri,
                    values,
                    null,
                    null
            );

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_updated), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER
        };
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }
        updateViews(data);
    }

    private void updateViews(Cursor data) {
        //if the cursor is not null, load data into views
        if (data.moveToFirst()) {
            int nameColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
            int quantityColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);

            String name = data.getString(nameColumnIndext);
            String supplier = data.getString(supplierColumnIndext);
            Float price = data.getFloat(priceColumnIndext);
            int quantity = data.getInt(quantityColumnIndext);
            productQuantity = quantity;
            String image = data.getString(imageColumnIndext);

            mNameEditText.setText(name);
            mSupplierEditText.setText(supplier);
            mPriceEditText.setText(String.valueOf(price));
            mQuantityEditText.setText(String.valueOf(quantity));
            mImageEditText.setText(image);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //on reset, clear all fields
        mNameEditText.setText("");
        mSupplierEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mImageEditText.setText("");
    }
}
