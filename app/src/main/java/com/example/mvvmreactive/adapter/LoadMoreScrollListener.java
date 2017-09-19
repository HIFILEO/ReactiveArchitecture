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

package com.example.mvvmreactive.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Scroll listener that triggers the interface to load more when at the bottom of the list.
 */
public class LoadMoreScrollListener extends RecyclerView.OnScrollListener {
    private final LinearLayoutManager linearLayoutManager;
    private OnLoadMoreListener onLoadMoreListener;

    /**
     * Constructor.
     * @param linearLayoutManager -
     * @param onLoadMoreListener -
     */
    public LoadMoreScrollListener(LinearLayoutManager linearLayoutManager,
                           OnLoadMoreListener onLoadMoreListener) {
        this.linearLayoutManager = linearLayoutManager;
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int totalItemCount = linearLayoutManager.getItemCount();
        int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

        if (onLoadMoreListener != null
                && totalItemCount <= (lastVisibleItem + 2)) {
            onLoadMoreListener.onLoadMore();
        }
    }

    /**
     * Listener when scrolling requires more data to be loaded.
     */
    public interface OnLoadMoreListener {
        /**
         * Load more data for adapter.
         */
        void onLoadMore();
    }
}
