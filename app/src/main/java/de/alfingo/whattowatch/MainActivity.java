package de.alfingo.whattowatch;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonParseException;

import java.io.IOException;
import java.util.ArrayList;

import de.alfingo.whattowatch.Utilities.MovieDBUtil;

public class MainActivity extends AppCompatActivity implements GridMovieAdapter.GridMovieClickListener {

    /**
     * The one class responsible for keeping my app green.
     */
    private RecyclerView mRecyclerView;

    /**
     * For displaying error messages.
     */
    private TextView mErrorView;

    /**
     * Showing that we are loading.
     */
    private ProgressBar mProgressBar;
    /**
     * The adapter for our recycler view
     */
    private GridMovieAdapter mMovieAdapter;

    /**
     * If the loading task is currently running.
     */
    private AsyncTask taskRunning;

    /**
     * For debugging purposes. To remove when not needed.
     */
    private Toast mToast;

    /**
     * The shared preferences file for this app
     */
    private SharedPreferences sharedPreferences;

    /**
     * The key for saving our sort.
     */
    private String KEY_SORT_BY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(getString(R.string.pref_shared_preferences),
                MODE_PRIVATE);
        KEY_SORT_BY = getString(R.string.pref_key_sort);

        // getting all the views from the layout.
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_main_movies_grid);
        mErrorView = (TextView) findViewById(R.id.tv_main_error_msg);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_main_loading);

        // setting the refresh button
        mErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMovieAdapter.setMovies(null);
                if(taskRunning == null || taskRunning.getStatus().equals(AsyncTask.Status.FINISHED))
                    startFetchMoviesTask(-1);
            }
        });

        // setting the recycler view with everything needed.
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        mMovieAdapter = new GridMovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true); // TODO: 23.01.2017 will not be the case after "infinite"(read: multi-page) scrolling is implemented.

        // starting the fetching task
        startFetchMoviesTask(-1);
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
            case R.id.action_main_sort: {
                String[] sortMethods = getResources().getStringArray(R.array.sorting_methods);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setTitle(R.string.sort_by)
                        .setItems(sortMethods, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sharedPreferences.edit().putInt(KEY_SORT_BY, which).apply();
                                startFetchMoviesTask(which);
                            }
                        }).create().show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(Movie movieClicked) {
        Intent detailsIntent = new Intent(this, MovieDetailsActivity.class);
        detailsIntent.putExtra(Movie.KEY_EXTRA_MOVIE, movieClicked);
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
     * @param sortBy the desired sorting method. -1 if default should be used.
     */
    private void startFetchMoviesTask(int sortBy) {
        String[] sortMethods = getResources().getStringArray(R.array.sorting_methods);
        if(sortBy < 0 || sortBy >= sortMethods.length)
            sortBy = sharedPreferences.getInt(KEY_SORT_BY, 0);
        String chosenMethod;
        if (sortMethods[sortBy].equals(getString(R.string.top_rated))) {
            chosenMethod = MovieDBUtil.TOP_PATH;
        } else {
            chosenMethod = MovieDBUtil.POPULAR_PATH;
        }

        mMovieAdapter.setMovies(null);
        if(taskRunning != null)
            taskRunning.cancel(true);
        taskRunning = new FetchMoviesTask().execute(chosenMethod);
    }

    /**
     * The task responsible for getting the information back from the MovieDB server. Everything
     * is done with the help of the Utilities classes.
     */
    private class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            ArrayList<Movie> movies = null;
            try {
                movies = MovieDBUtil.getAllMovies(params[0], 1);
                if (movies != null)
                    for (int i = 2; i < 10; i++)
                        movies.addAll(MovieDBUtil.getAllMovies(null, i));
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO: 23.01.2017 Show different messages for different errors.
                e.printStackTrace();
            }
            return movies;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);
            mProgressBar.setVisibility(View.INVISIBLE);
            showError(movies == null);
            mMovieAdapter.setMovies(movies);
        }
    }

}
