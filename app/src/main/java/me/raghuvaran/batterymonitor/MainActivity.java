package me.raghuvaran.batterymonitor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Switch mainSwitch;
    CheckBox upperLimit;
    CheckBox lowerLimit;
    SeekBar upperSeek;
    SeekBar lowerSeek;
    TextView charge;
    Button exitBtn;

    String app_name = "Phoney Caller";

    ChargeReceiver chargeReceiver;
    BroadcastReceiver isConnectedReceiver, alertReceiver;
    private MediaPlayer mp;

    NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainSwitch = (Switch) findViewById(R.id.mSwitch);
        upperLimit = (CheckBox) findViewById(R.id.upperAlert);
        lowerLimit = (CheckBox) findViewById(R.id.lowerAlert);
        upperSeek = (SeekBar) findViewById(R.id.upperSeekBar);
        lowerSeek = (SeekBar) findViewById(R.id.lowerSeekBar);
        charge = (TextView) findViewById(R.id.remainingChargeValu);
        exitBtn = (Button) findViewById(R.id.exitButton);





        //Disable Monitor switch if device is not plugged; No sense if device is not plugged



        isConnectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isConnected = isConnected();
                Toast.makeText(MainActivity.this, "Received isConnected "+String.valueOf(isConnected), Toast.LENGTH_SHORT).show();
                mainSwitch.setEnabled(isConnected);
                toggleCheckBoxes();
                if(!isConnected){
                    stopMonitoring();
                }else{
                    mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    //
                    android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Battery Monitor")
                            .setContentText("Click here to open Battery Monitor to start charging");
                    Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            getApplicationContext(),
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    mBuilder.setContentIntent(pendingIntent);
                    mNotificationManager.notify(1, mBuilder.build());
                }
            }
        };
        registerReceiver(isConnectedReceiver, new IntentFilter("me.raghuvaran.battery.isConnected"));



        alertReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int max = intent.getIntExtra("MaxReached",-1);
                Toast.makeText(MainActivity.this, "Received MaxReached" + String.valueOf(max), Toast.LENGTH_SHORT).show();
                if(max>0){
                    shout();
                }
            }
        };


        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isEnabled() && isChecked) startMonitoring();
                else stopMonitoring();
                toggleCheckBoxes();
            }
        });

        upperLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleSeekBars();
            }
        });

        lowerLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleSeekBars();
            }
        });

        upperSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                charge.setText(String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }

    void toggleCheckBoxes(){
        upperLimit.setEnabled(!mainSwitch.isChecked()); lowerLimit.setEnabled(!mainSwitch.isChecked());
        toggleSeekBars();
    }


    void toggleSeekBars(){
        upperSeek.setEnabled(upperLimit.isEnabled() && upperLimit.isChecked());
        lowerSeek.setEnabled(lowerLimit.isEnabled() && lowerLimit.isChecked());
    }

    boolean isConnected(){
        Intent batteryStatus = this.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = 0;
        if (batteryStatus != null) {
            status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        }
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }


    void startMonitoring(){
        int max, min = max = -1;
        if(upperLimit.isChecked()) max = upperSeek.getProgress();
        if(lowerLimit.isChecked()) min = lowerSeek.getProgress();
        Toast.makeText(MainActivity.this, "Started monitoring", Toast.LENGTH_SHORT).show();
        if(max == -1 && min == -1) {
            stopMonitoring();
            return;
        }

        chargeReceiver = new ChargeReceiver();
        chargeReceiver.max = max; chargeReceiver.min = min;

        registerReceiver(chargeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(alertReceiver, new IntentFilter("me.raghuvaran.battery.alert"));

        Toast.makeText(MainActivity.this, "Registered receivers", Toast.LENGTH_SHORT).show();
    }

    void shout(){
        if(mp == null || !mp.isPlaying()){

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mp = MediaPlayer.create(getApplicationContext(), notification);
            mp.start();
            Toast.makeText(MainActivity.this, "Started shouting", Toast.LENGTH_SHORT).show();
            mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            //
            android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Battery Charged")
                    .setContentText("Battery charge has reached ");
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(pendingIntent);
            mNotificationManager.notify(2, mBuilder.build());

        }
    }

    void shutUp(){
        try{
            if(mp != null && mp.isPlaying()) mp.stop();
        }catch (Exception e){
            Log.e(app_name, "Error occurred while stopping media player");
        }
    }

    void stopMonitoring(){
        try {
            if (alertReceiver != null)
                unregisterReceiver(alertReceiver);
        }catch (IllegalArgumentException e){
            Log.i(app_name,"alertReceiver already unregistered");
        }
        try{
            if(chargeReceiver != null)
                unregisterReceiver(chargeReceiver);
        }catch (IllegalArgumentException e){
            Log.i(app_name,"chargeReceiver already unregistered");
        }
        shutUp();
        mainSwitch.setChecked(false);

        Toast.makeText(MainActivity.this, "Stopped monitoring", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
           boolean isConnected = isConnected();
            mainSwitch.setEnabled(isConnected);
            if(!isConnected) {
                stopMonitoring();
            }
            toggleCheckBoxes();

        //start monitoring if enabled by default
        if(mainSwitch.isEnabled() && !mainSwitch.isChecked()) {
            stopMonitoring();
        }else if(mainSwitch.isEnabled() && mainSwitch.isChecked()){
            startMonitoring();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (isConnectedReceiver != null)
                unregisterReceiver(isConnectedReceiver);
        }catch (IllegalArgumentException e){
            Log.i(app_name,"isConnectedReceiver already unregistered");
        }
        try {
            if (alertReceiver != null)
                unregisterReceiver(alertReceiver);
        }catch (IllegalArgumentException e){
            Log.i(app_name,"alertReceiver already unregistered");
        }
        try{
            if(chargeReceiver != null)
                unregisterReceiver(chargeReceiver);
        }catch (IllegalArgumentException e){
            Log.i(app_name,"chargeReceiver already unregistered");
        }
    }
}

