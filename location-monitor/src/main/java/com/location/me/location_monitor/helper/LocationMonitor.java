package com.location.me.location_monitor.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.location.me.location_monitor.R;
import com.location.me.location_monitor.conts.Constants;
import com.google.android.gms.location.LocationListener;

public class LocationMonitor {

    private static final String TAG = LocationMonitor.class.getSimpleName();
    private static volatile LocationMonitor locationMonitor = new LocationMonitor();


    public static final String ACTION_LOCATION_BROADCAST = LocationMonitor.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    private LocationMonitor() {
    }


    public static LocationMonitor getLocationMonitor() {
        synchronized (locationMonitor) {
            if (locationMonitor != null)
                return locationMonitor;
            else
                return new LocationMonitor();
        }
    }

    public static final class Builder {

        private Activity context;
        //        private int location_interval, fastest_location_interval;
        volatile GoogleApiClient mLocationClient;
        LocationRequest mLocationRequest = new LocationRequest();

        public Builder(Activity context) {
            this.context = context;
        }

        public final Builder initiateLocationInterval(int location_interval, int fastest_location_interval) {
            mLocationRequest.setInterval(location_interval);
            mLocationRequest.setFastestInterval(fastest_location_interval);
            return this;
        }

        public final Builder buildGoogleClient() {
            mLocationClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                        @Override
                        public void onConnected(@Nullable Bundle bundle) {

                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.

                                Log.d(TAG, "== Error On onConnected() Permission not granted");
                                //Permission not granted by user so cancel the further execution.

                                return;
                            }
                            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {

                                    Log.d(TAG, "Location changed");
//                                    Log.d("Location", context.getString(R.string.msg_location_service_started) + "\n Latitude : " + location.getLatitude() + "\n Longitude: " +
//                                            location.getLongitude());

                                    if (location != null) {
                                        Log.d(TAG, "== location != null");

                                        //Send result to activities
                                        sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                                    }
                                }
                            });

                            Log.d(TAG, "Connected to Google API");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                            Log.d(TAG, "Connection suspended");
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {


                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                            Log.d(TAG, "Failed to connect to Google API");
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
            return this;
        }

        Handler handler;
        Runnable r = null;

        /**
         * This is normal build that will destroy when app goes to background state or kill-state
         *
         * @return builder instance
         */
        public final Builder build() {

            int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
            //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes

            mLocationRequest.setPriority(priority);
            mLocationClient.connect();
            if (r != null) {
                handler.removeCallbacks(r);
                r = null;
                handler = null;
            }
            return this;
        }

        /**
         * This is build when your app killed or this builder process denied to execute.
         *
         * @return builder instance
         */
        public final Builder buildOnKill() {
            handler = new Handler();
            r = new Runnable() {
                @Override
                public void run() {
                    build();
                    handler.postDelayed(this, 1000);
                }
            };

            handler.postDelayed(r, 2000);

            return this;
        }

        private void sendMessageToUI(String lat, String lng) {

            Log.d(TAG, "Sending info to your App UI...");

            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(EXTRA_LATITUDE, lat);
            intent.putExtra(EXTRA_LONGITUDE, lng);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
