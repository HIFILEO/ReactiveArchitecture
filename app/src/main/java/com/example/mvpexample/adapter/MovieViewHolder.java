/*
Copyright 2017 LEO LLC

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.example.mvpexample.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mvpexample.R;
import com.example.mvpexample.adapter.BaseViewHolder;
import com.example.mvpexample.model.MovieViewInfo;
import com.squareup.picasso.Picasso;



import butterknife.Bind;


/**
 * Movie view holder.
 */
public class MovieViewHolder extends BaseViewHolder {
    @Bind(R.id.nameTextView)
    TextView nameTextView;
    @Bind(R.id.moviePosterImageView)
    ImageView moviePosterImageView;
    @Bind(R.id.releaseDateTextView)
    TextView releaseDateTextView;
    @Bind(R.id.ratingTextView)
    TextView ratingTextView;
    @Bind(R.id.highRatingImageView)
    ImageView highRatingImageView;

    public MovieViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * Bind the {@link MovieViewInfo} to the view holder.
     * @param movieViewInfo - data to bind with.
     */
    public void bind(MovieViewInfo movieViewInfo) {
        nameTextView.setText(movieViewInfo.getTitle());
        releaseDateTextView.setText(movieViewInfo.getReleaseDate());
        ratingTextView.setText(movieViewInfo.getRating());

        //Rating
        if (movieViewInfo.isHighRating()) {
            highRatingImageView.setVisibility(View.VISIBLE);
        } else {
            highRatingImageView.setVisibility(View.GONE);
        }

        //Picasso
        Picasso.with(moviePosterImageView.getContext())
                .load(movieViewInfo.getPictureUrl())
                .into(moviePosterImageView);
    }
}
