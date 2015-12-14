package com.deschene.popularmovies.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.adapter.TrailerAdapter;
import com.deschene.popularmovies.data.MoviesContract;
import com.deschene.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

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
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private Switch mFavoriteSwitch;
    private Movie mMovie;
    private String[] mTrailers;
    private TrailerAdapter mTrailerAdapter;

    /**
     * Empty constructor for system creation.
     */
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        final Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Movie.EXTRA_MOVIE)) {
            mMovie = intent.getParcelableExtra(Movie.EXTRA_MOVIE);
            ((TextView) rootView.findViewById(R.id.detail_movie_title))
                    .setText(mMovie.getOriginalTitle());
            ((TextView) rootView.findViewById(R.id.detail_movie_plot))
                    .setText(mMovie.getOverview());
            final String userRating = String.format(getString(R.string.detail_user_rating_format),
                    mMovie.getVoteAverage());
            ((TextView) rootView.findViewById(R.id.detail_movie_user_rating)).setText(userRating);
            final String releaseDate = mMovie.getReleaseDate(true);
            ((TextView) rootView.findViewById(R.id.detail_movie_release_date)).setText(releaseDate);
            final ImageView posterImage =
                    (ImageView) rootView.findViewById(R.id.detail_poster_image_view);
            Picasso.with(getActivity()).load(mMovie.getMoviePosterUrl()).into(posterImage);
        }

        if (savedInstanceState == null) {
            updateTrailers();
        }

        return rootView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFavoriteSwitch = (Switch) getActivity().findViewById(R.id.favorite_switch);
        final Uri movieUri =
                MoviesContract.MovieEntry.buildMoviesUri(Long.parseLong(mMovie.getId()));
        final Cursor cursor =
                getActivity().getContentResolver().query(movieUri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mFavoriteSwitch.setChecked(true);
            }
            cursor.close();
        }

        mFavoriteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    // save to database if it's not there
                    final long movieId = Long.parseLong(mMovie.getId());
                    final Uri movieUri = MoviesContract.MovieEntry.buildMoviesUri(movieId);
                    final Cursor cursor = getActivity().getContentResolver()
                            .query(movieUri, null, null, null, null);
                    if (cursor != null) {

                        if (!cursor.moveToFirst()) {
                            // nothing in database, add it
                            final ContentValues movieValues = new ContentValues();

                            movieValues.put(MoviesContract.MovieEntry.COLUMN_ID, mMovie.getId());
                            movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE,
                                    mMovie.getOriginalTitle());
                            movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW,
                                    mMovie.getOverview());
                            movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_URL,
                                    mMovie.getMoviePosterUrl());
                            movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
                                    mMovie.getReleaseDate(false));
                            movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                                    mMovie.getVoteAverage());

                            getActivity().getContentResolver()
                                    .insert(MoviesContract.MovieEntry.CONTENT_URI, movieValues);
                        }
                        cursor.close();
                    }
                    Toast.makeText(getActivity(), "Yo", Toast.LENGTH_LONG).show();
                } else {
                    // remove from database
                    getActivity().getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI,
                            MoviesContract.MovieEntry.COLUMN_ID + "=?",
                            new String[] { mMovie.getId() });
                }
            }
        });
    }

    /**
     * Initialize trailers ListView.
     */
    private void onTrailersLoaded() {
        getView().findViewById(R.id.trailer_progress).setVisibility(View.GONE);
        final ListView trailerList = (ListView) getView().findViewById(R.id.trailer_list_view);
        mTrailerAdapter = new TrailerAdapter(getActivity(), mTrailers);
        trailerList.setAdapter(mTrailerAdapter);
        trailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                final String trailer = mTrailerAdapter.getItem(position);
                final Uri youtubeUri = Uri.parse("http://www.youtube.com/watch?v=" + trailer);
                final Intent intent = new Intent(Intent.ACTION_VIEW, youtubeUri);
                startActivity(intent);
            }
        });
    }

    private void updateTrailers() {
        new FetchTrailersTask().execute(mMovie.getId(), getString(R.string.themoviesdb_api_key));
    }

    /**
     * AsyncTask for fetching the movies from themoviedb.
     */
    public class FetchTrailersTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(final String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailersJsonString = null;

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
                trailersJsonString = buffer.toString();
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
                return getTrailerDataFromJson(trailersJsonString);
            } catch (final JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }

            // Won't happen unless there is a json error
            return null;
        }

        @Override
        protected void onPostExecute(final String[] data) {
            super.onPostExecute(null);

            mTrailers = data;
            // Update Trailers view
            onTrailersLoaded();
        }
    }

    /**
     * Converts the json string of movie data to an array of String trailers.
     *
     * @param trailersJsonStr the string of json
     * @return a String array of trailers
     * @throws JSONException if parsing string fails
     */
    public String[] getTrailerDataFromJson(final String trailersJsonStr) throws JSONException {
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
