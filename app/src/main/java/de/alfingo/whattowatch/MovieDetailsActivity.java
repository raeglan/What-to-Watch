package de.alfingo.whattowatch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.Locale;

import de.alfingo.whattowatch.Utilities.MovieDBUtil;

/**
 * The detail movie activity. This only uses the information already present for each movie.
 * No new DB query was needed to fill this page.
 * @author Rafael Miranda
 * @since 24.01.2017
 */
public class MovieDetailsActivity extends AppCompatActivity {

    static final String TAG = MovieDetailsActivity.class.getSimpleName();

    /**
     * Movie to display.
     */
    private Movie mMovie;

    /**
     * Just a hard reference to our target, Picasso only keeps soft references.
     */
    Target mBackgroundTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Intent intent = getIntent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.toolbar_layout_detail);

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

        // get the clicked movie and prepopulate our view.
        TextView descriptionView = (TextView) findViewById(R.id.tv_detail_description);
        RatingBar movieRating = (RatingBar) findViewById(R.id.ratingBar_details);
        TextView ratingDescription = (TextView)  findViewById(R.id.tv_detail_rating);
        TextView releaseDate = (TextView) findViewById(R.id.tv_detail_release_date);

        mMovie = (Movie) intent.getSerializableExtra(Movie.KEY_EXTRA_MOVIE);
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
    }
}
