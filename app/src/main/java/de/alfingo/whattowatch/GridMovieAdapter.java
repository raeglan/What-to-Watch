package de.alfingo.whattowatch;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The adapter for our movies grid. Uses Picasso for handling images.
 * @author Rafael
 * @since 22.01.2017
 */

public class GridMovieAdapter extends RecyclerView.Adapter<GridMovieAdapter.GridMovieViewHolder>{

    /**
     * The class responsible for handling clicks on movies.
     */
    GridMovieClickListener mMovieClickListener;

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

    @Override
    public void onBindViewHolder(GridMovieViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
        }

        @Override
        public void onClick(View v) {

        }
    }
}
