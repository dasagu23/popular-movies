package com.deschene.popularmovies.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Image view that that ensures a good aspect ratio for the movie posters.
 */
public class PosterImageView extends ImageView {
    public PosterImageView(final Context context) {
        super(context);
    }

    public PosterImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public PosterImageView(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() * 1.48));
    }
}
