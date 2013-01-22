package com.mridang.saavync.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.PersistentCookieStore;
import com.mridang.saavync.exceptions.ConnectivityException;
import com.mridang.saavync.interceptors.GzippedInterceptor;

/*
 * This class contains all the methods for authenticating with the server and
 * also fetching the starred songs on Saavn.
 */
public class SaavnClient {

    /* The tag used to log to adb console. */
    private static final String TAG = "SaavnClient";
    /* The location of the page for logging in */
    public static final String API_URL = "https://www.saavn.com/api.php";

    /*
     * Connects to the Saavn servers and authenticates with it using the provided
     * username and password and fetches the authentication token.
     *
     * @param  ctxContext             The context of the calling activity for getting a cookie-store instance
     * @param  strUsername            The server account username for logging in to Saavn
     * @param  strPassword            The server account password for logging in to Saavn
     * @return                        The authentication token returned by the server (or null)
     * @throws ConnectivityException  When there was a temporary connectivity problem
     */
	public static String authenticate(Context ctxContext, String strUsername,
			String strPassword) throws ConnectivityException {

        Log.d(TAG, "Logging in");

        try {

            for (Cookie cooCookie:  new PersistentCookieStore(ctxContext).getCookies()) {
                if (cooCookie.isExpired(new Date())) {
                    Log.d(TAG, String.format("Removing expired cookie: %s", cooCookie.getName()));
                    continue;
                }
            }

            //Create a connection and set all the connection parameters
            HttpParams htpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(htpParameters, 10000);

            //Prepare the data that will be posted to Saavn for authentication
            List<NameValuePair> lstCredentials = new ArrayList<NameValuePair>();
            lstCredentials.add(new BasicNameValuePair("username", strUsername));
            lstCredentials.add(new BasicNameValuePair("password", strPassword));
            lstCredentials.add(new BasicNameValuePair("_marker", "0"));
            lstCredentials.add(new BasicNameValuePair("__call", "user.login"));

            //Attache the persistent cookie storage to the connection handler we created.
            HttpPost htpPost = new HttpPost(API_URL);
            htpPost.setEntity(new UrlEncodedFormEntity(lstCredentials));
            htpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:18.0) Gecko/20100101 Firefox/18.0");
            htpPost.setHeader("X-Requested-With", "XMLHttpRequest");
            htpPost.setHeader("Referer", "http://www.saavn.com/s/");
            htpPost.addHeader("Accept-Encoding", "gzip");
            DefaultHttpClient dhcClient = new DefaultHttpClient(htpParameters);
            dhcClient.addResponseInterceptor(new GzippedInterceptor(), 0);
            PersistentCookieStore pscStore = new PersistentCookieStore(ctxContext);
            dhcClient.setCookieStore(pscStore);

            //Make the request and get the reponse which we don't really need, just cookies.
            HttpResponse resResponse = dhcClient.execute(htpPost);
            Log.d(TAG, EntityUtils.toString(resResponse.getEntity()));

            for (Cookie cooCookie:  new PersistentCookieStore(ctxContext).getCookies()) {
                if (cooCookie.getName().equalsIgnoreCase("I")) {
                    Log.d(TAG, String.format("Successfully authenticated. Got an authentication token: %s", cooCookie.getValue()));
                    return cooCookie.getValue();
                }
            }

            return null;

        } catch (SocketException e) {
            throw new ConnectivityException(
                    "An error occurred while trying to connect to Saavn.", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "The default encoding is not supported.", e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(
                    "A protocol exception was encountered.", e);
        } catch (ParseException e) {
            throw new RuntimeException(
                    "An error occurred while trying to read the header elements.", e);
        } catch (IOException e) {
            throw new RuntimeException(
                    "An error occurred while trying to read response stream.", e);
        }

    }


