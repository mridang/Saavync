package com.mridang.saavync.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mridang.saavync.syncadapter.SyncAdapter;

/*
 * Service to handle Account sync. This is invoked with an intent with action
 * ACTION_AUTHENTICATOR_INTENT. It instantiates the syncadapter and returns its IBinder.
 */
public class SyncService extends Service {

    /* An object to keep hold of a synchronisation lock. */
    private static final Object sSyncAdapterLock = new Object();
    /* The instance of the synchronisation adapter. */
    private static SyncAdapter synAdapter = null;

    /*
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {

        synchronized (sSyncAdapterLock) {
            if (synAdapter == null) {
                synAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }

    }


    /*
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {

        return synAdapter.getSyncAdapterBinder();

    }

}