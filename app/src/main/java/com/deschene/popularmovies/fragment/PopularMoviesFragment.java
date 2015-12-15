package com.deschene.popularmovies.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.activity.SettingsActivity;
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

    /**
     * Request code for onActivityResult. Tells this fragment to update the movie from the server
     * due to a settings change.
     */
    public static final int UPDATE_SETTINGS_REQUEST = 1;

    private static final String STATE_MOVIES = "movies";

    private MovieAdapter mMovieAdapter;
    private Movie[] mMovies;

    /**
     * Empty constructor for system fragment creation.
     */
    public PopularMoviesFragment() {
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Movie movie);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(getActivity(), SettingsActivity.class),
                    UPDATE_SETTINGS_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_popular_movies, container, false);

        mMovieAdapter =
                new MovieAdapter(getActivity(), R.layout.grid_item_movie, new ArrayList<Movie>());

        final GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                final Movie movie = mMovieAdapter.getItem(position);
                ((Callback) getActivity()).onItemSelected(movie);
            }
        });

        if (savedInstanceState == null) {
            updateMovies();
        } else {
            // load movies from bundle
            mMovies = (Movie[]) savedInstanceState.getParcelableArray(STATE_MOVIES);
            if (mMovies != null) {
                mMovieAdapter.clear();
                for (final Movie movie : mMovies) {
                    mMovieAdapter.add(movie);
                }
                mMovieAdapter.notifyDataSetChanged();
            }
        }

        return rootView;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_SETTINGS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                // If the update settings request is ok, refresh movies
                updateMovies();
            }
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
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
        final SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        protected Movie[] doInBackground(final String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                final SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                final String pref = prefs.getString(getString(R.string.pref_sort_order_key),
                        getString(R.string.pref_sort_order_default));

                final Uri.Builder builder = new Uri.Builder();
                builder.scheme("https").authority("api.themoviedb.org").appendPath("3")
                        .appendPath("discover").appendPath("movie")
                        .appendQueryParameter("sort_by", pref)
                        .appendQueryParameter("api_key", params[0]);

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
                moviesJsonStr = buffer.toString();
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
                return getMovieDataFromJson(moviesJsonStr);
            } catch (final JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            // THIS WILL HAPPEN ONLY IF THERE IS AN ERROR IN THE JSON
            return null;
        }

        @Override
        protected void onPostExecute(final Movie[] movieData) {
            super.onPostExecute(movieData);
            mMovies = movieData;
            if (movieData != null) {
                mMovieAdapter.clear();
                for (final Movie movie : movieData) {
                    mMovieAdapter.add(movie);
                }
                mMovieAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Converts the json string of movie data to an array of movies.
     *
     * @param moviesJsonStr the string of json
     * @return an array of {@link Movie}
     * @throws JSONException if parsing string fails
     */
    public Movie[] getMovieDataFromJson(final String moviesJsonStr) throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_RESULTS = "results";
        final String OWM_ORIGINAL_TITLE = "original_title";
        final String OWM_POSTER_URL = "poster_path";
        final String OWM_OVERVIEW = "overview";
        final String OWM_VOTE_AVERAGE = "vote_average";
        final String OWM_RELEASE_DATE = "release_date";
        final String OWM_ID = "id";

        final JSONObject moviesJson = new JSONObject(moviesJsonStr);
        final JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);

        final Movie[] movies = new Movie[moviesArray.length()];

        for (int i = 0; i < moviesArray.length(); i++) {
            final JSONObject movieObject = moviesArray.getJSONObject(i);

            movies[i] = new Movie(movieObject.getString(OWM_ID),
                    movieObject.getString(OWM_ORIGINAL_TITLE),
                    movieObject.getString(OWM_POSTER_URL), movieObject.getString(OWM_OVERVIEW),
                    movieObject.getString(OWM_VOTE_AVERAGE),
                    movieObject.getString(OWM_RELEASE_DATE));
        }

        return movies;
    }
}
