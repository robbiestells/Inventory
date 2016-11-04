package com.example.android.inventory;

import android.app.LoaderManager;
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
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.addProduct));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editProduct));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //find all views
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mImageEditText = (EditText) findViewById(R.id.edit_product_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
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
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                deleteProduct();
                return true;
            case android.R.id.home:
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null){
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if(rowsDeleted == 0){
                Toast.makeText(this, getString(R.string.editor_delete_fail), Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_success), Toast.LENGTH_SHORT);
            }
        }
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        Integer quantityInteger = Integer.parseInt(mQuantityEditText.getText().toString());
        Float priceInteger = Float.parseFloat(mPriceEditText.getText().toString());
        String imageString = mImageEditText.getText().toString();

        if (mCurrentProductUri == null && TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "Name Required", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceInteger);

        int quantity = 0;
        if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
            quantity = Integer.parseInt(mQuantityEditText.getText().toString());
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, "Product insert failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(
                    mCurrentProductUri,
                    values,
                    null,
                    null
            );

            if (rowsAffected == 0) {
                Toast.makeText(this, "Product Update failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product updated!", Toast.LENGTH_SHORT).show();
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
        if (data == null || data.getCount() < 1){
            return;
        }

        if (data.moveToFirst()){
            int nameColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
            int quantityColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndext = data.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);

            String name = data.getString(nameColumnIndext);
            String supplier = data.getString(supplierColumnIndext);
            int price = data.getInt(priceColumnIndext);
            int quantity = data.getInt(quantityColumnIndext);
            String image = data.getString(imageColumnIndext);

            mNameEditText.setText(name);
            mSupplierEditText.setText(supplier);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(quantity);
            mImageEditText.setText(image);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mSupplierEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mImageEditText.setText("");
    }
}
