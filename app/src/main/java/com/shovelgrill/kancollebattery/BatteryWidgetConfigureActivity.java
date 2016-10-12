package com.shovelgrill.kancollebattery;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shovelgrill.kancollebattery.db.ImageSet;
import com.shovelgrill.kancollebattery.db.ShipsDbHelper;
import com.squareup.picasso.Picasso;

import java.io.File;

public class BatteryWidgetConfigureActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "com.shovelgrill.kancollebattery.BatteryWidget";
    private static final String PREF_PREFIX_KEY = "wiki_id_";
    static final int SELECT_SHIP_REQUEST_CODE = 1;
    public static final String EXTRA_WIKI_ID = "com.shovelgrill.kancollebattery.extra.WIKI_ID";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    int temporary_wiki_id = ImageSet.ID_NOT_INITIALIZED;
    int downloadActivityScrollPos = DownloadActivity.INTENT_SCROLL_UNDEFINED;


    ImageView previewImageView1;
    ImageView previewImageView2;
    TextView selectedShipTextView;
    Context context;


    public BatteryWidgetConfigureActivity() {
        super();
    }

    static void saveWikiIdPref(Context context, int appWidgetId, int wiki_id) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId, wiki_id);
        prefs.apply();
    }

    static int loadWikiIdPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int wiki_id = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, ImageSet.ID_NOT_INITIALIZED);
        return wiki_id;
    }

    static void deleteWikiIdPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_SHIP_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int wiki_id = data.getIntExtra(EXTRA_WIKI_ID, ImageSet.ID_NOT_INITIALIZED);
                downloadActivityScrollPos = data.getIntExtra(DownloadActivity.EXTRA_SCROLL_POSITION, DownloadActivity.INTENT_SCROLL_UNDEFINED);
                if (wiki_id != ImageSet.ID_NOT_INITIALIZED) {
                    setWikiId(wiki_id);
                }
            }
        }
    }

    private void setWikiId(int wiki_id) {
        temporary_wiki_id = wiki_id;
        if (wiki_id == ImageSet.ID_NOT_INITIALIZED) {
            Picasso.with(this).load(R.drawable.compass_400).into(previewImageView1);
            Picasso.with(this).load(R.drawable.compass_400).into(previewImageView2);
            selectedShipTextView.setText(getString(R.string.please_select));

        } else {
            File SDDir = getExternalFilesDir(ImageSet.WAIFU_FOLDER);
            Picasso.with(this).load(new File(SDDir, ImageSet.getFileName(wiki_id)[0])).into(previewImageView1);
            Picasso.with(this).load(new File(SDDir, ImageSet.getFileName(wiki_id)[1])).into(previewImageView2);
            ShipsDbHelper shipsDbHelper = new ShipsDbHelper(context);
            ImageSet imageSet = shipsDbHelper.getImageSetByWikiId(wiki_id);
            selectedShipTextView.setText(imageSet.name);
            shipsDbHelper.close();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_configure, menu);
        final MenuItem applyItem = menu.findItem(R.id.action_apply);
        applyItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (temporary_wiki_id == ImageSet.ID_NOT_INITIALIZED) {
                    Toast.makeText(context, context.getString(R.string.ship_not_selected_yet), Toast.LENGTH_LONG).show();
                    return true;
                }

                saveWikiIdPref(context, mAppWidgetId, temporary_wiki_id);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                (new BatteryWidget()).updateAppWidget(context, appWidgetManager, mAppWidgetId);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
                return true;
            }
        });
        return true;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setResult(RESULT_CANCELED);
        context = this;


        View rootView = this.getLayoutInflater().inflate(R.layout.battery_widget_configure, null);
        setContentView(rootView);


        rootView.findViewById(R.id.select_ship_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = BatteryWidgetConfigureActivity.this;
                Intent intent = new Intent(context, DownloadActivity.class);
                intent.putExtra(DownloadActivity.EXTRA_SCROLL_POSITION, downloadActivityScrollPos);
                startActivityForResult(intent, SELECT_SHIP_REQUEST_CODE);
            }
        });

        previewImageView1 = (ImageView) rootView.findViewById(R.id.ship_preview_image1);
        previewImageView2 = (ImageView) rootView.findViewById(R.id.ship_preview_image2);
        selectedShipTextView = (TextView) rootView.findViewById(R.id.text_ship_name);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            mAppWidgetId = extras.getInt(BatteryWidget.EXTRA_WIDGET_ID, -1);
            if (mAppWidgetId == -1) {
                finish();
                return;
            }
        }

        int wiki_id = loadWikiIdPref(context, mAppWidgetId);
        if (wiki_id != ImageSet.ID_NOT_INITIALIZED) {
            setWikiId(wiki_id);
        }

    }
}

