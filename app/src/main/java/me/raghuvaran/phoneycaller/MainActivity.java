package me.raghuvaran.phoneycaller;

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

    MyReceiver myReceiver;
    BroadcastReceiver batteryCharging, alert;
    private MediaPlayer mp;

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



        batteryCharging = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isCharging = intent.getBooleanExtra("isCharging", false);
                Toast.makeText(MainActivity.this, "Received isCharging "+String.valueOf(isCharging), Toast.LENGTH_SHORT).show();
                mainSwitch.setEnabled(isCharging);
                toggleCheckBoxes();
                if(!isCharging){
                    stopMonitoring();
                }
            }
        };
        registerReceiver(batteryCharging, new IntentFilter("me.raghuvaran.battery.isCharging"));



        alert = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int max = intent.getIntExtra("MaxReached",-1);
                Toast.makeText(MainActivity.this, "Received MaxReached" + String.valueOf(max), Toast.LENGTH_SHORT).show();
                if(max>0){
                    shout();
                }
            }
        };






        //Disable SeekBars by default
        toggleCheckBoxes();

        //start monitoring if enabled by default
        if(mainSwitch.isEnabled() && mainSwitch.isChecked()) startMonitoring();

        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleCheckBoxes();
                if(isChecked) startMonitoring();
                else stopMonitoring();

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
        upperLimit.setEnabled(mainSwitch.isChecked()); lowerLimit.setEnabled(mainSwitch.isChecked());
        toggleSeekBars();
    }


    void toggleSeekBars(){
        upperSeek.setEnabled(upperLimit.isEnabled() && upperLimit.isChecked());
        lowerSeek.setEnabled(lowerLimit.isEnabled() && lowerLimit.isChecked());
    }


    void startMonitoring(){
        int max, min = max = -1;
        if(upperLimit.isChecked()) max = upperSeek.getProgress();
        if(lowerLimit.isChecked()) min = lowerSeek.getProgress();
        Toast.makeText(MainActivity.this, "Started monitoring", Toast.LENGTH_SHORT).show();
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        myReceiver = new MyReceiver();
        myReceiver.max = max; myReceiver.min = min;
        registerReceiver(myReceiver, iFilter);
        registerReceiver(alert, new IntentFilter("me.raghuvaran.battery.alert"));
        Toast.makeText(MainActivity.this, "Registered receivers", Toast.LENGTH_SHORT).show();
    }

    void shout(){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();
        Toast.makeText(MainActivity.this, "Started shouting", Toast.LENGTH_SHORT).show();
    }

    void stopMonitoring(){
        unregisterReceiver(myReceiver);
        unregisterReceiver(alert);

        mp.stop();
        Toast.makeText(MainActivity.this, "Stopped monitoring", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(batteryCharging, new IntentFilter("me.raghuvaran.battery.isCharging"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(batteryCharging);
    }
}

