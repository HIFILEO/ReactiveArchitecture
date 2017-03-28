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

package example.com.mvpexample.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import example.com.mvpexample.R;
import example.com.mvpexample.model.MovieViewInfo;


/**
 * Simple recycle view adapter with continuous scrolling.
 */
public class NowPlayingListAdapter extends RecyclerArrayAdapter<MovieViewInfo, BaseViewHolder> {
    private OnLoadMoreListener onLoadMoreListener;
    private static final int VIEW_PROGRESS = 0;
    private static final int VIEW_ITEM = 1;

    private boolean loading = false;

    /**
     * Constructor.
     * @param objects - list of {@link MovieViewInfo}
     * @param onLoadMoreListener - listener for when to load more information
     * @param recyclerView - {@link RecyclerView} using this adapter.
     */
    public NowPlayingListAdapter(List<MovieViewInfo> objects,
                                 OnLoadMoreListener onLoadMoreListener,
                                 RecyclerView recyclerView) {
        super(objects);
        this.onLoadMoreListener = onLoadMoreListener;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager)
                    recyclerView.getLayoutManager();

            recyclerView.addOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int totalItemCount = linearLayoutManager.getItemCount();
                    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                    if (NowPlayingListAdapter.this.onLoadMoreListener != null
                            && !loading && totalItemCount <= (lastVisibleItem + 2)) {

                        //Add spinner
                        add(null);

                        loading = true;
                        NowPlayingListAdapter.this.onLoadMoreListener.onLoadMore();
                    }
                }
            });
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
     * @param movieViewInfoList - list to add
     */
    public void addList(List<MovieViewInfo> movieViewInfoList) {
        remove(getItem(getItemCount() -1));

        for (MovieViewInfo movieViewInfo : movieViewInfoList) {
            add(movieViewInfo);
        }
        loading = false;
    }

    public void disableLoadMore() {
       onLoadMoreListener = null;
    }

    public interface OnLoadMoreListener{
        void onLoadMore();
    }
}
