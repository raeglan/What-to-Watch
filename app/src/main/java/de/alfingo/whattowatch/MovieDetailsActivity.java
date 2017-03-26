package de.alfingo.whattowatch;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Target;

import java.io.IOException;

import de.alfingo.whattowatch.data.MoviesContract;
import de.alfingo.whattowatch.utilities.MovieDBUtil;

/**
 * The detail movie activity. This only uses the information already present for each movie.
 * No new DB query was needed to fill this page.
 *
 * @author Rafael Miranda
 * @since 24.01.2017
 */
public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Movie> {

    static final String TAG = MovieDetailsActivity.class.getSimpleName();

    static final int LOADER_ID = 1;

    private static final String MOVIE_ID = "extra-id";

    /**
     * Just a hard reference to our target, Picasso only keeps soft references.
     */
    Target mBackgroundTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.toolbar_layout_detail);

        String movieID = getIntent().getAction();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        R.string.under_construction, Toast.LENGTH_SHORT).show();
            }
        });

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
        // get the clicked movie and prepopulate our view.
        TextView descriptionView = (TextView) findViewById(R.id.tv_detail_description);
        RatingBar movieRating = (RatingBar) findViewById(R.id.ratingBar_details);
        TextView ratingDescription = (TextView)  findViewById(R.id.tv_detail_rating);
        TextView releaseDate = (TextView) findViewById(R.id.tv_detail_release_date);

        String movieID = getIntent().getAction();

        setTitle(mMovie.title);
        descriptionView.setText(mMovie.overview);
        float voteAverage = mMovie.vote_average /2;  // the average got is in 10 stars
        movieRating.setRating(voteAverage);
        ratingDescription.setText(getString(R.string.ratings_description,
                voteAverage,
                mMovie.vote_count));
        String formattedDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
                .format(mMovie.release_date);
        releaseDate.setText(formattedDate);

        // loading background image
        mBackgroundTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                toolbarLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d(TAG, "Bitmap load failed.");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.d(TAG, "Prepare load of Backdrop.");
            }
        };
        Picasso.with(this)
                .load(MovieDBUtil.getPictureUri(mMovie.backdrop_path,
                        MovieDBUtil.IMAGE_SIZE_ORIGINAL_PATH))
                .into(mBackgroundTarget);
        */

        Bundle loaderArgs = new Bundle();
        loaderArgs.putString(MOVIE_ID, movieID);
        getSupportLoaderManager().initLoader(LOADER_ID, loaderArgs, this);
    }

    @Override
    public Loader<Movie> onCreateLoader(int id, @NonNull final Bundle args) {
        return new AsyncTaskLoader<Movie>(this) {

            /**
             * For storing the movie data, this prevents a reload sometimes.
             */
            Movie loadedMovie;

            @Override
            protected void onStartLoading() {
                if (loadedMovie != null)
                    deliverResult(loadedMovie);
                else
                    forceLoad();
            }

            @Override
            public Movie loadInBackground() {
                String movieID = args.getString(MOVIE_ID);
                Movie returnMovie = null;
                if (movieID != null) {
                    try {
                        returnMovie = MovieDBUtil.getMovieDetails(movieID);
                    } catch (IOException e) {
                        // TODO: 26.03.2017 Show the user some feedback on what went wrong.
                        e.printStackTrace();
                    }
                }

                // getting if the movie already is a favorite or not.
                Uri movieWithID = MoviesContract.FavoriteMoviesEntry.CONTENT_URI.buildUpon()
                        .appendPath(movieID)
                        .build();
                Cursor cursor = getContentResolver().query(movieWithID, null, null, null, null);
                if (cursor != null) {
                    if (returnMovie != null && cursor.getCount() > 0)
                        returnMovie.favorite = true;
                    cursor.close();
                }

                return returnMovie;
            }

            @Override
            public void deliverResult(Movie data) {
                loadedMovie = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Movie> loader, Movie data) {

    }

    @Override
    public void onLoaderReset(Loader<Movie> loader) {

    }
}
