package me.raghuvaran.phoneycaller;

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
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        Intent intent1 = new Intent("me.raghuvaran.battery.isCharging");
        intent1.putExtra("isCharging", isCharging);

        Toast.makeText(context.getApplicationContext(), "isCharging: "+ String.valueOf(isCharging), Toast.LENGTH_SHORT).show();
        context.sendBroadcast(intent1);

    }
}