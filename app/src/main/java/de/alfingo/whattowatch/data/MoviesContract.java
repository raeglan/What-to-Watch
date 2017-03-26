package de.alfingo.whattowatch.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * The contract class for persisting movies.
 * @author Rafael
 * @since 26.03.2017
 */
public class MoviesContract {
    public final static String DB_NAME = "what-to-watch.db";
    public final static int DB_VERSION = 1;

    public final static String AUTHORITY = "de.alfingo.whattowatch";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_FAVORITES = "favorites";

    public static class FavoriteMoviesEntry implements BaseColumns {

        public final static Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES).build();

        public final static String TABLE_NAME = "FavoriteMovies";

        public final static String COLUMN_MOVIE_ID = "MovieID";

        public final static String COLUMN_POSTER_PATH = "PosterPath";

        public final static String COLUMN_TITLE = "MovieTitle";

        public final static String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_TITLE + " TEXT NOT NULL," +
                        COLUMN_POSTER_PATH + " TEXT NOT NULL," +
                        "UNIQUE (" + COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        public final static String DROP_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
