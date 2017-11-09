package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;


import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.MainActivity;

import static com.udacity.stockhawk.R.*;

/**
 * Created by ddutra9 on 08/11/17.
 */

public class StockWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_stock_list);

            Intent i = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);
            views.setOnClickPendingIntent(R.id.main_activity, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
