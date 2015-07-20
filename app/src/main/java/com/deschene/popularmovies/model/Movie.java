package com.deschene.popularmovies.model;

/**
 * Movie model class. Contains the basic contents describing a movie
 *      - Original Title
 *      - Movie Poster thumbnail (image URL)
 *      - Plot synopsis (overview)
 *      - User rating (vote_average)
 *      - Release date
 */
public class Movie {

    private String mOriginalTitle = null;
    private String mMoviePosterUrl = null;
    private String mPlotSynopsis = null;
    private String mUserRating = null;
    private String mReleaseDate = null;

    public Movie(String mOriginalTitle, String mMoviePosterUrl, String mPlotSynopsis,
            String mUserRating, String mReleaseDate) {
        this.mOriginalTitle = mOriginalTitle;
        this.mMoviePosterUrl = mMoviePosterUrl;
        this.mPlotSynopsis = mPlotSynopsis;
        this.mUserRating = mUserRating;
        this.mReleaseDate = mReleaseDate;
    }

    public void setOriginalTitle(String mOriginalTitle) {
        this.mOriginalTitle = mOriginalTitle;
    }

    public void setMoviePosterUrl(String mMoviePosterUrl) {
        this.mMoviePosterUrl = mMoviePosterUrl;
    }

    public void setPlotSynopsis(String mPlotSynopsis) {
        this.mPlotSynopsis = mPlotSynopsis;
    }

    public void setUserRating(String mUserRating) {
        this.mUserRating = mUserRating;
    }

    public void setReleaseDate(String mReleaseDate) {
        this.mReleaseDate = mReleaseDate;
    }

    public String getOriginalTitle() {

        return mOriginalTitle;
    }

    public String getMoviePosterUrl() {
        return mMoviePosterUrl;
    }

    public String getPlotSynopsis() {
        return mPlotSynopsis;
    }

    public String getUserRating() {
        return mUserRating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }
}
