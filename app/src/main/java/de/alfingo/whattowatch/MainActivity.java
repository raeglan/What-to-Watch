package de.alfingo.whattowatch;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonParseException;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindInt;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.alfingo.whattowatch.data.MoviesContract;
import de.alfingo.whattowatch.utilities.EndlessScrollingRecyclerView;
import de.alfingo.whattowatch.utilities.MovieDBUtil;

public class MainActivity extends AppCompatActivity implements GridMovieAdapter.GridMovieClickListener {

    /**
     * The one class responsible for keeping my app green.
     */
    @BindView(R.id.rv_main_movies_grid)
    RecyclerView mRecyclerView;

    /**
     * For displaying error messages.
     */
    @BindView(R.id.tv_main_error_msg)
    TextView mErrorView;

    /**
     * Showing that we are loading.
     */
    @BindView(R.id.pb_main_loading)
    ProgressBar mProgressBar;

    /**
     * The bottom navigation view, used for changing sorting and displaying the favorites.
     */
    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;

    @BindInt(R.integer.favorites_index)
    int FAVORITE_DISPLAY;

    @BindInt(R.integer.top_rated_index)
    int TOP_DISPLAY;

    @BindInt(R.integer.most_popular_index)
    int POPULAR_DISPLAY;

    /**
     * The adapter for our recycler view
     */
    GridMovieAdapter mMovieAdapter;

    /**
     * If the loading task is currently running.
     */
    AsyncTask taskRunning;

    /**
     * The current display, used for avoiding double fetching of movies.
     */
    private int mCurrentDisplay;

    /**
     * The key for saving our sort.
     */
    @BindString(R.string.pref_key_display)
    String KEY_DISPLAY;

    /**
     * The listener here is going to support our bottom navigation.
     */
    BottomNavigationView.OnNavigationItemSelectedListener mItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int selectedDisplay;
                    switch (item.getItemId()) {
                        case R.id.navigation_top_rated:
                            selectedDisplay = TOP_DISPLAY;
                            break;
                        case R.id.navigation_popular:
                            selectedDisplay = POPULAR_DISPLAY;
                            break;
                        case R.id.navigation_favorites:
                            selectedDisplay = FAVORITE_DISPLAY;
                            break;
                        default:
                            throw new UnsupportedOperationException
                                    ("Item not known: " + item.getItemId());
                    }

                    // if we are already in the display selected we just scroll to the top.
                    if (mCurrentDisplay == selectedDisplay && mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.smoothScrollToPosition(0);
                    } else
                        startFetchMoviesTask(selectedDisplay, 1);

                    mCurrentDisplay = selectedDisplay;

                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // setting the bottom navigation listener and the checked item
        mBottomNavigationView.setOnNavigationItemSelectedListener(mItemSelectedListener);
        mCurrentDisplay = TOP_DISPLAY;

