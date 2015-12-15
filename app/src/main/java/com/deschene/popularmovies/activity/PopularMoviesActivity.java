package com.deschene.popularmovies.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.fragment.DetailFragment;
import com.deschene.popularmovies.fragment.PopularMoviesFragment;
import com.deschene.popularmovies.model.Movie;
import com.deschene.popularmovies.network.AbstractFetchGuestSessionIdTask;
import com.facebook.stetho.Stetho;

/**
 * The main activity for this application. Displays a grid view of popular movies through {@link
 * com.deschene.popularmovies.fragment.PopularMoviesFragment}.
 */
public class PopularMoviesActivity extends AppCompatActivity
        implements PopularMoviesFragment.Callback {

    private boolean mTwoPane;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);

        final String sessionId = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_session_id_key), null);
        if (sessionId == null) {
            new FetchSessionIdTask().execute(getString(R.string.themoviesdb_api_key));
        }

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
            final DetailFragment fragment = DetailFragment.newInstance(movie);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment, DetailFragment.class.getSimpleName())
                    .commit();
        } else {
            final Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_MOVIE, movie);
            startActivity(intent);
        }
    }

    private void saveSessionIdToPrefs(final String id) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putString(getString(R.string.pref_session_id_key), id).apply();
    }

    public class FetchSessionIdTask extends AbstractFetchGuestSessionIdTask {
        @Override
        protected void onPostExecute(final String id) {
            super.onPostExecute(id);
            saveSessionIdToPrefs(id);
        }
    }
}
