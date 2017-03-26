package de.alfingo.whattowatch;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple RecyclerView Adapter for displaying reviews.
 *
 * @author Rafael
 * @since 26.03.2017
 */
class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private List<Movie.Review> mReviews;

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Movie.Review review = mReviews.get(position);
        holder.mAuthorTextView.setText(review.author);
        holder.mReviewContentTextView.setText(review.content);
    }

    @Override
    public int getItemCount() {
        return mReviews == null ? 0 : mReviews.size();
    }

    /**
     * Swaps the data inside this adapter and notifies the view holder
     * @param reviews the new list with reviews which should be inside this adapter.
     */
    void swapData(List<Movie.Review> reviews) {
        mReviews = reviews;
        notifyDataSetChanged();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_review_author)
        TextView mAuthorTextView;

        @BindView(R.id.tv_review_content)
        TextView mReviewContentTextView;

        ReviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
