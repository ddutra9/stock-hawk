package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by donato on 13/11/17.
 */

public class StockRemoteView extends RemoteViewsService {

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data = null;
            private DecimalFormat dollarFormat;
            private DecimalFormat dollarFormatWithPlus;

            @Override
            public void onCreate() {
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(Contract.Quote.URI, Contract.Quote.QUOTE_COLUMNS,
                        null, null, Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);

                views.setTextViewText(R.id.symbol, data.getString(Contract.Quote.POSITION_SYMBOL));
                views.setTextViewText(R.id.price, dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                if (rawAbsoluteChange > 0) {
                    views.setInt(R.id.change, "setBackgroundColor",
                            ContextCompat.getColor(getBaseContext(), R.color.material_green_700));
                } else {
                    views.setInt(R.id.change, "setBackgroundColor",
                            ContextCompat.getColor(getBaseContext(), R.color.material_red_700));
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                views.setTextViewText(R.id.change, change);

                final Intent fillInIntent = new Intent();
                final Bundle extras = new Bundle();
                extras.putString(MainActivity.STOCK_SYMBOL, data.getString(Contract.Quote.POSITION_SYMBOL));
                fillInIntent.putExtras(extras);
                views.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getBaseContext().getPackageName(), R.layout.list_item_quote);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
