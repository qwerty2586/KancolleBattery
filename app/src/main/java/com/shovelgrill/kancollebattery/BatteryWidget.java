package com.shovelgrill.kancollebattery;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.shovelgrill.kancollebattery.db.ImageSet;

public class BatteryWidget extends AppWidgetProvider {

    private static final String TAG = "BatteryWidget";
    public static final String ACTION_BATTERY_UPDATE = "com.shovelgrill.kancollebattery.action.UPDATE";

    public static final String ACTION_SETUP = "com.shovelgrill.kancollebattery.action.SETUP";
    public static final String EXTRA_WIDGET_ID = "com.shovelgrill.kancollebattery.extra.WIDGET_ID";

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        int wiki_id = BatteryWidgetConfigureActivity.loadWikiIdPref(context,appWidgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.battery_widget);
        Intent intent = new Intent(context,BatteryWidget.class);
        intent.setAction(ACTION_SETUP);
        intent.putExtra(EXTRA_WIDGET_ID,appWidgetId);
        PendingIntent pi = PendingIntent.getBroadcast(context,appWidgetId,intent,0);
        views.setOnClickPendingIntent(R.id.shipImageView,pi);

        int batteryLevel = BatteryService.getBatteryState();
        boolean ac = BatteryService.getAcState();

        String battery_text = ""+batteryLevel+"%";
        if (ac) battery_text += "+";
        views.setTextViewText(R.id.batteryText, battery_text);
        views.setViewVisibility(R.id.batteryProgressBar1, View.INVISIBLE);
        views.setViewVisibility(R.id.batteryProgressBar2, View.INVISIBLE);
        views.setViewVisibility(R.id.batteryProgressBar3, View.INVISIBLE);
        views.setViewVisibility(R.id.batteryProgressBar4, View.INVISIBLE);
        views.setViewVisibility(R.id.batteryProgressBar5, View.INVISIBLE);
        views.setViewVisibility(R.id.batteryProgressBar6, View.INVISIBLE);

        boolean use_non_dmg=true;

        if (ac) {
            if (batteryLevel > 29) {
                use_non_dmg=true;
            } else {
                use_non_dmg=false;
            }
            views.setImageViewResource(R.id.stampImageView, R.drawable.dmg6);
            views.setViewVisibility(R.id.batteryProgressBar6, View.VISIBLE);
            views.setProgressBar(R.id.batteryProgressBar6, 100, batteryLevel, false);

        } else if (batteryLevel > 49) {
            use_non_dmg=true;
            views.setImageViewResource(R.id.stampImageView, R.drawable.dmg1);
            views.setViewVisibility(R.id.batteryProgressBar1, View.VISIBLE);
            views.setProgressBar(R.id.batteryProgressBar1, 100, batteryLevel, false);

        } else if (batteryLevel < 50 && batteryLevel > 29) {
            use_non_dmg=false;
            views.setImageViewResource(R.id.stampImageView, R.drawable.dmg2);
            views.setViewVisibility(R.id.batteryProgressBar2, View.VISIBLE);
            views.setProgressBar(R.id.batteryProgressBar2, 100, batteryLevel, false);
        } else if (batteryLevel < 30 && batteryLevel > 19) {
            use_non_dmg=false;
            views.setImageViewResource(R.id.stampImageView, R.drawable.dmg3);
            views.setViewVisibility(R.id.batteryProgressBar3, View.VISIBLE);
            views.setProgressBar(R.id.batteryProgressBar3, 100, batteryLevel, false);
        } else if (batteryLevel < 20 && batteryLevel > 10) {
            use_non_dmg=false;
            views.setImageViewResource(R.id.stampImageView, R.drawable.dmg4);
            views.setViewVisibility(R.id.batteryProgressBar4, View.VISIBLE);
            views.setProgressBar(R.id.batteryProgressBar4, 100, batteryLevel, false);
        } else { // 0-10
            use_non_dmg=false;
            views.setImageViewResource(R.id.stampImageView, R.drawable.dmg5);
            views.setViewVisibility(R.id.batteryProgressBar5, View.VISIBLE);
            views.setProgressBar(R.id.batteryProgressBar5, 100, batteryLevel, false);
        }

        String img_path = context.getExternalFilesDir(ImageSet.WAIFU_FOLDER).toString() + "/";

        if (use_non_dmg) {
            img_path += ImageSet.getFileName(wiki_id)[0];

        } else {
            img_path += ImageSet.getFileName(wiki_id)[1];
        }
        Bitmap bitmap = BitmapFactory.decodeFile(img_path);
        views.setImageViewBitmap(R.id.shipImageView,bitmap);


        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        checkServiceAndRun(context);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            BatteryWidgetConfigureActivity.deleteWikiIdPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        checkServiceAndRun(context);
    }

    private void checkServiceAndRun(Context context) {
        if (!BatteryService.isServiceRunning()) {
            context.startService(new Intent(context, BatteryService.class));
        }

    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
        checkServiceAndRun(context);
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        checkServiceAndRun(context);
        updateAppWidget(context,appWidgetManager,appWidgetId);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_BATTERY_UPDATE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int widgetIDs[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, BatteryWidget.class));
            for (int id : widgetIDs) {
                updateAppWidget(context, appWidgetManager, id);
            }
        }

        if (intent.getAction().equals(ACTION_SETUP)) {
            Intent activity_intent = new Intent(context,BatteryWidgetConfigureActivity.class);
            activity_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity_intent.putExtras(intent);
            context.startActivity(activity_intent);
        }
    }


}

