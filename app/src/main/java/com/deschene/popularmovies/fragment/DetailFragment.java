package com.deschene.popularmovies.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.adapter.ReviewAdapter;
import com.deschene.popularmovies.adapter.TrailerAdapter;
import com.deschene.popularmovies.data.MoviesContract;
import com.deschene.popularmovies.model.Movie;
import com.deschene.popularmovies.network.AbsractFetchReviewsTask;
import com.deschene.popularmovies.network.AbsractFetchTrailersTask;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String ARG_MOVIE = DetailFragment.class.getSimpleName() + "movie";

    private Switch mFavoriteSwitch;
    private Movie mMovie;
    private String[] mReviews;
    private String[] mTrailers;
    private ReviewAdapter mReviewAdapter;
    private TrailerAdapter mTrailerAdapter;

    public static DetailFragment newInstance(final Movie movie) {
        final DetailFragment fragment = new DetailFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Empty constructor for system creation.
     */
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        mMovie = args.getParcelable(ARG_MOVIE);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ((TextView) rootView.findViewById(R.id.detail_movie_title))
                .setText(mMovie.getOriginalTitle());
        ((TextView) rootView.findViewById(R.id.detail_movie_plot)).setText(mMovie.getOverview());
        final String userRating = String.format(getString(R.string.detail_user_rating_format),
                mMovie.getVoteAverage());
        ((TextView) rootView.findViewById(R.id.detail_movie_user_rating)).setText(userRating);
        final String releaseDate = mMovie.getReleaseDate(true);
        ((TextView) rootView.findViewById(R.id.detail_movie_release_date)).setText(releaseDate);
        final ImageView posterImage =
                (ImageView) rootView.findViewById(R.id.detail_poster_image_view);
        Picasso.with(getActivity()).load(mMovie.getMoviePosterUrl()).into(posterImage);

        if (savedInstanceState == null) {
            fetchTrailers();
            fetchReviews();
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
                    cacheMovie();
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
     * Caches the movie in the database
     */
    private void cacheMovie() {
        final long movieId = Long.parseLong(mMovie.getId());
        final Uri movieUri = MoviesContract.MovieEntry.buildMoviesUri(movieId);
        final Cursor cursor =
                getActivity().getContentResolver().query(movieUri, null, null, null, null);
        if (cursor != null) {

            if (!cursor.moveToFirst()) {
                // nothing in database, add it
                final ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesContract.MovieEntry.COLUMN_ID, mMovie.getId());
                movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, mMovie.getOriginalTitle());
                movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
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
    }

    /**
     * Initialize trailers ListView.
     */
    private void onReviewsLoaded() {
        getView().findViewById(R.id.reviews_progress).setVisibility(View.GONE);
        final ListView reviewsList = (ListView) getView().findViewById(R.id.review_list_view);
        mReviewAdapter = new ReviewAdapter(getActivity(), mReviews);
        reviewsList.setAdapter(mReviewAdapter);
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

    private void fetchTrailers() {
        new FetchMovieTrailersTask()
                .execute(mMovie.getId(), getString(R.string.themoviesdb_api_key));
    }

    private void fetchReviews() {
        new FetchMovieReviewsTask()
                .execute(mMovie.getId(), getString(R.string.themoviesdb_api_key));
    }

    /**
     * AsyncTask for fetching the movies from themoviedb.
     */
    public class FetchMovieTrailersTask extends AbsractFetchTrailersTask {

        @Override
        protected void onPostExecute(final String[] data) {
            super.onPostExecute(null);

            mTrailers = data;
            onTrailersLoaded();
        }
    }

    /**
     * AsyncTask for fetching the movies from themoviedb.
     */
    public class FetchMovieReviewsTask extends AbsractFetchReviewsTask {

        @Override
        protected void onPostExecute(final String[] data) {
            super.onPostExecute(null);

            mReviews = data;
            onReviewsLoaded();
        }
    }
}
