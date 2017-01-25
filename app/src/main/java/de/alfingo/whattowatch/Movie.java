package de.alfingo.whattowatch;

import java.io.Serializable;
import java.util.Date;

/**
 * The Movie Object, as received from MovieDB, created automatically with Gson.
 * @author Rafael
 * @since 23.01.2017
 */
public class Movie implements Serializable {
    public static String KEY_EXTRA_MOVIE = "movie_key_extra";
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
}