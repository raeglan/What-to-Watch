package de.alfingo.whattowatch;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements GridMovieAdapter.GridMovieClickListener{

    /**
     * The one class responsible for keeping my app green.
     */
    private RecyclerView mRecyclerView;

    /**
     * For debugging purposes. To remove when not needed.
     */
    private Toast mToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // setting our recycler grid view.
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_main_movies_grid);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setAdapter(new GridMovieAdapter(this));
        mRecyclerView.setLayoutManager(layoutManager);
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
        if(mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, "Movie clicked with ID: " + movieID, Toast.LENGTH_SHORT);
        mToast.show();
        // TODO: 23.01.2017 Perform DB Query for the movie in question and open a new Activity with the detailed view.
    }
}
