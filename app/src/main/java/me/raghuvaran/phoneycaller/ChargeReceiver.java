package me.raghuvaran.phoneycaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.widget.Toast;

/**
 * Created by rchowda on 3/18/2017.
 */
public class ChargeReceiver extends BroadcastReceiver {

    int max, min;

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float charge = level*100.0f/scale;

        Toast.makeText(context.getApplicationContext(), "Battery Charge" + String.valueOf(charge), Toast.LENGTH_SHORT).show();

        if(charge > max){
            //shout
            Toast.makeText(context.getApplicationContext(), "Reached max:" + String.valueOf(max), Toast.LENGTH_SHORT).show();
            Intent intent1 = new Intent("me.raghuvaran.battery.alert");
            intent1.putExtra("MaxReached", max);
            context.sendBroadcast(intent1);

        }

    }
}
