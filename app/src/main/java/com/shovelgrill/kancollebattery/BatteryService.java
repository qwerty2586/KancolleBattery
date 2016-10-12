package com.shovelgrill.kancollebattery;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;

public class BatteryService extends Service {
    private static final String TAG = "BatteryService";
    private static BroadcastReceiver screenOffReceiver;
    private static BroadcastReceiver screenOnReceiver;
    private static BroadcastReceiver userPresentReceiver;
    private static BroadcastReceiver batteryChangeReceiver;


    private static int current_battery_state = -1001;
    private static boolean current_ac_state = true;
    private static int last_battery_state = -1000;
    private static boolean last_ac_state = false;
    private static boolean service_running = false;
    private static boolean user_present = true;
    private static boolean screen_on = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerScreenOffReceiver();
        registerScreenOnReceiver();
        registerUserPresentReceiver();
        registerBatteryChangeReceiver();
        service_running = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(screenOffReceiver);
        unregisterReceiver(screenOnReceiver);
        unregisterReceiver(userPresentReceiver);
        unregisterReceiver(batteryChangeReceiver);
        service_running = false;
    }


    private void registerScreenOffReceiver() {
        screenOffReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                user_present = false;
                screen_on = false;
            }

        };

        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    private void registerScreenOnReceiver() {
        screenOnReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                screen_on = true;
                KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                if (!keyguardManager.inKeyguardRestrictedInputMode())
                    user_present = true;
                checkAndUpdate();
            }

        };

        registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
    }

    private void registerUserPresentReceiver() {
        userPresentReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                user_present = true;
                checkAndUpdate();
            }

        };

        registerReceiver(userPresentReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
    }

    private void checkAndUpdate() {
        if (screen_on && user_present) {
            if (current_ac_state != last_ac_state || current_battery_state != last_battery_state) {
                Intent i = new Intent(BatteryWidget.ACTION_BATTERY_UPDATE);
                sendBroadcast(i);
                last_ac_state = current_ac_state;
                last_battery_state = current_battery_state;
            }
        }
    }


    private void registerBatteryChangeReceiver() {
        batteryChangeReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                current_ac_state = (status == BatteryManager.BATTERY_STATUS_CHARGING);
                current_battery_state = level * 100 / scale;
                checkAndUpdate();
            }

        };

        registerReceiver(batteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }


    public static int getBatteryState() {
        return current_battery_state;
    }



    public static boolean getAcState() {
        return current_ac_state;

    }


    public static boolean isServiceRunning() {
        return service_running;
    }

}
