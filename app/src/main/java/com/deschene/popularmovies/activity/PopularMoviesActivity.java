package com.deschene.popularmovies.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.fragment.PopularMoviesFragment;
import com.deschene.popularmovies.model.Movie;
import com.facebook.stetho.Stetho;

/**
 * The main activity for this application. Displays a grid view of popular movies through {@link
 * com.deschene.popularmovies.fragment.PopularMoviesFragment}.
 */
public class PopularMoviesActivity extends AppCompatActivity
        implements PopularMoviesFragment.Callback {

    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());

        mTwoPane = findViewById(R.id.detail_container) != null;

        PopularMoviesFragment fragment =
                (PopularMoviesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    public void onItemSelected(final Movie movie) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            //            Bundle args = new Bundle();
            //            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            //            DetailFragment fragment = new DetailFragment();
            //            fragment.setArguments(args);

            //            getSupportFragmentManager().beginTransaction()
            //                    .replace(R.id.weather_detail_container, fragment,
            // DETAILFRAGMENT_TAG)
            //                    .commit();
        } else {
            final Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_MOVIE, movie);
            startActivity(intent);
        }
    }
}
