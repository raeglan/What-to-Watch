package de.alfingo.whattowatch;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.alfingo.whattowatch.utilities.MovieDBUtil;

/**
 * The adapter for our movies grid. Uses Picasso for handling images.
 * @author Rafael
 * @since 22.01.2017
 */

class GridMovieAdapter extends RecyclerView.Adapter<GridMovieAdapter.GridMovieViewHolder>{

    private final static String TAG = GridMovieAdapter.class.getSimpleName();

    /**
     * The class responsible for handling clicks on movies.
     */
    private GridMovieClickListener mMovieClickListener;

    /**
     * The array containing all the movies to display.
     */
    private ArrayList<Movie> mMovies;

    /**
     * An constructor for the adapter. Setting the click listener.
     * @param movieClickListener the class handling the clicks.
     */
    GridMovieAdapter(GridMovieClickListener movieClickListener) {
        mMovieClickListener = movieClickListener;
    }

    @Override
    public GridMovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_movie_view_holder, parent, false);
        return new GridMovieViewHolder(itemView);
    }

    /**
     * Bind the Movie title and poster to the view.
     * @param holder the holder which should contain these delicious data
     * @param position the position of the item in the adapter.
     */
    @Override
    public void onBindViewHolder(GridMovieViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        Movie movie = mMovies.get(position);
        holder.mMovieTitle.setText(movie.title);
        // sets the text visible again
        holder.mMovieTitle.setVisibility(View.VISIBLE);
        holder.mMoviePoster.setContentDescription(movie.title);
        Uri posterUri = MovieDBUtil.getPictureUri(movie.poster_path, null);
        Picasso.with(context)
                .load(posterUri)
                .placeholder(R.drawable.ic_main_poster_placeholder)
                .into(holder.mMoviePoster, holder);
    }

    @Override
    public int getItemCount() {
        return mMovies == null? 0 : mMovies.size();
    }

    /**
     * Sets the adapter movies to display the updated information.
     */
    void setMovies(ArrayList<Movie> movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    /**
     * Adds a new set of movies to an already existing list.
     */
    void addMovies(ArrayList<Movie> movies) {
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }

    /**
     * The click action for a movie.
     */
    interface GridMovieClickListener {
        /**
         * When a movie is clicked this method is called by the view holder.
         * @param movieClicked the movie wrapper object with all the infos to prepopulate the
         *                     details page.
         */
        void onClick(Movie movieClicked);
    }

    /**
     * A simple ViewHolder for storing the item view for our movies grid. This implements the
     * OnClickListener so it will be able to handle Clicks and a callback from Picasso to Display
     * the title accordingly.
     */
    class GridMovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            Callback{
        /**
         * White movie title
         */
        TextView mMovieTitle;
        /**
         * Movie poster, with scrim for the text.
         */
        ImageView mMoviePoster;

        GridMovieViewHolder(View itemView) {
            super(itemView);
            mMoviePoster = (ImageView) itemView.findViewById(R.id.iv_item_poster);
            mMovieTitle = (TextView) itemView.findViewById(R.id.tv_item_title);
            itemView.setOnClickListener(this);
        }

        /**
         * If clicked we want to pass the movie, and start the details view.
         * @param v which view was clicked(not used)
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie movieClicked = mMovies.get(adapterPosition);
            mMovieClickListener.onClick(movieClicked);
        }

        /**
         * When the image is successfully loaded we make our Text invisible.
         */
        @Override
        public void onSuccess() {
            mMovieTitle.setVisibility(View.INVISIBLE);
        }

        /** Do nothing */
        @Override
        public void onError() {}
    }
}
