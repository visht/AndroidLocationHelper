package com.chat.me.demolocationsupport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.location.me.location_monitor.helper.LocationMonitor;
import com.location.me.location_monitor.helper.PermissionUtil;

public class LocationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location2);

        new PermissionUtil.Builder(this).build();

        new LocationMonitor.Builder(this)
                .buildGoogleClient()
                .initiateLocationInterval(1000, 1000)
                .build();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String latitude = intent.getStringExtra(LocationMonitor.EXTRA_LATITUDE);
                        String longitude = intent.getStringExtra(LocationMonitor.EXTRA_LONGITUDE);

                        if (latitude != null && longitude != null) {
                            Log.i("Your lat and long ", getString(R.string.msg_location_service_started) + "\n Latitude : " + latitude + "\n Longitude: " + longitude);
                        }
                    }
                }, new IntentFilter(LocationMonitor.ACTION_LOCATION_BROADCAST)
        );
    }
}
