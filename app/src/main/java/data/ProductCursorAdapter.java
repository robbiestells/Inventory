package data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.R;

/**
 * Created by rsteller on 11/3/2016.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c){
        super(context, c, 0);
    }

    //make a new blank list item view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //binds data to list item
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.product_name);
        TextView tvQuantity = (TextView) view.findViewById(R.id.product_quantity);
        WebView wbImage = (WebView) view.findViewById(R.id.product_image);

        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int imageColumnIndext = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

        String name = cursor.getString(nameColumnIndex);
        int quantity = cursor.getInt(quantityColumnIndex);
        String image = cursor.getString(imageColumnIndext);

        tvName.setText(name);
        tvQuantity.setText(String.valueOf(quantity));

        WebSettings settings = wbImage.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        wbImage.loadUrl(image);
    }
}
