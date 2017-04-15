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

package com.example.mvpexample.presenter;

import android.support.test.espresso.IdlingResource;

import com.example.mvpexample.interactor.NowPlayingInteractor;
import com.example.mvpexample.model.MovieInfo;

import java.util.List;

/**
 * Espresso needs "hand-made" resources to know when an activity is idle. This is that "hand-made" resource.
 * Since there are multiple threads fetching data, using the presenter as the {@link IdlingResource} made sense.
 */
public class NowPlayingPresenterImpl_IdlingResource extends NowPlayingPresenterImpl implements IdlingResource {
    private ResourceCallback resourceCallback;
    private boolean idle = true;

    public NowPlayingPresenterImpl_IdlingResource(NowPlayingViewModel nowPlayingViewModel, NowPlayingInteractor nowPlayingInteractor) {
        super(nowPlayingViewModel, nowPlayingInteractor);
    }

    @Override
    public String getName() {
        return NowPlayingPresenterImpl_IdlingResource.class.getSimpleName();
    }

    @Override
    public boolean isIdleNow() {
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    @Override
    public void loadMoreInfo() {
        super.loadMoreInfo();
        idle = false;
    }

    @Override
    public void infoLoaded(List<MovieInfo> movieInfoList) {
        super.infoLoaded(movieInfoList);
        idle = true;

        if (resourceCallback != null) {
            //Called when the resource goes from busy to idle.
            resourceCallback.onTransitionToIdle();
        }
    }
}
