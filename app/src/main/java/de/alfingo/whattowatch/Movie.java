package de.alfingo.whattowatch;

import java.util.Date;

/**
 * The Movie Object, as received from MovieDB.
 * @author Rafael
 * @since 23.01.2017
 */
public class Movie {
    final String posterPath;
    final String overview;
    final Date releaseDate;
    final int[] genreIDs;
    final int id;
    final String originalTitle;
    final String originalLanguage;
    final String title;
    final String backdropPath;
    final double popularity;
    final int voteCount;
    final double voteAverage;

    /**
     * Creates a new movie object with all the data got from the MovieDB API.
     * @param posterPath Path to poster image
     * @param overview the text describing the movie
     * @param releaseDate release date in the locale asked
     * @param genreIDs ids of the genre
     * @param id id of the movie
     * @param originalTitle ...
     * @param originalLanguage saved in locale(en, de, etc)
     * @param title the title in the language queried
     * @param backdropPath path to backdrop
     * @param popularity popularity
     * @param voteCount how many voted
     * @param voteAverage double with votes average
     */
    public Movie(String posterPath, String overview, Date releaseDate, int[] genreIDs,
                 int id, String originalTitle, String originalLanguage,
                 String title, String backdropPath, double popularity,
                 int voteCount, double voteAverage) {
        // TODO: 23.01.2017 use this class for something, see if we are asking for too much.
        this.posterPath = posterPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.genreIDs = genreIDs;
        this.id = id;
        this.originalTitle = originalTitle;
        this.originalLanguage = originalLanguage;
        this.title = title;
        this.backdropPath = backdropPath;
        this.popularity = popularity;
        this.voteCount = voteCount;
        this.voteAverage = voteAverage;
    }
}
