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

package com.example.reactivearchitecture.adapter.nowplaying;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.reactivearchitecture.R;
import com.example.reactivearchitecture.adapter.BaseViewHolder;
import com.example.reactivearchitecture.adapter.RecyclerArrayAdapter;
import com.example.reactivearchitecture.model.MovieViewInfo;

import java.util.List;

/**
 * Adapter that shows a list of {@link MovieViewInfo} with continuous scrolling.
 */
public class NowPlayingListAdapter extends RecyclerArrayAdapter<MovieViewInfo, BaseViewHolder> {
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ITEM = 1;

    /**
     * Constructor.
     *
     * @param objects - list of {@link MovieViewInfo}
     */
    public NowPlayingListAdapter(List<MovieViewInfo> objects) {
        super(objects);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder baseViewHolder = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_ITEM) {
            ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.movie_item, parent, false);
            baseViewHolder = new MovieViewHolder(binding);
        } else {
            View view = layoutInflater.inflate(R.layout.progress_item, parent, false);
            baseViewHolder = new ProgressViewHolder(view);
        }

        return baseViewHolder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        MovieViewInfo item = getItem(position);
        if (item != null) {
            ((MovieViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItem(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    /**
     * Add a list of {@link MovieViewInfo} to the current adapter.
     *
     * @param movieViewInfoList - list to add
     */
    public void addList(List<MovieViewInfo> movieViewInfoList) {
        for (MovieViewInfo movieViewInfo : movieViewInfoList) {
            add(movieViewInfo);
        }
    }
}
