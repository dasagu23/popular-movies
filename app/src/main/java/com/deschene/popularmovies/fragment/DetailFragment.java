package com.deschene.popularmovies.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private Movie mMovie;

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

        return rootView;
    }
}