        // setting the refresh button
        mErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMovieAdapter.setMovies(null);
                if (taskRunning == null || taskRunning.getStatus().equals(AsyncTask.Status.FINISHED))
                    startFetchMoviesTask(mCurrentDisplay, 1);
            }
        });

        // setting the recycler view with everything needed, we calculate the number of columns we
        // want to display based on the display width.
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 100);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, noOfColumns);

        mMovieAdapter = new GridMovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        EndlessScrollingRecyclerView endlessScrollingListener =
                new EndlessScrollingRecyclerView((GridLayoutManager) layoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        if (mCurrentDisplay != FAVORITE_DISPLAY)
                            startFetchMoviesTask(mCurrentDisplay, page);
                    }
                };
        mRecyclerView.addOnScrollListener(endlessScrollingListener);

        // if we were already somewhere then we want to get back to it.
        if (savedInstanceState != null) {
            // this is the only way to do it for now, Google needs to update the bottom navigation
            // to support rotating the display by default.
            mCurrentDisplay = savedInstanceState.getInt(KEY_DISPLAY, TOP_DISPLAY);
            View selectedDisplayView;
            if(mCurrentDisplay == FAVORITE_DISPLAY)
                selectedDisplayView = findViewById(R.id.navigation_favorites);
            else if(mCurrentDisplay == POPULAR_DISPLAY)
                selectedDisplayView = findViewById(R.id.navigation_popular);
            else
                selectedDisplayView = findViewById(R.id.navigation_top_rated);
            selectedDisplayView.performClick();
        } else
            startFetchMoviesTask(mCurrentDisplay, 1);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_DISPLAY, mCurrentDisplay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Toast.makeText(this, R.string.under_construction, Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(Movie movieClicked) {
        Intent detailsIntent = new Intent(this, MovieDetailsActivity.class);
        detailsIntent.setAction(String.valueOf(movieClicked.id));
        startActivity(detailsIntent);
    }

    /**
     * Toggles between the Grid movies view and the error message.
     *
     * @param errorOccurred If an error has occurred.
     */
    private void showError(boolean errorOccurred) {
        mErrorView.setVisibility(errorOccurred ? View.VISIBLE : View.INVISIBLE);
        mRecyclerView.setVisibility(errorOccurred ? View.INVISIBLE : View.VISIBLE);
    }

    /**
     * Starts a new fetch task with the desired sorting, defined in strings.xml as an array.
     *
     * @param displayType the desired sorting method. -1 if default should be used.
     * @param page        which page should be fetched.
     */
    private void startFetchMoviesTask(int displayType, int page) {
        String[] displayMethods = getResources().getStringArray(R.array.display_methods);
        if (displayType < 0 || displayType >= displayMethods.length) {
            displayType = 0;
        }

        // we only want to reset grid if we just changed display.
        if (page == 1)
            mMovieAdapter.setMovies(null);
        if (taskRunning != null)
            taskRunning.cancel(true);
        taskRunning = new FetchMoviesTask().execute(displayType, page);
    }

    /**
     * The task responsible for getting the information back from the MovieDB server. Everything
     * is done with the help of the Utilities classes.
     */
    private class FetchMoviesTask extends AsyncTask<Integer, Void, ArrayList<Movie>> {

        boolean firstPage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Movie> doInBackground(Integer... params) {
            ArrayList<Movie> movies = null;

            if (params[0] == FAVORITE_DISPLAY) {
                Cursor cursor = getContentResolver().
                        query(MoviesContract.FavoriteMoviesEntry.CONTENT_URI, null,
                                null, null, null);

                if (cursor != null) {
                    int titleIndex = cursor.getColumnIndex(MoviesContract.FavoriteMoviesEntry
                            .COLUMN_TITLE);
                    int movieIDIndex = cursor.getColumnIndex(MoviesContract.FavoriteMoviesEntry
                            .COLUMN_MOVIE_ID);
                    int posterPathIndex = cursor.getColumnIndex(MoviesContract.FavoriteMoviesEntry
                            .COLUMN_POSTER_PATH);
                    movies = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        Movie movie = new Movie();
                        movie.title = cursor.getString(titleIndex);
                        movie.id = cursor.getInt(movieIDIndex);
                        movie.poster_path = cursor.getString(posterPathIndex);
                        movies.add(movie);
                    }
                    cursor.close();
                }
            } else {
                try {
                    int pageIndex = 1;
                    if (params.length > 1)
                        pageIndex = params[1];
                    movies = MovieDBUtil.getAllMovies(MainActivity.this, params[0], pageIndex);
                    firstPage = pageIndex == 1;
                } catch (JsonParseException | IOException e) {
                    e.printStackTrace(); // TODO: 26.03.2017 Show different messages for different errors.
                }
            }

            return movies;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);
            mProgressBar.setVisibility(View.INVISIBLE);

            if (mCurrentDisplay == FAVORITE_DISPLAY && (movies == null || movies.isEmpty())) {
                Toast.makeText(MainActivity.this,
                        R.string.no_favorites, Toast.LENGTH_SHORT).show();
            } else showError(movies == null);

            if (firstPage || mCurrentDisplay == FAVORITE_DISPLAY)
                mMovieAdapter.setMovies(movies);
            else
                mMovieAdapter.addMovies(movies);
        }
    }

}
