package com.deschene.popularmovies.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.fragment.DetailFragment;
import com.deschene.popularmovies.model.Movie;

/**
 * Movie Details view activity.
 */
public class DetailActivity extends AppCompatActivity {

    /**
     * Tag for passing as a parcelable
     */
    public static final String EXTRA_MOVIE = "extra_movie";

    private Movie mMovie;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (savedInstanceState == null) {
            if (getIntent().hasExtra(EXTRA_MOVIE)) {
                mMovie = getIntent().getParcelableExtra(EXTRA_MOVIE);
            }
            final DetailFragment fragment = DetailFragment.newInstance(mMovie);
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment)
                    .commit();
        }
    }

}
