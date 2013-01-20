package com.mridang.saavync.helpers;

import java.io.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

/*
 * This class contains a custom implementation of the media scanner
 * which will be used to add new songs to the library.
 */
public class MediaAdder implements MediaScannerConnectionClient {

	/* The location of the track that is to be scanned */
	private final String strTrack;
	/* The instance of the connection to the media scanner */
	private final MediaScannerConnection mscConnection;

	/*
	 * @see android.media.MediaScannerConnection.MediaScannerConnectionClient#onMediaScannerConnected()
	 */
	@Override
	public void onMediaScannerConnected() {
		mscConnection.scanFile(this.strTrack, null);
	}


	/*
	 * Constructor
	 */
	public MediaAdder(Context ctxContext, File filTrack, String strMime) {
		this.strTrack = filTrack.getAbsolutePath();
		mscConnection = new MediaScannerConnection(ctxContext, this);
		mscConnection.connect();
	}


	/*
	 * @see android.media.MediaScannerConnection.MediaScannerConnectionClient#onScanCompleted(java.lang.String, android.net.Uri)
	 */
	@Override
	public void onScanCompleted(String path, Uri uri) {
		mscConnection.disconnect();
	}

}