    /*
     * This method connects to Saavn and fetches a list of the user's starred tracks in a
     * JSON format.
     *
     * @param  ctxContext             The context of the calling activity for getting a cookie-store instance
     * @return                        An array containing the user's starred tracks.
     * @throws ConnectivityException  When there was a temporary connectivity problem
     */
    public static JSONArray getStarred(Context ctxContext) throws ConnectivityException {

        try {

            Log.d(TAG, "Fetching the list of starred tracks.");

            HttpParams htpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(htpParameters, 10000);

            //Prepare the data that will be posted to Saavn for authentication
            List<NameValuePair> lstCredentials = new ArrayList<NameValuePair>();
            lstCredentials.add(new BasicNameValuePair("t", String.valueOf(System.currentTimeMillis() / 1000)));
            lstCredentials.add(new BasicNameValuePair("_format", "json"));
            lstCredentials.add(new BasicNameValuePair("_marker", "0"));
            lstCredentials.add(new BasicNameValuePair("__call", "favourites.listDetails"));

            //Attach the persistent cookie storage to the connection handler we created.
            HttpPost htpPost = new HttpPost(API_URL);
            htpPost.setEntity(new UrlEncodedFormEntity(lstCredentials));
            htpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:18.0) Gecko/20100101 Firefox/18.0");
            htpPost.setHeader("X-Requested-With", "XMLHttpRequest");
            htpPost.setHeader("Referer", "http://www.saavn.com/s/");
            htpPost.addHeader("Accept-Encoding", "gzip");
            DefaultHttpClient dhcClient = new DefaultHttpClient(htpParameters);
            dhcClient.addResponseInterceptor(new GzippedInterceptor(), 0);
            PersistentCookieStore pscStore = new PersistentCookieStore(ctxContext);
            dhcClient.setCookieStore(pscStore);

            //Make the request and get the response which we don't really need, just cookies.
            HttpResponse resResponse = dhcClient.execute(htpPost);
            String strResponse = EntityUtils.toString(resResponse.getEntity());

            strResponse = StringUtils.strip(StringUtils.trim(StringEscapeUtils.unescapeJava(strResponse)), "\"");
            JSONArray lstTracks = new JSONArray(strResponse);
            return lstTracks;

        } catch (JSONException e) {
            throw new RuntimeException(
                    "An error occurred while trying to read the response data.", e);
        } catch (SocketException e) {
            throw new ConnectivityException(
                    "An error occurred while trying to connect to Saavn.", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "The default encoding is not supported.", e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(
                    "A protocol exception was encountered.", e);
        } catch (ParseException e) {
            throw new RuntimeException(
                    "An error occurred while trying to read the header elements.", e);
        } catch (IOException e) {
            throw new RuntimeException(
                    "An error occurred while trying to read response stream.", e);
        }

    }


    /*
     * This method connects to Saavn and unstars a track that has been deleted
     * locally.
     *
     * @param  ctxContext             The context of the calling activity for getting a cookie-store instance
     * @param  strId                  The unique identifier of the track to unstar
     * @throws ConnectivityException  When there was a temporary connectivity problem
     */
	public static void unstarTrack(Context ctxContext, String strId)
			throws ConnectivityException {

        try {

            Log.d(TAG, "Unstarring the locally deleted track.");

            HttpParams htpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(htpParameters, 10000);

            //Prepare the data that will be posted to Saavn for authentication
            List<NameValuePair> lstCredentials = new ArrayList<NameValuePair>();
            lstCredentials.add(new BasicNameValuePair("t", String.valueOf(System.currentTimeMillis() / 1000)));
            lstCredentials.add(new BasicNameValuePair("pid", strId));
            lstCredentials.add(new BasicNameValuePair("_marker", "0"));
            lstCredentials.add(new BasicNameValuePair("__call", "favourites.removeSong"));

            //Attach the persistent cookie storage to the connection handler we created.
            HttpPost htpPost = new HttpPost(API_URL);
            htpPost.setEntity(new UrlEncodedFormEntity(lstCredentials));
            htpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:18.0) Gecko/20100101 Firefox/18.0");
            htpPost.setHeader("X-Requested-With", "XMLHttpRequest");
            htpPost.setHeader("Referer", "http://www.saavn.com/s/");
            htpPost.addHeader("Accept-Encoding", "gzip");
            DefaultHttpClient dhcClient = new DefaultHttpClient(htpParameters);
            dhcClient.addResponseInterceptor(new GzippedInterceptor(), 0);
            PersistentCookieStore pscStore = new PersistentCookieStore(ctxContext);
            dhcClient.setCookieStore(pscStore);

            //Make the request and get the response which we don't really need, just cookies.
            HttpResponse resResponse = dhcClient.execute(htpPost);
            Log.d(TAG, EntityUtils.toString(resResponse.getEntity()));

            return;

        } catch (SocketException e) {
            throw new ConnectivityException(
                    "An error occurred while trying to connect to Saavn.", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "The default encoding is not supported.", e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(
                    "A protocol exception was encountered.", e);
        } catch (ParseException e) {
            throw new RuntimeException(
                    "An error occurred while trying to read the header elements.", e);
        } catch (IOException e) {
            throw new RuntimeException(
                    "An error occurred while trying to read response stream.", e);
        }

    }

}