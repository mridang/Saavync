package com.mridang.saavync.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.mridang.saavync.authenticator.Authenticator;

/**
 * Service to handle Account authentication. It instantiates the authenticator
 * and returns its IBinder.
 */
public class AuthenticationService extends Service {

    /* The tag used to log to adb console. */
    private static final String TAG = "AuthenticationService";
    /* An instance of an authenticator. */
    private Authenticator autAuthenticator;

    /*
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Saavn authentication service started.");
        }
        autAuthenticator = new Authenticator(this);

    }


    /*
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "getBinder()...  returning the binder for intent " + intent);
        }
        return autAuthenticator.getIBinder();

    }


    /*
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Saavn authentication service stopped.");
        }

    }

}