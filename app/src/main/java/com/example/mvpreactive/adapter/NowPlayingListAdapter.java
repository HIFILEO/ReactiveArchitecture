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

package com.example.mvpreactive.adapter;

import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mvpreactive.R;
import com.example.mvpreactive.model.MovieViewInfo;

import java.util.List;

/**
 * Adapter that shows a list of {@link MovieViewInfo} with continuous scrolling.
 */
public class NowPlayingListAdapter extends RecyclerArrayAdapter<MovieViewInfo, BaseViewHolder> {
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ITEM = 1;
    private final LoadMoreScrollListener loadMoreScrollListener;

    /**
     * Constructor.
     *
     * @param objects            - list of {@link MovieViewInfo}
     * @param onLoadMoreListener - listener for when to load more information
     * @param recyclerView       - {@link RecyclerView} using this adapter.
     * @param showLoadMore       - true to show load more spinner, false otherwise.
     */
    public NowPlayingListAdapter(List<MovieViewInfo> objects,
                                 OnLoadMoreListener onLoadMoreListener,
                                 RecyclerView recyclerView,
                                 boolean showLoadMore) {
        super(objects);

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            loadMoreScrollListener =
                    new LoadMoreScrollListener(
                            (LinearLayoutManager) recyclerView.getLayoutManager(),
                            onLoadMoreListener,
                            this
                    );
            recyclerView.addOnScrollListener(loadMoreScrollListener);

            if (showLoadMore) {
                loadMoreScrollListener.setLoading(true);
            }
        } else {
            loadMoreScrollListener = null;
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder baseViewHolder;

        if (viewType == VIEW_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, parent, false);
            baseViewHolder = new MovieViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_item, parent, false);

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
        if (getItemCount() != 0) {
            remove(getItem(getItemCount() - 1));
        }

        for (MovieViewInfo movieViewInfo : movieViewInfoList) {
            add(movieViewInfo);
        }
        loadMoreScrollListener.setLoading(false);
    }

    public void disableLoadMore() {
        loadMoreScrollListener.setOnLoadMoreListener(null);
    }

    public boolean isLoadingMoreShowing() {
        return loadMoreScrollListener.isLoading();
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

    /**
     * Class for handling when the adapter should insert progress item and trigger load more
     * interface.
     */
    @VisibleForTesting
    static class LoadMoreScrollListener extends RecyclerView.OnScrollListener {
        private final LinearLayoutManager linearLayoutManager;
        private final NowPlayingListAdapter nowPlayingListAdapter;
        private OnLoadMoreListener onLoadMoreListener;
        private boolean loading;

        /**
         * Constructor.
         * @param linearLayoutManager -
         * @param onLoadMoreListener -
         * @param nowPlayingListAdapter -
         */
        LoadMoreScrollListener(LinearLayoutManager linearLayoutManager,
                                      OnLoadMoreListener onLoadMoreListener,
                                      NowPlayingListAdapter nowPlayingListAdapter) {
            this.linearLayoutManager = linearLayoutManager;
            this.onLoadMoreListener = onLoadMoreListener;
            this.nowPlayingListAdapter = nowPlayingListAdapter;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int totalItemCount = linearLayoutManager.getItemCount();
            int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

            if (onLoadMoreListener != null
                    && !loading && totalItemCount <= (lastVisibleItem + 2)) {

                //Add spinner
                nowPlayingListAdapter.add(null);

                loading = true;
                onLoadMoreListener.onLoadMore();
            }
        }

        public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
            this.onLoadMoreListener = onLoadMoreListener;
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
        }

        public boolean isLoading() {
            return loading;
        }
    }
}
