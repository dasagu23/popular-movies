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
 * AsyncTask for fetching movie reviews.
 */
public class AbsractFetchReviewsTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = AbsractFetchReviewsTask.class.getSimpleName();

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
                    .appendPath("movie").appendPath(params[0]).appendPath("reviews")
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
     * Converts the json string of movie data to an array of String reviews.
     *
     * @param reviewsJsonStr the string of json
     * @return a String array of reviews
     * @throws JSONException if parsing string fails
     */
    public String[] getDataFromJson(final String reviewsJsonStr) throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_RESULTS = "results";
        final String OWM_KEY = "content";

        final JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
        final JSONArray reviewsArray = reviewsJson.getJSONArray(OWM_RESULTS);

        final String[] reviews = new String[reviewsArray.length()];

        for (int i = 0; i < reviewsArray.length(); i++) {
            final JSONObject movieObject = reviewsArray.getJSONObject(i);

            reviews[i] = movieObject.getString(OWM_KEY);
        }

        return reviews;
    }
}
