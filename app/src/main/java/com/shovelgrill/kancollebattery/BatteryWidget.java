package com.shovelgrill.kancollebattery;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link BatteryWidgetConfigureActivity BatteryWidgetConfigureActivity}
 */
public class BatteryWidget extends AppWidgetProvider {

    public static final String ACTION_SETUP = "com.shovelgrill.kancollebattery.action.SETUP";
    public static final String EXTRA_WIDGET_ID = "com.shovelgrill.kancollebattery.extra.WIDGET_ID";

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = BatteryWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.battery_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        Intent intent = new Intent(context,BatteryWidget.class);
        intent.setAction(ACTION_SETUP);
        intent.putExtra(EXTRA_WIDGET_ID,appWidgetId);
        PendingIntent pi = PendingIntent.getBroadcast(context,appWidgetId,intent,0);
        views.setOnClickPendingIntent(R.id.appwidget_text,pi);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            BatteryWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_SETUP)) {
            Intent activity_intent = new Intent(context,BatteryWidgetConfigureActivity.class);
            activity_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity_intent.putExtras(intent);
            context.startActivity(activity_intent);
        }
    }
}

