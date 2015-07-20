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
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    /**
     * Tag for passing as a parcelable
     */
    public static final String EXTRA_MOVIE = "extra_movie";

    /**
     * Base URL for movie poster request
     */
    private static final String sBaseUrl = "http://image.tmdb.org/t/p/";
    /**
     * Image size request from the server
     */
    private static final String sUrlImageSize = "w500/";

    private String mOriginalTitle = null;
    private String mMoviePosterUrl = null;
    private String mOverview = null;
    private String mVoteAverage = null;
    private String mReleaseDate = null;

    public Movie(String mOriginalTitle, String mMoviePosterUrl, String mOverview,
            String mVoteAverage, String mReleaseDate) {
        this.mOriginalTitle = mOriginalTitle;
        this.mMoviePosterUrl = sBaseUrl + sUrlImageSize + mMoviePosterUrl;
        this.mOverview = mOverview;
        this.mVoteAverage = mVoteAverage;
        this.mReleaseDate = mReleaseDate;
    }

    private Movie(Parcel in) {
        mOriginalTitle = in.readString();
        mMoviePosterUrl = in.readString();
        mOverview = in.readString();
        mVoteAverage = in.readString();
        mReleaseDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mOriginalTitle);
        dest.writeString(mMoviePosterUrl);
        dest.writeString(mOverview);
        dest.writeString(mVoteAverage);
        dest.writeString(mReleaseDate);
    }

    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    public String getMoviePosterUrl() {
        return mMoviePosterUrl;
    }

    public String getOverview() {
        return mOverview;
    }

    public String getVoteAverage() {
        return mVoteAverage;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
