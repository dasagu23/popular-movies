package com.deschene.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Movie model class. Contains the basic contents describing a movie <ul> <li> Original Title </li>
 * <li> Movie Poster thumbnail (image URL) </li> <li> Plot synopsis (overview) </li> <li> User
 * rating (vote_average) </li> <li> Release date </li> </ul>
 */
public class Movie implements Parcelable {

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(final Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(final int size) {
            return new Movie[size];
        }
    };

    /**
     * Base URL for movie poster request
     */
    private static final String sBaseUrl = "http://image.tmdb.org/t/p/";
    /**
     * Image size request from the server
     */
    private static final String sUrlImageSize = "w500/";

    private final String mId;
    private final String mOriginalTitle;
    private final String mMoviePosterUrl;
    private final String mOverview;
    private final String mVoteAverage;
    private final String mReleaseDate;

    /**
     * Constructor.
     *
     * @param id themoviedb
     * @param originalTitle title
     * @param moviePosterUrl poster URL
     * @param overview overview
     * @param voteAverage vote average
     * @param releaseDate release dat
     */
    public Movie(final String id, final String originalTitle, final String moviePosterUrl,
            final String overview, final String voteAverage, final String releaseDate) {
        mId = id;
        mOriginalTitle = originalTitle;
        mMoviePosterUrl = sBaseUrl + sUrlImageSize + moviePosterUrl;
        mOverview = overview;
        mVoteAverage = voteAverage;
        mReleaseDate = releaseDate;
    }

    private Movie(final Parcel in) {
        mId = in.readString();
        mOriginalTitle = in.readString();
        mMoviePosterUrl = in.readString();
        mOverview = in.readString();
        mVoteAverage = in.readString();
        mReleaseDate = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(mId);
        dest.writeString(mOriginalTitle);
        dest.writeString(mMoviePosterUrl);
        dest.writeString(mOverview);
        dest.writeString(mVoteAverage);
        dest.writeString(mReleaseDate);
    }

    /**
     * @return the movie's themoviedb id.
     */
    public String getId() {
        return mId;
    }

    /**
     * @return original title.
     */
    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    /**
     * @return poster URL.
     */
    public String getMoviePosterUrl() {
        return mMoviePosterUrl;
    }

    /**
     * @return overview.
     */
    public String getOverview() {
        return mOverview;
    }

    /**
     * @return vote average.
     */
    public String getVoteAverage() {
        return mVoteAverage;
    }

    /**
     * @param yearFormat if true
     * @return release date.
     */
    public String getReleaseDate(final boolean yearFormat) {

        return yearFormat ? mReleaseDate.substring(0, 4) : mReleaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
