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

package com.example.mvpreactive.presenter;

import android.os.Bundle;

import com.example.mvpreactive.model.MovieViewInfo;

import java.util.List;


/**
 * View interface to be implemented by the forward facing UI part of android. An activity or fragment.
 */
public interface NowPlayingViewModel {

    /**
     * Show main progress spinner.
     * @param show - true to show, false otherwise.
     */
    void showInProgress(boolean show);

    /**
     * Show error message.
     */
    void showError();

    /**
     * Add data to adapter.
     * @param displayList - data to add
     */
    void addToAdapter(List<MovieViewInfo> displayList);

    /**
     * Restore the screen from saved instance.
     * @param savedInstanceState - saved instance state from activity.
     */
    void restoreState(Bundle savedInstanceState);

    /**
     * Create adapter.
     * @param savedInstanceState - saved instance state from activity.
     */
    void createAdapter(Bundle savedInstanceState);
}
