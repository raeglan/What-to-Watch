package de.alfingo.whattowatch;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.alfingo.whattowatch.Utilities.MovieDBUtil;

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
        Uri posterUri = MovieDBUtil.getPictureUri(movie.poster_path, null);
        Picasso.with(context)
                .load(posterUri)
                .into(holder.mMoviePoster);
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
     * The click action for a movie.
     */
    interface GridMovieClickListener {
        /**
         * When a movie is clicked this method is called by the view holder.
         * @param movieID the movie ID for the MovieDB API call with more infos.
         */
        void onClick(int movieID);
    }

    /**
     * A simple ViewHolder for storing the item view for our movies grid.
     */
    class GridMovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
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
            mMovieTitle.setAlpha(0); // TODO: 23.01.2017 See if the title is even needed. Probably not.
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            int movieID = mMovies.get(adapterPosition).id;
            mMovieClickListener.onClick(movieID);
        }
    }
}
