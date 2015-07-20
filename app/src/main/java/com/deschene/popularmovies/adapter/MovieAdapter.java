package com.deschene.popularmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.deschene.popularmovies.R;
import com.deschene.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom adapter for displaying Movies in a {@link android.widget.GridView}
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    private Context mContext;
    private int mResourceId;
    private List<Movie> mMovies;

    /**
     * Constructor.
     *
     * @param context the application context
     * @param resource the resource id of the layout for each view
     * @param movies the list of movies
     */
    public MovieAdapter(Context context, int resource, ArrayList<Movie> movies) {
        super(context, resource, movies);
        mContext = context;
        mResourceId = resource;
        mMovies = movies;
    }

    @Override
    public int getCount() {
        return mMovies.size();
    }

    @Override
    public Movie getItem(int position) {
        return mMovies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Movie movie = mMovies.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId, parent, false);
        }
        String url = movie.getMoviePosterUrl();
        final ImageView imageView =
                (ImageView) convertView.findViewById(R.id.grid_item_movie_imageview);

        Picasso.with(mContext).load(url).into(imageView);

        return convertView;
    }
}
