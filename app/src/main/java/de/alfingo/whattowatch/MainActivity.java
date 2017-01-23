package de.alfingo.whattowatch;

import android.os.AsyncTask;
import android.os.Bundle;
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

import org.json.JSONException;

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

    private boolean taskRunning;

    /**
     * For debugging purposes. To remove when not needed.
     */
    private Toast mToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // getting all the views from the layout.
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_main_movies_grid);
        mErrorView = (TextView) findViewById(R.id.tv_main_error_msg);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_main_loading);
        taskRunning = false;

        // setting the refresh button
        mErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMovieAdapter.setMovies(null);
                if(!taskRunning)
                    new FetchMoviesTask().execute();
            }
        });

        // setting the recycler view with everything needed.
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        mMovieAdapter = new GridMovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true); // TODO: 23.01.2017 will not be the case after "infinite"(read: multi-page) scrolling is implemented.

        // starting the fetching task
        new FetchMoviesTask().execute();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(int movieID) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, "Movie clicked with ID: " + movieID, Toast.LENGTH_SHORT);
        mToast.show();
        // TODO: 23.01.2017 Perform DB Query for the movie in question and open a new Activity with the detailed view.
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
     * The task responsible for getting the information back from the MovieDB server. Everything
     * is done with the help of the Utilities classes.
     */
    private class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        @Override
        protected void onPreExecute() {
            taskRunning = true;
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            ArrayList<Movie> movies = null;
            try {
                movies = MovieDBUtil.getAllMovies(null, 1);
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
            taskRunning = false;
            mProgressBar.setVisibility(View.INVISIBLE);
            showError(movies == null);
            mMovieAdapter.setMovies(movies);
        }
    }

}
