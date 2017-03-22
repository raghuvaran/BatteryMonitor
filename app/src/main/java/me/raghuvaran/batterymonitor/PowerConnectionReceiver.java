package me.raghuvaran.batterymonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.Toast;


/**
 * Created by rchowda on 3/19/2017.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intent1 = new Intent("me.raghuvaran.battery.isConnected");


//        Toast.makeText(context.getApplicationContext(), "isConnected: "+ String.valueOf(isConnected), Toast.LENGTH_SHORT).show();
        context.sendBroadcast(intent1);

    }
}