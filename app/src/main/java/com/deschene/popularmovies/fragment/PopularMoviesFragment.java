package com.deschene.popularmovies.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.model.Movie;

import java.util.ArrayList;

/**
 * Fragment that displays a gridview of popular movies.
 */
public class PopularMoviesFragment extends Fragment {

    private ArrayAdapter<Movie> mMovieAdapter;

    public PopularMoviesFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        // Kick off movie request
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_popular_movies, container, false);

        mMovieAdapter =
                new ArrayAdapter<>(getActivity(), R.layout.grid_item_movie, new ArrayList<Movie>());

        final GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Movie movie = mMovieAdapter.getItem(position);
//                final Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
//                intent.putExtra(Intent.EXTRA_TEXT, "yada yada");
//                startActivity(intent);
            }
        });

        return rootView;
    }
}
