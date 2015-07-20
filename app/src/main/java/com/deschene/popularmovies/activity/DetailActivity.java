package com.deschene.popularmovies.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private Movie mMovie;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            final Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Movie.EXTRA_MOVIE)) {
                mMovie = intent.getParcelableExtra(Movie.EXTRA_MOVIE);
                ((TextView) rootView.findViewById(R.id.detail_movie_title))
                        .setText(mMovie.getOriginalTitle());
                ((TextView) rootView.findViewById(R.id.detail_movie_plot))
                        .setText(mMovie.getOverview());
                final String userRating = String.format(getString(R.string.detail_user_rating_format),
                        mMovie.getVoteAverage());
                ((TextView) rootView.findViewById(R.id.detail_movie_user_rating))
                        .setText(userRating);
                final String releaseDate =
                        String.format(getString(R.string.detail_release_date_formt),
                                mMovie.getReleaseDate());
                ((TextView) rootView.findViewById(R.id.detail_movie_release_date))
                        .setText(releaseDate);
                ImageView posterImage = ((ImageView) rootView.findViewById(R.id.detail_poster_image_view));
                Picasso.with(getActivity()).load(mMovie.getMoviePosterUrl()).into(posterImage);
            }

            return rootView;
        }
    }
}
