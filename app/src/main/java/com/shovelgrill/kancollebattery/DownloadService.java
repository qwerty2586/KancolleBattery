package com.shovelgrill.kancollebattery;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.shovelgrill.kancollebattery.db.ImageSet;
import com.shovelgrill.kancollebattery.db.Ship;
import com.shovelgrill.kancollebattery.db.ShipsDbHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DownloadService extends IntentService {

    private static final String TAG = "DownlaodService";
    private static final String ACTION_REFRESH_ALL = "com.shovelgrill.kancollebattery.action.REFRESH_ALL";
    private static final String ACTION_DOWNLOAD_SHIP_SKIN = "com.shovelgrill.kancollebattery.action.DOWNLOAD_SHIP_SKIN";
    private static final String EXTRA_SKIN_ID = "com.shovelgrill.kancollebattery.extra.SKIN_ID";

    public static final String INTENT_BROADCAST_EVENT = "com.shovelgrill.kancollebattery.INTENT_BROADCAST_EVENT";
    public static final String EXTRA_BROADCAST_TYPE = "com.shovelgrill.kancollebattery.extra.BROADCAST_TYPE";
    public static final int BROADCAST_TYPE_SHIP_COUNT = 0;
    public static final int BROADCAST_DOWNLOAD_ALL_FAILED = 1;
    public static final int BROADCAST_DOWNLOAD_ALL_SHIP_PROGRESS = 2;
    public static final int BROADCAST_DOWNLOAD_ALL_SUCCESS = 3;
    public static final int BROADCAST_DOWNLOAD_SKIN_FAILED = 4;
    public static final int BROADCAST_DOWNLOAD_SKIN_1_PROGRESS = 5;
    public static final int BROADCAST_DOWNLOAD_SKIN_2_PROGRESS = 6;
    public static final int BROADCAST_DOWNLOAD_SKIN_1_SUCCESS = 7;
    public static final int BROADCAST_DOWNLOAD_SKIN_2_SUCCESS = 8;
    public static final int BROADCAST_DOWNLOAD_SKIN_SUCCESS = 9;


    public static final String EXTRA_BROADCAST_DATA = "com.shovelgrill.kancollebattery.extra.BROADCAST_DATA";
    public static final String EXTRA_BROADCAST_DATA2 = "com.shovelgrill.kancollebattery.extra.BROADCAST_DATA2";

    public static final int DATA_UNDEFINED_INT = -111;


    private static final String WIKIA_URL = "http://kancolle.wikia.com";


    public DownloadService() {
        super("DownloadService");
    }

    public static void startActionRefreshAll(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_REFRESH_ALL);
        context.startService(intent);
    }

    public static void startActionDownloadShipSkin(Context context, int ship_skin_id) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD_SHIP_SKIN);
        intent.putExtra(EXTRA_SKIN_ID, ship_skin_id);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REFRESH_ALL.equals(action)) {
                handleActionRefreshAll();
            } else if (ACTION_DOWNLOAD_SHIP_SKIN.equals(action)) {
                final int ship_skin_id = intent.getIntExtra(EXTRA_SKIN_ID,0);
                handleActionDownloadShipSkin(ship_skin_id);
            }
        }
    }

    private void handleActionRefreshAll() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        ShipsDbHelper shipsDbHelper = new ShipsDbHelper(this);

        Document doc;
        Element table;
        Elements links;
        Element elem;

        try {
            doc = Jsoup.connect(WIKIA_URL+"/wiki/Ship").get();
            // first wikitable on page
            table = doc.select(".wikitable").get(0);
            links = table.select("td a");
            broadcastOut(localBroadcastManager,BROADCAST_TYPE_SHIP_COUNT,links.size());
            //now we have internet connection so we can drop old database
            shipsDbHelper.clearAll();
            File SDDir = this.getExternalFilesDir(ImageSet.WAIFU_FOLDER);

            // for skipping same links
            List<Integer> unique_skin_id = new ArrayList<>();
            List<String> unique_ship = new ArrayList<>();


            for (int i=0;i<links.size();i++) {
                broadcastOut(localBroadcastManager,BROADCAST_DOWNLOAD_ALL_SHIP_PROGRESS,i);
                elem = links.get(i);
                // left side of wiki table
                if (elem.attr("href").contains("List") || elem.attr("href").contains("Auxiliary")) continue;
                String[] shipNameElements = elem.attr("href").split("/");
                String shipUrlName = shipNameElements[shipNameElements.length - 1];

                if (unique_ship.contains(shipUrlName)) continue;
                unique_ship.add(shipUrlName);

                Ship ship = new Ship();
                ship.url_name = shipUrlName;

                // unescape HTML entities
                ship.name = Jsoup.parse(ship.url_name).text().replace("_"," ").trim();


                int ship_db_id = (int)shipsDbHelper.addShip(ship);

                String url = WIKIA_URL + elem.attr("href") + "/Gallery";
                Document gallery = Jsoup.connect(url).get();
                Elements pics = gallery.select("a").select(".image.image-thumbnail").select("img");
                for (int j = pics.size() - 1; j >= 0; j--) {
                    if (pics.get(j).attr("src").charAt(0) != 'h') pics.remove(j);
                }

                List<String[]> alts   = new ArrayList<>();
                List<String> pic_urls =  new ArrayList<>();
                for (int j = 0; j < pics.size(); j++) {
                    alts.add(pics.get(j).attr("alt").trim().split(" "));
                    pic_urls.add(pics.get(j).attr("src").trim());
                }
                for (int j = 0; j < alts.size(); j++) {
                    if (alts.get(j)[alts.get(j).length-1].toUpperCase().equals("DAMAGED")) continue;
                    if (!isInteger(alts.get(j)[alts.get(j).length-2])) continue;
                    int skin_id = Integer.valueOf(alts.get(j)[alts.get(j).length-2]);
                    if (unique_skin_id.contains(Integer.valueOf(skin_id))) continue;
                    unique_skin_id.add(Integer.valueOf(skin_id));

                    for (int k = 0; k < alts.size(); k++) {
                        if (j==k) continue;
                        boolean exp = alts.get(k)[alts.get(k).length-1].toUpperCase().equals("DAMAGED");
                        if (!isInteger(alts.get(k)[alts.get(k).length-3])) continue;
                        if (exp) {
                            for (int l = 0;l<alts.get(j).length-1;l++) {
                                if (!(alts.get(j)[l].equals(alts.get(k)[l]))) {
                                    exp=false; break;
                                }
                            }
                        }

                        if (exp) {
                            if (Integer.valueOf(alts.get(k)[alts.get(k).length - 3]) == skin_id) {
                                ImageSet imageSet = new ImageSet();
                                imageSet.ship_id = ship_db_id;
                                imageSet.image_url = pic_urls.get(j);
                                imageSet.image_url_dmg = pic_urls.get(k);
                                imageSet.name = "";
                                for (int l = 1; l < alts.get(j).length - 2; l++)
                                    imageSet.name += " " + alts.get(j)[l];
                                imageSet.name = imageSet.name.trim();
                                imageSet.wiki_id = skin_id;
                                imageSet.downloaded = false;
                                if ((new File(SDDir,ImageSet.getFileName(imageSet.wiki_id)[0]).exists())&&(new File(SDDir,ImageSet.getFileName(imageSet.wiki_id)[0]).exists())) imageSet.downloaded=true;


                                shipsDbHelper.addImageSet(imageSet);
                                k = Integer.MAX_VALUE - 1; // dont need find next
                            }
                        }
                    }

                }




            }
            Thread.sleep(2000);
            broadcastOut(localBroadcastManager,BROADCAST_DOWNLOAD_ALL_SUCCESS);


        } catch (Exception ex) {
            broadcastOut(localBroadcastManager,BROADCAST_DOWNLOAD_ALL_FAILED);
        }

        shipsDbHelper.close();
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }

    private void broadcastOut(LocalBroadcastManager localBroadcastManager, int broadcastType, int broadcastData, int broadcastData2) {
        Intent intent = new Intent(INTENT_BROADCAST_EVENT);
        intent.putExtra(EXTRA_BROADCAST_TYPE,broadcastType);
        if (broadcastData!=DATA_UNDEFINED_INT) {
            intent.putExtra(EXTRA_BROADCAST_DATA,broadcastData);
        }
        if (broadcastData2!=DATA_UNDEFINED_INT) {
            intent.putExtra(EXTRA_BROADCAST_DATA2,broadcastData2);
        }
        localBroadcastManager.sendBroadcast(intent);
    }

    private void broadcastOut(LocalBroadcastManager localBroadcastManager, int broadcastType, int broadcastData) {
        broadcastOut(localBroadcastManager,broadcastType,broadcastData,DATA_UNDEFINED_INT);
    }

    private void broadcastOut(LocalBroadcastManager localBroadcastManager, int broadcastType) {
        broadcastOut(localBroadcastManager,broadcastType,DATA_UNDEFINED_INT);
    }

    private void handleActionDownloadShipSkin(int ship_skin_id) {
        ShipsDbHelper shipsDbHelper = new ShipsDbHelper(this);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        List<ImageSet> imageSets = shipsDbHelper.getImageSets(); // todle by chtelo udelat lip
        ImageSet imageSet = imageSets.get(ship_skin_id);
        try {
            DefaultHttpClient httpclient =  new DefaultHttpClient(){};
            HttpGet httpget = new HttpGet (imageSet.image_url);
            HttpResponse response = httpclient.execute (httpget);
            HttpEntity entity = response.getEntity();
            int size = Integer.valueOf(response.getHeaders("Content-Length")[0].getValue());

            File SDDir = this.getExternalFilesDir(ImageSet.WAIFU_FOLDER);
            File file = new File(SDDir,ImageSet.getFileName(imageSet.wiki_id)[0]); // id_img.png
            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = entity.getContent();
            int downloadedSize = 0;
            int lastProgress = -1;
            byte[] buffer = new byte[2048];
            int bufferLen = 0;
            while ((bufferLen = inputStream.read(buffer))>0) {
                fileOutput.write(buffer,0,bufferLen);
                downloadedSize += bufferLen;
                int progress = (int)(((double)downloadedSize * 100.0) / (double) size);
                if (progress>=lastProgress+10) {
                    broadcastOut(localBroadcastManager, BROADCAST_DOWNLOAD_SKIN_1_PROGRESS, ship_skin_id,progress);
                    lastProgress=progress;
                }
            }
            inputStream.close();
            fileOutput.close();
            broadcastOut(localBroadcastManager, BROADCAST_DOWNLOAD_SKIN_1_SUCCESS, ship_skin_id);


            httpget = new HttpGet (imageSet.image_url_dmg);
            response = httpclient.execute (httpget);
            entity = response.getEntity();
            size = Integer.valueOf(response.getHeaders("Content-Length")[0].getValue());

            file = new File(SDDir,ImageSet.getFileName(imageSet.wiki_id)[1]); // id_dmg.png
            fileOutput = new FileOutputStream(file);
            inputStream = entity.getContent();
            downloadedSize = 0;
            lastProgress = -1;
            bufferLen = 0;
            while ((bufferLen = inputStream.read(buffer))>0) {
                fileOutput.write(buffer,0,bufferLen);
                downloadedSize += bufferLen;
                int progress = (int)(((double)downloadedSize * 100.0) / (double) size);
                if (progress>=lastProgress+10) {
                    broadcastOut(localBroadcastManager, BROADCAST_DOWNLOAD_SKIN_2_PROGRESS, ship_skin_id,progress);
                    lastProgress=progress;
                }
            }
            inputStream.close();
            fileOutput.close();

            broadcastOut(localBroadcastManager, BROADCAST_DOWNLOAD_SKIN_2_SUCCESS, ship_skin_id);

            shipsDbHelper.setImageSetDownloaded(imageSet.wiki_id,true);
            broadcastOut(localBroadcastManager, BROADCAST_DOWNLOAD_SKIN_SUCCESS, ship_skin_id);


        } catch (IOException e) {
            e.printStackTrace();
            broadcastOut(localBroadcastManager,BROADCAST_DOWNLOAD_SKIN_FAILED,ship_skin_id);
            // delete files
            File SDDir = this.getExternalFilesDir(ImageSet.WAIFU_FOLDER);
            File file = new File(SDDir,ImageSet.getFileName(imageSet.wiki_id)[0]);
            if (file.exists()) file.delete();
            file = new File(SDDir,ImageSet.getFileName(imageSet.wiki_id)[1]);
            if (file.exists()) file.delete();

        }

        shipsDbHelper.close();
    }
}
