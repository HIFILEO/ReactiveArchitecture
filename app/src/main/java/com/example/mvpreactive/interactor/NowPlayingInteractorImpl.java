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

package com.example.mvpreactive.interactor;

import android.support.annotation.VisibleForTesting;

import com.example.mvpreactive.dagger.ActivityScope;
import com.example.mvpreactive.gateway.ServiceGateway;
import com.example.mvpreactive.model.MovieInfo;
import com.example.mvpreactive.model.NowPlayingInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import timber.log.Timber;

/**
 * Info Interactor Implementation.
 */
@ActivityScope
public class NowPlayingInteractorImpl implements NowPlayingInteractor {
    private static final int SLEEP_TIME = 3;
    private NowPlayingResponseModel nowPlayingResponseModel;
    private ServiceGateway serviceGateway;
    private int sleepSeconds = SLEEP_TIME;

    @Inject
    public NowPlayingInteractorImpl(ServiceGateway serviceGateway) {
        this.serviceGateway = serviceGateway;
    }

    public void setNowPlayingResponseModel(NowPlayingResponseModel nowPlayingResponseModel) {
        this.nowPlayingResponseModel = nowPlayingResponseModel;
    }

    @Override
    public Observable<List<MovieInfo>> loadMoreInfo(int pageNumber) {
        return serviceGateway.getNowPlaying(pageNumber)
                //Delay for 3 seconds to show spinner on screen.
                .delay(sleepSeconds, TimeUnit.SECONDS)
                .flatMap(new MovieListFetcher());
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    protected void setSleepSeconds(int sleepSeconds) {
        this.sleepSeconds = sleepSeconds;
    }

    /**
     * Fetch movies list from {@link NowPlayingInfo}.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    static class MovieListFetcher implements Function<NowPlayingInfo, ObservableSource<List<MovieInfo>>> {

        @Override
        public ObservableSource<List<MovieInfo>> apply(@NonNull NowPlayingInfo nowPlayingInfo) throws Exception {
            Timber.i("Thread name: %s for class %s",
                    Thread.currentThread().getName(),
                    "Interactor - " + getClass().getSimpleName());
            return Observable.just(nowPlayingInfo.getMovies());
        }
    }
}
