package com.shovelgrill.kancollebattery;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.shovelgrill.kancollebattery.db.ImageSet;
import com.shovelgrill.kancollebattery.db.ShipsDbHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DownloadActivity extends AppCompatActivity {
    private static final String TAG = "DownloadActivity";
    private String PROGRESS_MESSAGE;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 0;
    public static final int INTENT_SCROLL_UNDEFINED = -500;
    public static final String EXTRA_SCROLL_POSITION = "com.shovelgrill.kancollebattery.extra.SCROLL_POSITION";
    ProgressDialog progressDialog;
    ListView listView;
    ImageSetAdapter imageSetAdapter;
    Context context;


    List<ImageSet> imageSetList;
    private int progressShipCount = -1;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int broadcastType = intent.getIntExtra(DownloadService.EXTRA_BROADCAST_TYPE, DownloadService.DATA_UNDEFINED_INT);
            switch (broadcastType) {
                case DownloadService.BROADCAST_DOWNLOAD_ALL_FAILED: {
                    progressDialog.hide();
                    Toast.makeText(context, R.string.download_failed1, Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, R.string.download_failed2, Toast.LENGTH_SHORT).show();
                    phase3();
                }
                break;
                case DownloadService.BROADCAST_TYPE_SHIP_COUNT: {
                    progressShipCount = intent.getIntExtra(DownloadService.EXTRA_BROADCAST_DATA, -1);
                    progressDialog.setMessage(PROGRESS_MESSAGE + " 0/" + progressShipCount);
                }
                break;
                case DownloadService.BROADCAST_DOWNLOAD_ALL_SHIP_PROGRESS: {
                    int shipProgress = intent.getIntExtra(DownloadService.EXTRA_BROADCAST_DATA, -1);
                    progressDialog.setMessage(PROGRESS_MESSAGE + " " + shipProgress + "/" + progressShipCount);
                }
                break;
                case DownloadService.BROADCAST_DOWNLOAD_ALL_SUCCESS: {
                    progressDialog.hide();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    Toast.makeText(context, R.string.download_succesfull, Toast.LENGTH_SHORT).show();
                    phase3();
                }
                break;
                case DownloadService.BROADCAST_DOWNLOAD_SKIN_1_PROGRESS: {
                    int skin_id = intent.getIntExtra(DownloadService.EXTRA_BROADCAST_DATA, -1);
                    int skin_progress = intent.getIntExtra(DownloadService.EXTRA_BROADCAST_DATA2, -1);
                    imageSetAdapter.progresses[skin_id] = skin_progress / 2;
                    imageSetAdapter.notifyDataSetChanged();
                }
                break;
                case DownloadService.BROADCAST_DOWNLOAD_SKIN_2_PROGRESS: {
                    int skin_id = intent.getIntExtra(DownloadService.EXTRA_BROADCAST_DATA, -1);
                    int skin_progress = intent.getIntExtra(DownloadService.EXTRA_BROADCAST_DATA2, -1);
                    imageSetAdapter.progresses[skin_id] = 50 + skin_progress / 2;
                    imageSetAdapter.notifyDataSetChanged();

                }
                break;
                case DownloadService.BROADCAST_DOWNLOAD_SKIN_SUCCESS: {
                    int skin_id = intent.getIntExtra(DownloadService.EXTRA_BROADCAST_DATA, -1);
                    imageSetAdapter.progresses[skin_id] = ImageSetAdapter.IGNORE_PROGRESS;
                    imageSetList.get(skin_id).downloaded = true;
                    imageSetAdapter.notifyDataSetChanged();
                }
                break;
                case DownloadService.BROADCAST_DOWNLOAD_SKIN_FAILED: {
                    int skin_id = intent.getIntExtra(DownloadService.EXTRA_BROADCAST_DATA, -1);
                    imageSetAdapter.progresses[skin_id] = ImageSetAdapter.IGNORE_PROGRESS;
                    Toast.makeText(context, getString(R.string.download_image_failed1)+imageSetList.get(skin_id).name+getString(R.string.download_image_failed2), Toast.LENGTH_SHORT).show();
                    imageSetAdapter.notifyDataSetChanged();
                }
                break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PROGRESS_MESSAGE = this.getResources().getString(R.string.progress_message);
        this.setTitle(this.getResources().getString(R.string.title_activity_download2));

        super.onCreate(savedInstanceState);
        View rootView = this.getLayoutInflater().inflate(R.layout.activity_download, null);
        listView = (ListView) rootView.findViewById(R.id.listview_image_sets);
        setContentView(rootView);

        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressDialog = new ProgressDialog(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(DownloadService.INTENT_BROADCAST_EVENT));
        if (storagePermissionCheck()) {
            phase2(); // and phase 3
            checkIntentAndScroll();
        } else {
            requestPermission();
        }
    }

    private void checkIntentAndScroll() {
        Intent intent = getIntent();
        int scrollPos = intent.getIntExtra(EXTRA_SCROLL_POSITION,INTENT_SCROLL_UNDEFINED);
        if (scrollPos!=INTENT_SCROLL_UNDEFINED) {
            listView.setSelection(scrollPos);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem refreshItem = menu.findItem(R.id.action_download);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
               searchItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                onSearchChange(newText);
                return true;
            }
        });
        refreshItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(context)
                        .setTitle(getString(R.string.string_refresh_ships_question))
                        .setMessage(getString(R.string.string_refresh_ships_question2))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                refreshShipList();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(R.drawable.ic_error_black_24dp)
                        .show();

                return true;
            }
        });

        return true;
    }

    private void onSearchChange(String text) {
        text = text.toUpperCase();
        int start_pos = listView.getFirstVisiblePosition();
        int THRESHOLD = 30;


        // EASTER EGGS :D
        if (text.contains("ZEKAMASHI")) text="SHIMAKAZE";
        else if (text.contains("BURNING")) text="KONGOU";
        else if (text.contains("LADY")) text="AKATSUKI";
        else if (text.contains("NANODESU")||text.contains("HAWAWA")) text="INAZUMA";
        else if (text.contains("PANPAKAPAN")) {
            THRESHOLD = 0;
            if ((text.length() % 2) == 0) text="HIBIKI";
            else                          text="IKAZUCHI";
        }

        if (imageSetList.get(start_pos).name.toUpperCase().contains(text)) return;


        for (int i = 0;i<imageSetList.size();i++) {
            if (imageSetList.get(i).name.toUpperCase().contains(text)) {
                if (Math.abs(i - start_pos) < THRESHOLD) listView.smoothScrollToPosition(i);
                else listView.setSelection(i);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        new File(this.getExternalFilesDir(ImageSet.WAIFU_FOLDER), ".nomedia").createNewFile();
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot create .nomedia :(");
                    }
                    phase2();
                } else {
                    Toast.makeText(this, getString(R.string.toast_not_allowed_permission),Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    private void phase2() {
        ShipsDbHelper shipsDbHelper = new ShipsDbHelper(this);
        if (shipsDbHelper.getShips().size() == 0) refreshShipList();
        else phase3();
    }

    private void phase3() {
        ShipsDbHelper shipsDbHelper = new ShipsDbHelper(this);
        imageSetList = shipsDbHelper.getImageSets();
        shipsDbHelper.close();
        imageSetAdapter = new ImageSetAdapter(this, R.layout.image_set_item, imageSetList);

        listView.setAdapter(imageSetAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClicked(position);
            }
        });

    }

    private void itemClicked(int position) {
        if (imageSetList.get(position).downloaded) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(BatteryWidgetConfigureActivity.EXTRA_WIKI_ID,imageSetList.get(position).wiki_id);
            returnIntent.putExtra(EXTRA_SCROLL_POSITION,listView.getPositionForView(listView.getChildAt(0)));
            setResult(Activity.RESULT_OK,returnIntent);
            finish();                                                                                           //           <------------------------- END HERE!!!!
        } else {
            DownloadService.startActionDownloadShipSkin(this, position);
        }
    }

    private boolean storagePermissionCheck() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();

    }

    private void requestPermission() {
        final Activity a = this;
        new AlertDialog.Builder(context)
                .setTitle(getString(R.string.allow_permission))
                .setMessage(getString(R.string.allow_permission2))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(a,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_STORAGE);
                    }
                })
                .setIcon(R.drawable.ic_error_black_24dp)
                .setCancelable(false)
                .show();

    }


    private void refreshShipList() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(PROGRESS_MESSAGE);
        DownloadService.startActionRefreshAll(this);
    }


}
