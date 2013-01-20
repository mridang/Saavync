package com.mridang.saavync.syncadapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.loopj.android.http.PersistentCookieStore;
import com.mridang.saavync.R;

/*
 * This class contains some helper functions which help with downloading a track, downloading a
 * cover or cleaning a directory.
 */
public class SyncHelper {

    /* The tag used to log to adb console. */
    private static final String TAG = "SyncHelper";

    /*
     * This methods downloads a cover from the Saavn website and saves it to the local cache
     * directory
     *
     * @param  strId       The unique identifier of the song
     * @param  strAlbum    The name of the album
     * @param  strSong     The name of the song
     * @param  strPath     The path to the album track to download
     * @param  ctxContext  The context of the calling activity
     */
	public static Boolean getTrack(String strId, String strAlbum, String strSong,
			String strPath, Context ctxContext) {

	    if (strId == null || strAlbum == null || strSong == null || strPath == null)
	        return false;

	    File filCache = ctxContext.getCacheDir();

        Log.d(TAG, String.format(
                "Downloading the track from %s and saving to %s.",
                strPath, filCache.toString()));

        try {

            //Show the notification that we are downloading the song
            NotificationManager nfmManager = (NotificationManager) ctxContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder notNotification = new Notification.Builder(ctxContext);
            notNotification.setSmallIcon(R.drawable.icn_downloading_music);
            notNotification.setContentTitle(ctxContext.getString(
                    R.string.downloading_notification_title));
            notNotification.setContentText(String.format(
                    ctxContext.getString(R.string.downloading_notification_message),
                    strAlbum, strSong));
            notNotification.setOngoing(true);
            notNotification.setOnlyAlertOnce(true);
            notNotification.setProgress(100, 1, true);
            Integer intNotification = new Random().nextInt();
            nfmManager.notify(intNotification, notNotification.getNotification());

            //Create a connection and set all the connection parameters
            HttpParams htpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(htpParameters, 30000);

            //Attache the persistent cookie storage to the connection handler we created.
            HttpGet httpGet = new HttpGet(strPath);
            DefaultHttpClient dhcClient = new DefaultHttpClient(htpParameters);
            PersistentCookieStore pscStore = new PersistentCookieStore(ctxContext);
            dhcClient.setCookieStore(pscStore);

            try {

                //Save the binary data to the filesystem
                HttpResponse resResponse = dhcClient.execute(httpGet);
                HttpEntity entEntity = resResponse.getEntity();
                File filTrack = new File(filCache, String.format("%s.mp3", strId));
                FileOutputStream fosOutput = new FileOutputStream(filTrack);

                try {

                    if (entEntity != null)
                        entEntity.writeTo(fosOutput);

                } finally {

                    if (fosOutput != null)
                        fosOutput.close();

                }

            } catch (Exception e) {

                Log.e(TAG, String.format("Error downloading track %s - %s",
                        strAlbum, strSong), e);
                throw e;

            } finally {

                nfmManager.cancel(intNotification);
                dhcClient.getConnectionManager().shutdown();

            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }


	/*
	 * This method cleans all the files in the directory that are not in the list of starrted tracks
	 * anymore.
     * This method to clean the cache directory and the method to the clean the music directory are
     * pretty much the same except that order in which the files are compared.
	 *
	 * @param  filDirectry  The directory that should be cleaned
	 * @param  lstTracks    The list of starred tracks that should be excluded
	 */
	public static void cleanMusic(File filDirectory, JSONArray lstTracks) {

        for (File filFile : filDirectory.listFiles()) {

            Boolean booDelete = true;
            for (Integer intId = 0; intId < lstTracks.length(); intId++) {

                try {

                    if (filFile.getName().startsWith(lstTracks.getJSONObject(intId).getString("id"))) {
                        booDelete = false;
                        break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            if (booDelete)
                filFile.delete();

        }

	}

	/*
	 * This method writes the information available in the JSON object into the ID3 tags of th
	 * music file.
	 */
	public static void writeTags(File filCache, File filMusic, JSONObject jsnTrack) throws JSONException {

        try {

            MusicMetadata mumTags = new MusicMetadata("name");

            if (jsnTrack.has("album"))
                mumTags.setAlbum(jsnTrack.getString("album"));
            if (jsnTrack.has("song"))
                mumTags.setSongTitle(jsnTrack.getString("song"));
            if (jsnTrack.has("singers"))
                mumTags.setArtist(jsnTrack.getString("singers"));

            File filCover = new File(filCache, String.format("%s.jpg", jsnTrack.getString("id")));
            RandomAccessFile rafCover = new RandomAccessFile(filCover, "r");
            byte[] bytCovers = null;

            try {

                bytCovers = new byte[(int) rafCover.length()];
                rafCover.read(bytCovers);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                rafCover.close();
            }


            Vector<ImageData> fileList = new Vector<ImageData>();

            if (jsnTrack.has("music"))
                mumTags.setComposer(jsnTrack.getString("music"));
            if (jsnTrack.has("year"))
                mumTags.setYear(jsnTrack.getString("year"));
            if (bytCovers != null && fileList.add(new ImageData(bytCovers, "", "", 3)))
                mumTags.setPictureList(fileList);

            File filTrack = new File(filCache, String.format("%s.mp3", jsnTrack.getString("id")));
            File filTracknew = new File(filMusic, String.format("%s.mp3", jsnTrack.getString("id")));
            MusicMetadataSet mmsTags = null;

            try {

                mmsTags = new MyID3().read(filTrack);
                new MyID3().write(filTrack, filTracknew, mmsTags, mumTags);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }

	}


    /*
     * This method cleans all the files in the directory that are not in the list of starrted tracks
     * anymore.
     * This method to clean the cache directory and the method to the clean the music directory are
     * pretty much the same except that order in which the files are compared.
     *
     * @param  filDirectry  The directory that should be cleaned
     * @param  lstTracks    The list of starred tracks that should be excluded
     */
    public static void cleanCache(File filDirectory, JSONArray lstTracks) {

        for (File filFile : filDirectory.listFiles()) {

            Boolean booDelete = true;
            for (Integer intId = 0; intId < lstTracks.length(); intId++) {

                try {

                    if (lstTracks.getJSONObject(intId).getString("id").startsWith(filFile.getName())) {
                        booDelete = false;
                        break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            if (booDelete)
                filFile.delete();

        }

    }


    /*
     * This methods downloads a cover from the Saavn website and saves it to the local cache
     * directory.
     *
     * @param  strId       The unique identifier of the song
     * @param  strAlbum    The name of the album
     * @param  strSong     The name of the song
     * @param  strPath     The path to the album cover to download
     * @param  ctxContext  The context of the calling activity
     */
	public static Boolean getCover(String strId, String strAlbum, String strSong,
			String strPath, Context ctxContext) {
		
		if (strPath.isEmpty())
			strPath = "http://www.saavn.com/_i/default-album-big.png";

        File filCache = ctxContext.getCacheDir();

        Log.d(TAG, String.format(
				"Downloading the cover from %s and saving to %s.",
				strPath, filCache.toString()));

		try {

        	//Show the notification that we have downloaded the song
            NotificationManager nfmManager = (NotificationManager) ctxContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder notNotification = new Notification.Builder(ctxContext);
			notNotification.setSmallIcon(R.drawable.icn_action_music);
            notNotification.setContentText(ctxContext.getString(
                    R.string.downloaded_notification_message));
            notNotification.setContentTitle(String.format(
            		ctxContext.getString(R.string.downloaded_notification_title),
                    strAlbum, strSong));
			notNotification.setOngoing(false);
			notNotification.setOnlyAlertOnce(true);
        	Integer intNotification = new Random().nextInt();

            //Create a connection and set all the connection parameters
            HttpParams htpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(htpParameters, 30000);

            //Attache the persistent cookie storage to the connection handler we created.
			HttpGet httpGet = new HttpGet(strPath);
			DefaultHttpClient dhcClient = new DefaultHttpClient(htpParameters);
			PersistentCookieStore pscStore = new PersistentCookieStore(ctxContext);
			dhcClient.setCookieStore(pscStore);

			try {

				//Save the binary data to the filesystem
	            HttpResponse resResponse = dhcClient.execute(httpGet);
	            HttpEntity entEntity = resResponse.getEntity();
            	File filCover = new File(filCache, String.format("%s.jpg", strId));
            	FileOutputStream fosOutput = new FileOutputStream(filCover);

                try {

		            if (entEntity != null)
		            	entEntity.writeTo(fosOutput);

                } finally {

                    if (fosOutput != null)
                    	fosOutput.close();

                }

				notNotification.setLargeIcon(BitmapFactory.decodeFile(filCover.toString()));

			} catch (Exception e) {

				Log.e(TAG, String.format("Error downloading cover %s - %s",
						strAlbum, strSong), e);
				throw e;

			} finally {

				nfmManager.notify(intNotification, notNotification.getNotification());
				dhcClient.getConnectionManager().shutdown();

			}

		} catch (Exception e) {
		    e.printStackTrace();
			return false;
		}

        return true;

    }

}