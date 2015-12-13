package com.deschene.popularmovies.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.data.MoviesContract;
import com.facebook.stetho.Stetho;

/**
 * The main activity for this application. Displays a grid view of popular movies through {@link
 * com.deschene.popularmovies.fragment.PopularMoviesFragment}.
 */
public class PopularMoviesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());

        // todo remove
        Cursor cursor = getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI, null, null, null, null);
    }
}
