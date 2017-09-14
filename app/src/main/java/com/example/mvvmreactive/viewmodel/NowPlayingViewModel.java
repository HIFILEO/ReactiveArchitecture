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

package com.example.mvvmreactive.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.example.mvvmreactive.gateway.ServiceGateway;
import com.example.mvvmreactive.model.MovieInfo;
import com.example.mvvmreactive.model.MovieViewInfo;
import com.example.mvvmreactive.model.MovieViewInfoImpl;
import com.example.mvvmreactive.model.NowPlayingInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

/**
 * View interface to be implemented by the forward facing UI part of android. An activity or fragment.
 */
public class NowPlayingViewModel extends ViewModel {
    private static final int SLEEP_TIME = 3;
    private int sleepSeconds = SLEEP_TIME;
    private boolean isLoading;
    private int pageNumber = 0;

    @NonNull
    private ServiceGateway serviceGateway;

    @NonNull
    private List<MovieViewInfo> movieViewInfoList = new ArrayList<>();

    @NonNull
    private Map<Integer, Observable<List<MovieViewInfo>>> cacheMap = new HashMap<>();

    @NonNull
    private final BehaviorSubject<Boolean> firstLoadSubject = BehaviorSubject.create();

    /**
     * Constructor. Members are injected.
     * @param serviceGateway -
     */
    @Inject
    public NowPlayingViewModel(@NonNull ServiceGateway serviceGateway) {
        this.serviceGateway = serviceGateway;
        firstLoadSubject.onNext(true);
    }

    /**
     * Is first load in progress.
     * @return - {@link Observable} true when loading, false otherwise.
     */
    @NonNull
    public Observable<Boolean> isFirstLoadInProgress() {
        return firstLoadSubject;
    }

    /**
     * Get {@link List} that the back the {@link android.support.v7.widget.RecyclerView.Adapter}.
     * @return - list of {@link MovieViewInfo}
     */
    @NonNull
    public List<MovieViewInfo> getMovieViewInfoList() {
        return movieViewInfoList;
    }

    /**
     * Load more information to the screen.
     * @return - {@link Observable} or {@link Observable#empty()} if previously loading.
     */
    @NonNull
    public Observable<List<MovieViewInfo>> loadMoreInfo() {
        if (!isLoading) {
            isLoading = true;
            return getMovieViewInfo();
        } else {
            return Observable.empty();
        }
    }

    /**
     * Get the {@link List} that is currently loading.
     * @return - {@link Observable} when {@link NowPlayingViewModel#isFirstLoadInProgress()} is true.
     * {@link Observable#empty()} otherwise.
     */
    @NonNull
    public Observable<List<MovieViewInfo>> getMovieViewInfo() {
        //
        //Return empty if not loading and not first call
        //
        if (!isLoading && pageNumber != 0) {
            Observable.empty();
        }

        //
        //Return Cache if available
        //
        if (cacheMap.containsKey(pageNumber)) {
            return cacheMap.get(pageNumber);
        }

        //
        //Fetch new
        //

        /*
        Although using subjects is an easy solution, a subject cannot handle errors. So to show
        a different type of setup, a cache is being used.
         */
        Observable<List<MovieViewInfo>> observableCache =  serviceGateway
                .getNowPlaying(++pageNumber)
                //Delay for 3 seconds to show spinner on screen.
                .delay(sleepSeconds, TimeUnit.SECONDS)
                //translate external to internal business logic (Example if we wanted to save to prefs)
                .flatMap(new NowPlayingViewModel.MovieListFetcher())
                //translate internal business logic to UI represented
                .flatMap(new TranslateForUiFunction())
                //During error, decrement page number on BGT
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                        Timber.e(throwable);
                        pageNumber--;
                    }
                })
                //subscribe up - do on computation thread.
                .subscribeOn(Schedulers.computation())
                //cache for late use. Below code only runs when someone is subscribed to cache.
                .cache()
                //observe down - update list on main thread to avoid concurrency issues with List<>
                .observeOn(AndroidSchedulers.mainThread())
                //Data coming in should be saved in ViewModel
                .doOnNext(new Consumer<List<MovieViewInfo>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull List<MovieViewInfo> movieViewInfos) throws Exception {
                        movieViewInfoList.addAll(movieViewInfos);

                        if (!movieViewInfos.isEmpty()) {
                            firstLoadSubject.onNext(false);
                        }
                    }
                })
                //Set loading to false when completed or error (aka -terminated)
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        isLoading = false;

                        //clear cache map, value was loaded.
                        cacheMap.remove(pageNumber);
                    }
                });

        //store cache
        cacheMap.put(pageNumber, observableCache);

        return observableCache;
    }

    /**
     * Fetch movies list from {@link NowPlayingInfo}.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    protected static class MovieListFetcher implements Function<NowPlayingInfo, ObservableSource<List<MovieInfo>>> {

        @Override
        public ObservableSource<List<MovieInfo>> apply(@io.reactivex.annotations.NonNull NowPlayingInfo nowPlayingInfo)
                throws Exception {
            Timber.i("Thread name: %s for class %s",
                    Thread.currentThread().getName(),
                    "Interactor - " + getClass().getSimpleName());
            return Observable.just(nowPlayingInfo.getMovies());
        }
    }

    /**
     * Translate the internal business logic to one that the UI understands.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    protected static class TranslateForUiFunction
            implements Function<List<MovieInfo>, ObservableSource<List<MovieViewInfo>>> {

        @Override
        public ObservableSource<List<MovieViewInfo>> apply(@io.reactivex.annotations.NonNull List<MovieInfo> movieInfoList)
                throws Exception {
            /*
            Note - translate internal business logic to presenter logic
            */
            Timber.i("Thread name: %s for class %s",
                    Thread.currentThread().getName(),
                    getClass().getSimpleName());
            List<MovieViewInfo> movieViewInfoList = new ArrayList<>();
            for (MovieInfo movieInfo : movieInfoList) {
                movieViewInfoList.add(new MovieViewInfoImpl(movieInfo));
            }

            return Observable.just(movieViewInfoList);
        }
    }
}
