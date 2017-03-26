package de.alfingo.whattowatch.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * a simple practically boilerplate content provider.
 * @author Rafael
 * @since 26.03.2017
 */
public class MoviesProvider extends ContentProvider{

    private final static int FAVORITES = 100;
    private final static int FAVORITE_WITH_ID = 101;

    private final UriMatcher sUriMatcher = buildMatcher();

    private MovieDBHelper mMovieDBHelper;

    private static UriMatcher buildMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_FAVORITES, FAVORITES);
        matcher.addURI(MoviesContract.AUTHORITY, MoviesContract.PATH_FAVORITES + "/#",
                FAVORITE_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mMovieDBHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mMovieDBHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor returnCursor;

        switch (match) {
            case FAVORITES:
                returnCursor = db.query(MoviesContract.FavoriteMoviesEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case FAVORITE_WITH_ID:
                String[] movieID = new String[]{uri.getLastPathSegment()};
                String idSelection = MoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID + "=?";
                returnCursor = db.query(MoviesContract.FavoriteMoviesEntry.TABLE_NAME, projection,
                        idSelection, movieID, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        if(returnCursor != null)
            //noinspection ConstantConditions
            returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        long rowID;
        SQLiteDatabase db = mMovieDBHelper.getWritableDatabase();

        if(match == FAVORITES) {
            rowID = db.insert(MoviesContract.FavoriteMoviesEntry.TABLE_NAME, null, values);
        } else
            throw new UnsupportedOperationException("Unknown uri: " + uri);

        if(rowID != -1) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mMovieDBHelper.getWritableDatabase();
        int rowsDeleted;

        switch (match) {
            case FAVORITES:
                rowsDeleted = db.delete(MoviesContract.FavoriteMoviesEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case FAVORITE_WITH_ID:
                String[] movieID = new String[]{uri.getLastPathSegment()};
                String idSelection = MoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID + "=?";
                rowsDeleted = db.delete(MoviesContract.FavoriteMoviesEntry.TABLE_NAME, idSelection,
                        movieID);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if(rowsDeleted > 0)
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented, for now not needed!");
    }
}
