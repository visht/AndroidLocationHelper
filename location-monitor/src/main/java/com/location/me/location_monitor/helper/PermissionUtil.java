package com.location.me.location_monitor.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.location.me.location_monitor.R;

public class PermissionUtil {

    private static final String TAG = PermissionUtil.class.getSimpleName();

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    public static final class Builder {
        private Activity context;
        static boolean mAlreadyStartedService = false;

        public Builder(Activity context) {
            this.context = context;
        }

        /**
         * Return the current state of the permissions needed.
         */
        private boolean checkPermissions() {
            int permissionState1 = ActivityCompat.checkSelfPermission(context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION);

            int permissionState2 = ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

        }

        /**
         * Start permissions requests.
         */
        private void requestPermissions() {

            boolean shouldProvideRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION);

            boolean shouldProvideRationale2 =
                    ActivityCompat.shouldShowRequestPermissionRationale(context,
                            Manifest.permission.ACCESS_COARSE_LOCATION);


            // Provide an additional rationale to the img_user. This would happen if the img_user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            if (shouldProvideRationale || shouldProvideRationale2) {
                Log.i(TAG, "Displaying permission rationale to provide additional context.");
                showSnackbar(R.string.permission_rationale,
                        android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Request permission
                                ActivityCompat.requestPermissions(context,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_PERMISSIONS_REQUEST_CODE);
                            }
                        });
            } else {
                Log.i(TAG, "Requesting permission");
                // Request permission. It's possible this can be auto answered if device policy
                // sets the permission in a given state or the img_user denied the permission
                // previously and checked "Never ask again".
                ActivityCompat.requestPermissions(context,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
            }
        }


        /**
         * Shows a {@link Snackbar}.
         *
         * @param mainTextStringId The id for the string resource for the Snackbar text.
         * @param actionStringId   The text of the action item.
         * @param listener         The listener associated with the Snackbar action.
         */
        private void showSnackbar(final int mainTextStringId, final int actionStringId,
                                  View.OnClickListener listener) {
            Snackbar.make(
                    context.findViewById(android.R.id.content),
                    context.getString(mainTextStringId),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(context.getString(actionStringId), listener).show();
        }



        /**
         * Step 1: Check Google Play services
         */
        public final Builder build() {

            //Check whether this user has installed Google play service which is being used by Location updates.
            if (isGooglePlayServicesAvailable()) {

                //Passing null to indicate that it is executing for the first time.
                stepTwo(null);

            } else {
                Toast.makeText(context.getApplicationContext(), R.string.no_google_playservice_available, Toast.LENGTH_LONG).show();
            }
            return this;
        }


        /**
         * Step 2: Check & Prompt Internet connection
         */
        private Boolean stepTwo(DialogInterface dialog) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

            if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
                promptInternetConnect();
                return false;
            }


            if (dialog != null) {
                dialog.dismiss();
            }

            //Yes there is active internet connection. Next check Location is granted by user or not.

            if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
                startStep3();
            } else {  //No user has not granted the permissions yet. Request now.
                requestPermissions();
            }
            return true;
        }


        /**
         * Return the availability of GooglePlayServices
         */
        boolean isGooglePlayServicesAvailable() {
            GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
            int status = googleApiAvailability.isGooglePlayServicesAvailable(context);
            if (status != ConnectionResult.SUCCESS) {
                if (googleApiAvailability.isUserResolvableError(status)) {
                    googleApiAvailability.getErrorDialog(context, status, 2404).show();
                }
                return false;
            }
            return true;
        }

        /**
         * Show A Dialog with button to refresh the internet state.
         */
        private void promptInternetConnect() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.title_alert_no_intenet);
            builder.setMessage(R.string.msg_alert_no_internet);

            String positiveText = context.getString(R.string.btn_label_refresh);
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            //Block the Application Execution until user grants the permissions
                            if (stepTwo(dialog)) {

                                //Now make sure about location permission.
                                if (checkPermissions()) {

                                    //Step 2: Start the Location Monitor Service
                                    //Everything is there to start the service.
//                                    startStep3();
                                } else if (!checkPermissions()) {
                                    requestPermissions();
                                }

                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }


        /**
         * Step 3: Start the Location Monitor Service
         */
        private void startStep3() {

            //And it will be keep running until you close the entire application from task manager.
            //This method will executed only once.

            if (!mAlreadyStartedService) {

                Log.d("started_ monitor", context.getString(R.string.msg_location_service_started));
                //Start location sharing service to app server.........


                mAlreadyStartedService = true;
                //Ends................................................
            }
        }

    }
}
