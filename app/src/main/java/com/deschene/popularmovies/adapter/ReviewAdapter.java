package com.deschene.popularmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.deschene.popularmovies.R;

import java.util.Arrays;

/**
 *
 */
public class ReviewAdapter extends ArrayAdapter<String> {

    private String[] mReviews;

    public ReviewAdapter(final Context context, final String[] reviews) {
        super(context, 0, Arrays.asList(reviews));
        mReviews = reviews;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_review, parent, false);
        }

        final TextView reviewText = (TextView) convertView.findViewById(R.id.trailer_text);

        reviewText.setText(mReviews[position]);

        return convertView;
    }
}
