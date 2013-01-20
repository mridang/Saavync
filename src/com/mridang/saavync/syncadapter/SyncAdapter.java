package com.mridang.saavync.syncadapter;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.mridang.saavync.client.SaavnClient;
import com.mridang.saavync.helpers.MediaAdder;
import com.mridang.saavync.helpers.SyncHelper;

/*
 * Class contains all the methods to synchronize the the starred tracks on the
 * server with the tracks stored locally. This only implements a one-way sync.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /* The tag used to log to adb console. */
    private static final String TAG = "SyncAdapter";
    /* The music directory used to store music */
    public static final File MUSIC_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

    /*
     * Constructor
     */
    public SyncAdapter(Context ctcContext, boolean booInitialize) {

        super(ctcContext, booInitialize);

    }


    /*
     * @{@inheritDoc}
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        try {

        	JSONArray lstTracks = SaavnClient.getStarred(this.getContext());
        	Log.d(TAG, lstTracks.toString(2));

        	Log.d(TAG, "Removing deleted files from library.");
        	SyncHelper.cleanMusic(MUSIC_DIR, lstTracks);
        	Log.d(TAG, "Cleaned");



        	Log.d(TAG, "Downloading starred tracks.");
            for (Integer intId = 0; intId < lstTracks.length(); intId++) {

                String strSong = lstTracks.getJSONObject(intId).getString("song");
                Log.d(TAG, String.format("Fetching track %s", strSong));

                String strId = lstTracks.getJSONObject(intId).getString("id");
                if (new File(MUSIC_DIR, String.format("%s.mp3", strId)).exists()) {

                	Log.d(TAG, "The track already exists. Skipping.");
                	new File(getContext().getCacheDir(), strId).createNewFile();
                    continue;

                } else {

                    String strAlbum = lstTracks.getJSONObject(intId).getString("album");
                	if (new File(getContext().getCacheDir(), strId).exists()) {

                		Log.d(TAG, "The track was deleted locally. Unstarring.");
                        SaavnClient.unstarTrack(getContext(), strId);
                        new File(getContext().getCacheDir(), strId).delete();

                	} else {

                	    String strTrack = lstTracks.getJSONObject(intId).getString("media_url");
                		if (!SyncHelper.getTrack(strId, strAlbum, strSong, strTrack, getContext())) {

                			Log.d(TAG, "Something went wrong downloading the track.");
                			new File(getContext().getCacheDir(), strId + ".mp3").delete();
                			continue;

                		} else {

                		    String strCover = lstTracks.getJSONObject(intId).getString("image");
                			if (SyncHelper.getCover(strId, strAlbum, strSong, strCover, getContext())) {

                			    Log.d(TAG, "Writing metadata for the track and adding to library.");
                			    SyncHelper.writeTags(getContext().getCacheDir(), MUSIC_DIR, lstTracks.getJSONObject(intId));
                                new MediaAdder(getContext(), new File(MUSIC_DIR, String.format("%s.mp3", strId)), null);

                			}

                		}

                    }

                }

            }
            Log.d(TAG, String.format("Downloaded successfully.", ""));



        	Log.d(TAG, "Removing deleted files from cache.");
	    	SyncHelper.cleanCache(getContext().getCacheDir(), lstTracks);
	    	Log.d(TAG, "Cleaned");

        } catch (JSONException e) {
            Log.e(TAG, "Error extracting field names from response.", e);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}