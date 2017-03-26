package de.alfingo.whattowatch.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A DB helper for our MoviesDB
 * @author Rafael
 * @since 26.03.2017
 */
class MovieDBHelper extends SQLiteOpenHelper {

    /**
     * Gets an instance of the DB helper for the What to Watch DB.
     */
    MovieDBHelper(Context context) {
        super(context, MoviesContract.DB_NAME, null, MoviesContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MoviesContract.FavoriteMoviesEntry.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(MoviesContract.FavoriteMoviesEntry.DROP_TABLE);
        onCreate(db);
    }
}
