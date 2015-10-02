package com.deschene.popularmovies.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.activity.DetailActivity;
import com.deschene.popularmovies.adapter.MovieAdapter;
import com.deschene.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Fragment that displays a gridview of popular movies.
 */
public class PopularMoviesFragment extends Fragment {

    private static final String STATE_MOVIES = "movies";

    private MovieAdapter mMovieAdapter;
    private Movie[] mMovies;

    public PopularMoviesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_popular_movies, container, false);

        mMovieAdapter =
                new MovieAdapter(getActivity(), R.layout.grid_item_movie, new ArrayList<Movie>());

        final GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Movie movie = mMovieAdapter.getItem(position);
                final Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Movie.EXTRA_MOVIE, movie);
                startActivity(intent);
            }
        });

        if (savedInstanceState == null) {
            updateMovies();
        } else {
            // load movies from bundle
            mMovies = (Movie[]) savedInstanceState.getParcelableArray(STATE_MOVIES);
            if (mMovies != null) {
                mMovieAdapter.clear();
                for (Movie movie : mMovies) {
                    mMovieAdapter.add(movie);
                }
                mMovieAdapter.notifyDataSetChanged();
            }
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArray(STATE_MOVIES, mMovies);
    }

    /**
     * Starts the request to update the movies in the db.
     */
    private void updateMovies() {
        new FetchMoviesTask().execute(getString(R.string.themoviesdb_api_key));
    }

    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortType = prefs.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_sort_order_default));
        sortType = sortType.equals(getString(R.string.pref_sort_order_highest_rated_value)) ?
                getString(R.string.pref_sort_order_highest_rated_title) :
                getString(R.string.pref_sort_order_most_popular_title);

        sortType = String.format(getString(R.string.popular_movies_sort_by_type_format), sortType);
        ((TextView) getActivity().findViewById(android.R.id.text1)).setText(sortType);
    }

    /**
     * AsyncTask for fetching the movies from themoviedb.
     */
    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected Movie[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                final SharedPreferences
                        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                final String pref = prefs.getString(getString(R.string.pref_sort_order_key),
                        getString(R.string.pref_sort_order_default));

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https").authority("api.themoviedb.org").appendPath("3")
                        .appendPath("discover").appendPath("movie")
                        .appendQueryParameter("sort_by", pref)
                        .appendQueryParameter("api_key", params[0]);

                URL url = new URL(builder.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
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
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
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
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // THIS WILL HAPPEN ONLY IF THERE IS AN ERROR IN THE JSON
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] movieData) {
            super.onPostExecute(movieData);
            mMovies = movieData;
            if (movieData != null) {
                mMovieAdapter.clear();
                for (Movie movie : movieData) {
                    mMovieAdapter.add(movie);
                }
                mMovieAdapter.notifyDataSetChanged();
            }
        }
    }

    public Movie[] getMovieDataFromJson(final String moviesJsonStr) throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_RESULTS = "results";
        final String OWM_ORIGINAL_TITLE = "original_title";
        final String OWM_POSTER_URL = "poster_path";
        final String OWM_OVERVIEW = "overview";
        final String OWM_VOTE_AVERAGE = "vote_average";
        final String OWM_RELEASE_DATE = "release_date";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);

        Movie[] movies = new Movie[moviesArray.length()];

        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieObject = moviesArray.getJSONObject(i);

            movies[i] = new Movie(movieObject.getString(OWM_ORIGINAL_TITLE),
                    movieObject.getString(OWM_POSTER_URL), movieObject.getString(OWM_OVERVIEW),
                    movieObject.getString(OWM_VOTE_AVERAGE),
                    movieObject.getString(OWM_RELEASE_DATE));
        }

        return movies;
    }
}
