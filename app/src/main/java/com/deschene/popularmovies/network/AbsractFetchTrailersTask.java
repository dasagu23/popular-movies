package com.deschene.popularmovies.network;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AsyncTask for fetching movie trailers.
 */
public class AbsractFetchTrailersTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = AbsractFetchTrailersTask.class.getSimpleName();

    @Override
    protected String[] doInBackground(final String... params) {
        // params[0] = id
        // params[2] = api key

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        final String jsonFromServer;
        try {
            final Uri.Builder builder = new Uri.Builder();
            builder.scheme("https").authority("api.themoviedb.org").appendPath("3")
                    .appendPath("movie").appendPath(params[0]).appendPath("videos")
                    .appendQueryParameter("api_key", params[1]);

            final URL url = new URL(builder.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            final InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            jsonFromServer = buffer.toString();
        } catch (final IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // Can't parse data if it wasn't successfully retrieved.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getDataFromJson(jsonFromServer);
        } catch (final JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return null;
    }

    /**
     * Converts the json string of movie data to an array of String trailers.
     *
     * @param trailersJsonStr the string of json
     * @return a String array of trailers
     * @throws JSONException if parsing string fails
     */
    public String[] getDataFromJson(final String trailersJsonStr) throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_RESULTS = "results";
        final String OWM_KEY = "key";

        final JSONObject trailersJson = new JSONObject(trailersJsonStr);
        final JSONArray trailersArray = trailersJson.getJSONArray(OWM_RESULTS);

        final String[] trailers = new String[trailersArray.length()];

        for (int i = 0; i < trailersArray.length(); i++) {
            final JSONObject movieObject = trailersArray.getJSONObject(i);

            trailers[i] = movieObject.getString(OWM_KEY);
        }

        return trailers;
    }
}
