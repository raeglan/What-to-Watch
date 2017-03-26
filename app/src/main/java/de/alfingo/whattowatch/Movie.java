package de.alfingo.whattowatch;

import android.net.Uri;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * The Movie Object, as received from MovieDB, created automatically with Gson.
 *
 * @author Rafael
 * @since 23.01.2017
 */
public class Movie implements Serializable {

    // everything here must be named exactly as in the DB.
    public String poster_path;
    public String overview;
    public Date release_date;
    public int[] genre_ids;
    public int id;
    public String original_title;
    public String original_language;
    public String title;
    public String backdrop_path;
    public double popularity;
    public int vote_count;
    public float vote_average;

    // these are set after the fact.
    public List<Review> reviews;
    public List<MovieVideo> videos;
    public boolean favorite;

    /**
     * A review POJO.
     */
    public class Review {
        public String author;
        public String content;
    }

    /**
     * A POJO for storing the video informations.
     */
    public class MovieVideo {
        public String site;
        public String name;
        public String key;
    }
}