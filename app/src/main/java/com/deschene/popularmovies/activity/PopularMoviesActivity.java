package com.deschene.popularmovies.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.deschene.popularmovies.R;

/**
 * The main activity for this application. Displays a grid view of popular movies through {@link
 * com.deschene.popularmovies.fragment.PopularMoviesFragment}.
 */
public class PopularMoviesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_movies);
    }

}
