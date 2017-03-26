package de.alfingo.whattowatch;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.alfingo.whattowatch.utilities.MovieDBUtil;

/**
 * A simple adapter for populating the Video Views.
 *
 * @author Rafael
 * @since 26.03.2017
 */
class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    private List<Movie.MovieVideo> mMovieVideos;

    /**
     * For handling clicks on the videos.
     */
    private MovieClickListener mClickListener;

    /**
     * Creates an adapter for displaying the trailers and sets the click listener to handle starting
     * the video playback.
     *
     * @param clickListener the activity which will handle the clicks on views
     */
    VideosAdapter(@NonNull MovieClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trailer, parent, false);
        return new VideoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Movie.MovieVideo video = mMovieVideos.get(position);

        holder.mTrailerNameTextView.setText(video.name);

        // setting the thumbnail for the trailer, the color primary light if none is available.
        Context context = holder.itemView.getContext();
        holder.mTrailerThumbnail.setContentDescription(video.name);
        if (Movie.MovieVideo.YOUTUBE.equalsIgnoreCase(video.site)) {
            Uri thumbnailUri = MovieDBUtil.getThumbnailUri(video.site, video.key);
            Picasso.with(context)
                    .load(thumbnailUri)
                    .placeholder(R.color.colorPrimaryLight)
                    .into(holder.mTrailerThumbnail);
        } else
            holder.mTrailerThumbnail.setImageResource(R.color.colorPrimaryLight);

    }

    @Override
    public int getItemCount() {
        return mMovieVideos == null ? 0 : mMovieVideos.size();
    }

    interface MovieClickListener {
        /**
         * Handles a video being clicked.
         *
         * @param videoUri the film Uri to be started.
         */
        void onClick(Uri videoUri);
    }

    /**
     * Swaps and invalidates the data from this adapter
     *
     * @param movieVideos the new videos list
     */
    void swapData(List<Movie.MovieVideo> movieVideos) {
        mMovieVideos = movieVideos;
        notifyDataSetChanged();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.iv_trailer_thumbnail)
        ImageView mTrailerThumbnail;

        @BindView(R.id.tv_trailer_name)
        TextView mTrailerNameTextView;

        VideoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie.MovieVideo video = mMovieVideos.get(adapterPosition);
            if (Movie.MovieVideo.YOUTUBE.equalsIgnoreCase(video.site))
                mClickListener.onClick(MovieDBUtil.getVideoUri(video.site, video.key));
            else
                Toast.makeText(itemView.getContext(),
                        R.string.only_youtube_supported, Toast.LENGTH_SHORT).show();
        }
    }
}
