package de.alfingo.whattowatch;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.alfingo.whattowatch.data.MoviesContract;
import de.alfingo.whattowatch.utilities.MovieDBUtil;

/**
 * The detail movie activity. This only uses the information already present for each movie.
 * No new DB query was needed to fill this page.
 *
 * @author Rafael Miranda
 * @since 24.01.2017
 */
public class MovieDetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Movie>, VideosAdapter.MovieClickListener {

    static final String TAG = MovieDetailsActivity.class.getSimpleName();

    static final int LOADER_ID = 1;

    private static final String MOVIE_ID = "extra-id";

    // all the views
    @BindView(R.id.ratingBar_details)
    RatingBar mRatingBar;

    @BindView(R.id.tv_detail_description)
    TextView mDescriptionTextView;

    @BindView(R.id.tv_detail_rating)
    TextView mRatingTextView;

    @BindView(R.id.tv_detail_release_date)
    TextView mReleaseDateTextView;

    @BindView(R.id.rv_trailers)
    RecyclerView mTrailersRecyclerView;

    @BindView(R.id.rv_reviews)
    RecyclerView mReviewsRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.toolbar_layout_detail)
    CollapsingToolbarLayout mToolbarLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;


    /**
     * Just a hard reference to our target, Picasso only keeps soft references.
     */
    Target mBackgroundTarget;

    /**
     * If the movie is on the favorite list.
     */
    boolean mFavorite;

    /**
     * As the name suggests, the adapter for displaying reviews.
     */
    ReviewsAdapter mReviewsAdapter;

    /**
     * This will display the trailers
     */
    VideosAdapter mVideosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String movieID = getIntent().getAction();

        // toggles between favorite and not favorite
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (movieID != null && !movieID.isEmpty()) {
                    Uri movieWithID = MoviesContract.FavoriteMoviesEntry.CONTENT_URI
                            .buildUpon()
                            .appendPath(movieID)
                            .build();
                    if (mFavorite)
                        getContentResolver().delete(movieWithID, null, null);
                    else
                        getContentResolver().insert(movieWithID, null);
                    mFavorite = !mFavorite;
                    // if it is a favorite fill the heart with joy and love.
                    mFab.setImageResource(mFavorite ? R.drawable.ic_favorite_white_24dp :
                            R.drawable.ic_favorite_border_white_24dp);
                }
            }
        });

        // setting the recycler views, they have both fixed size on the list.
        mReviewsAdapter = new ReviewsAdapter();
        mReviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);
        mReviewsRecyclerView.setHasFixedSize(true);
        // the videos should scroll horizontally.
        mVideosAdapter = new VideosAdapter(this);
        mTrailersRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mTrailersRecyclerView.setAdapter(mVideosAdapter);
        mTrailersRecyclerView.setHasFixedSize(true);

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
        setTitle(data.title);
        mToolbarLayout.setTitle(data.title);

        // sets the heart to be filled when the movie is a favorite.
        mFab.setImageResource(data.favorite ?
                R.drawable.ic_favorite_white_24dp :
                R.drawable.ic_favorite_border_white_24dp);
        mFavorite = data.favorite;

        mDescriptionTextView.setText(data.overview);
        // the average got is in a 10 stars rating
        float voteAverage = data.vote_average / 2;
        mRatingBar.setRating(voteAverage);
        mRatingTextView.setText(getString(R.string.ratings_description,
                voteAverage, data.vote_count));

        String formattedDate = new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
                .format(data.release_date);
        mReleaseDateTextView.setText(formattedDate);


        // loading background image
        mBackgroundTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mToolbarLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
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
                .load(MovieDBUtil.getPictureUri(data.backdrop_path,
                        MovieDBUtil.IMAGE_SIZE_ORIGINAL_PATH))
                .into(mBackgroundTarget);

        // reviews and videos are set now!
        mReviewsAdapter.swapData(data.reviews);
        mVideosAdapter.swapData(data.videos);
    }

    @Override
    public void onLoaderReset(Loader<Movie> loader) {

    }

    @Override
    public void onClick(Uri videoUri) {
        Intent playbackIntent = new Intent(Intent.ACTION_VIEW, videoUri);
        // to avoid crashing if the user has no browser, youtube or lives under a rock.
        if (playbackIntent.resolveActivity(getPackageManager()) != null)
            startActivity(new Intent(Intent.ACTION_VIEW, videoUri));
    }
}
