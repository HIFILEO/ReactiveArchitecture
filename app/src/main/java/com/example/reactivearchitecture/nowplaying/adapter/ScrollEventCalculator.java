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

package com.example.reactivearchitecture.nowplaying.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding2.support.v7.widget.RecyclerViewScrollEvent;

/**
 * Class for handling when {@link RecyclerView} hits the bottom of a scroll. Only works
 * for {@link LinearLayoutManager}.
 */
public class ScrollEventCalculator {
    private RecyclerViewScrollEvent recyclerViewScrollEvent;

    public ScrollEventCalculator(RecyclerViewScrollEvent recyclerViewScrollEvent) {
        this.recyclerViewScrollEvent = recyclerViewScrollEvent;
    }

    /**
     * Determine if the scroll event at the end of the recycler view.
     * @return true if at end of linear list recycler view, false otherwise.
     */
    public boolean isAtScrollEnd() {
        RecyclerView.LayoutManager layoutManager =
                recyclerViewScrollEvent.view().getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            int totalItemCount = linearLayoutManager.getItemCount();
            int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

            return totalItemCount <= (lastVisibleItem + 2);
        } else {
            return false;
        }
    }
}
