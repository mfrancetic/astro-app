package com.udacity.astroapp.utils;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /* URL's of the photo, asteroid and observatory search */
    private static final String PHOTO_BASE_URL = "https://api.nasa.gov/planetary/apod?";
    private static final String ASTEROID_BASE_URl = "https://api.nasa.gov/neo/rest/v1/feed?";
    private static final String OBSERVATORY_BASE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=planetarium&observatory";
    private static final String OBSERVATORY_DETAILS_BASE_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

    /* Respective parameters and keys of the API queries */
    private static final String API_PARAM = "api_key";
    private static final String DATE_PARAM = "date";
    private static final String START_DATE_PARAM = "start_date";
    private static final String END_DATE_PARAM = "end_date";
    private static final String LOCATION_PARAM = "location";
    private static final String RADIUS_PARAM = "radius";
    private static final String GOOGLE_API_KEY_PARAM = "key";
    private static final String PLACE_ID_PARAM = "placeid";
    private static final String FIELDS_PARAM = "fields";
    private static final String LANGUAGE_PARAM = "language";
    private static final String radiusKey = "50000";
    private static final String fieldsKey = "name, photo, opening_hours, website";

    /* API keys */
    private static final String api_key = Secret.nasa_api_key;
    private static final String googleApiKey = Secret.google_play_services_api_key;

    private QueryUtils() {
    }

    /**
     * Create a URL for the photo
     */
    public static URL createPhotoUrl(String date) {
        URL url = null;
        Uri baserUri = Uri.parse(PHOTO_BASE_URL);

        Uri.Builder uriBuilder = baserUri.buildUpon();
        uriBuilder
                .appendQueryParameter(API_PARAM, api_key)
                .appendQueryParameter(DATE_PARAM, date)
                .build();
        try {
            url = new URL(uriBuilder.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL", e);
        }
        return url;
    }

    /**
     * Create a URL for the asteroids
     */
    public static URL createAsteroidUrl(String startDate, String endDate) {
        URL url = null;
        Uri baseUri = Uri.parse(ASTEROID_BASE_URl);

        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder
                .appendQueryParameter(START_DATE_PARAM, startDate)
                .appendQueryParameter(END_DATE_PARAM, endDate)
                .appendQueryParameter(API_PARAM, api_key)
                .build();
        try {
            url = new URL(uriBuilder.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL", e);
        }
        return url;
    }

    /**
     * Create a URL for the observatories
     */
    public static URL createObservatoryURL(String latitudeLongitude, String language) {
        URL url = null;
        Uri baseUri = Uri.parse(OBSERVATORY_BASE_URL);
        Uri.Builder builder = baseUri.buildUpon();
        builder.appendQueryParameter(GOOGLE_API_KEY_PARAM, googleApiKey)
                .appendQueryParameter(LOCATION_PARAM, latitudeLongitude)
                .appendQueryParameter(FIELDS_PARAM, fieldsKey)
                .appendQueryParameter(RADIUS_PARAM, radiusKey)
                .appendQueryParameter(LANGUAGE_PARAM, language)
                .build();
        try {
            url = new URL(builder.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL", e);
        }
        return url;
    }

    /**
     * Create a URL for the observatory details
     */
    public static URL createObservatoryDetailsUrl(String placeId, String language) {
        URL url = null;
        Uri baseUri = Uri.parse(OBSERVATORY_DETAILS_BASE_URL);
        Uri.Builder builder = baseUri.buildUpon();
        builder.appendQueryParameter(GOOGLE_API_KEY_PARAM, googleApiKey)
                .appendQueryParameter(PLACE_ID_PARAM, placeId)
                .appendQueryParameter(LANGUAGE_PARAM, language)
                .build();
        try {
            url = new URL(builder.toString());
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public static String makeHttpRequest(URL url) throws IOException {

        /* Define the read time out, connect time out, success response code and request method */
        int READ_TIME_OUT = 10000;
        int CONNECT_TIME_OUT = 10000;
        int SUCCESS_RESPONSE_CODE = 200;

        String REQUEST_METHOD = "GET";
        String jsonResponse = "";

        /* If the URL is null, then return early. */
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(READ_TIME_OUT);
            urlConnection.setConnectTimeout(CONNECT_TIME_OUT);
            urlConnection.setRequestMethod(REQUEST_METHOD);
            urlConnection.connect();

            /* If the request was successful (response code 200),
             then read the input stream and parse the response. */
            if (urlConnection.getResponseCode() == SUCCESS_RESPONSE_CODE) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the photo JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                /* Closing the input stream could throw an IOException, which is why
                 the makeHttpRequest(URL url) method signature specifies than an IOException
                 could be thrown. */
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the InputStream into a String which contains the whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                    Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
            reader.close();
        }
        return output.toString();
    }
}