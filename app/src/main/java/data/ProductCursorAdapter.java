package data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.MainActivity;
import com.example.android.inventory.R;

/**
 * Created by rsteller on 11/3/2016.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //make a new blank list item view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //binds data to list item
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final int itemId = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry._ID));

        //find name, quanity, price, and image views
        final TextView tvName = (TextView) view.findViewById(R.id.product_name);
        final TextView tvQuantity = (TextView) view.findViewById(R.id.product_quantity);
        final TextView tvPrice = (TextView) view.findViewById(R.id.product_price);
        WebView wbImage = (WebView) view.findViewById(R.id.product_image);

        //find columns in table
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int imageColumnIndext = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

        //get data from table
        String name = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        final float price = cursor.getFloat(priceColumnIndex);
        String priceString = "$" + String.valueOf(price);
        String image = cursor.getString(imageColumnIndext);

        //assign data to views
        tvName.setText(name);
        tvQuantity.setText(String.valueOf(quantity));
        tvPrice.setText(priceString);

        WebSettings settings = wbImage.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        wbImage.loadUrl(image);

        //create sale button that decreases quantity by one
        Button saleButton = (Button) view.findViewById(R.id.decrease_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();

                //decrease if quantity is not 0 and update database
                if (quantity > 0) {
                    int newQuantity = quantity - 1;
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                    Uri uri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, itemId);
                    context.getContentResolver().update(uri, values, null, null);
                }
                context.getContentResolver().notifyChange(ProductContract.ProductEntry.CONTENT_URI, null);
            }
        });
    }
}