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

package com.example.mvvmreactive.presenter;

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;

import com.example.mvvmreactive.interactor.NowPlayingInteractor;
import com.example.mvvmreactive.interactor.NowPlayingResponseModel;
import com.example.mvvmreactive.model.MovieInfo;
import com.example.mvvmreactive.model.MovieViewInfo;
import com.example.mvvmreactive.model.MovieViewInfoImpl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Implements the Presenter interface.
 */
public class NowPlayingPresenterImpl implements NowPlayingPresenter, NowPlayingResponseModel {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    static Observable<List<MovieViewInfo>> requestCache;

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    static int pageNumber = 0;

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    final CompositeDisposable disposables = new CompositeDisposable();

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    final NowPlayingViewModel nowPlayingViewModel;

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    final NowPlayingInteractor nowPlayingInteractor;

    @Inject
    public NowPlayingPresenterImpl(NowPlayingViewModel nowPlayingViewModel,
                                   NowPlayingInteractor nowPlayingInteractor) {
        this.nowPlayingInteractor = nowPlayingInteractor;
        this.nowPlayingViewModel = nowPlayingViewModel;
    }

    @Override
    public void loadMoreInfo() {
        if (requestCache == null) {
            requestCache = nowPlayingInteractor
                    .loadMoreInfo(++pageNumber)
                    .flatMap(new TranslateForPresenterFunction())
                    //subscribe up - translate presenter logic on computation scheduler
                    .subscribeOn(Schedulers.computation())
                    .cache();
        }

        subscribeToData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        nowPlayingInteractor.setNowPlayingResponseModel(this);
        nowPlayingViewModel.showInProgress(true);
        nowPlayingViewModel.createAdapter(savedInstanceState);

        if (savedInstanceState == null) {
            pageNumber = 0;
            requestCache = null;
            loadMoreInfo();
        } else {
            nowPlayingViewModel.restoreState(savedInstanceState);

            //Check to subscriber upon recreation
            if (requestCache != null) {
                subscribeToData();
            }
        }
    }

    @Override
    public void onDestroy() {
        //unsubscribe - Using clear will clear all, but can accept new disposable
        disposables.clear();
    }

    @Override
    public void dataRestored() {
        nowPlayingViewModel.showInProgress(false);
    }

    /**
     * Subscriber to observer data for loading more data.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    void subscribeToData() {
        Disposable disposable = requestCache
                //observe down - update UI on main thread.
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Timber.i("Thread name: %s for class %s",
                                Thread.currentThread().getName(),
                                getClass().getSimpleName().isEmpty()
                                        ? "Presenter Sub Complete" : getClass().getSimpleName());
                        requestCache = null;
                    }
                })
                .subscribe(new UiUpdateConsumer(nowPlayingViewModel),
                        new UiUpdateOnErrorConsumer(this, nowPlayingViewModel));

        //add disposable to list.
        disposables.add(disposable);
    }

    /**
     * Translate the internal business logic to one that the UI understands.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    protected static class TranslateForPresenterFunction
            implements Function<List<MovieInfo>, ObservableSource<List<MovieViewInfo>>> {

        @Override
        public ObservableSource<List<MovieViewInfo>> apply(@NonNull List<MovieInfo> movieInfoList) throws Exception {
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

    /**
     * Update the UI upon a successful subscription for loading more information.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    protected static class UiUpdateConsumer implements Consumer<List<MovieViewInfo>> {
        private final NowPlayingViewModel nowPlayingViewModel;

        public UiUpdateConsumer(NowPlayingViewModel nowPlayingViewModel) {
            this.nowPlayingViewModel = nowPlayingViewModel;
        }

        @Override
        public void accept(@NonNull List<MovieViewInfo> movieViewInfoList) throws Exception {
            Timber.i("Thread name: %s for class %s",
                    Thread.currentThread().getName(),
                    getClass().getSimpleName().isEmpty());
            nowPlayingViewModel.addToAdapter(movieViewInfoList);
            nowPlayingViewModel.showInProgress(false);
        }
    }

    /**
     * Update the UI upon an error subscription from loading more information.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    protected static class UiUpdateOnErrorConsumer implements Consumer<Throwable> {
        private final NowPlayingPresenter nowPlayingPresenter;
        private final NowPlayingViewModel nowPlayingViewModel;

        /**
         * Constructor.
         * @param nowPlayingPresenter -
         * @param nowPlayingViewModel -
         */
        public UiUpdateOnErrorConsumer(NowPlayingPresenter nowPlayingPresenter,
                                       NowPlayingViewModel nowPlayingViewModel) {
            /*
            Note - no weak references here since these are CONSUMER type of objects that are disposed of during
            activity lifecycle events.
             */
            //
            this.nowPlayingPresenter = nowPlayingPresenter;
            this.nowPlayingViewModel = nowPlayingViewModel;
        }

        @Override
        public void accept(@NonNull Throwable throwable) throws Exception {
            Timber.e("Error fetching data (update UI): %s", throwable.toString());

            //Remember, error means completion.
            requestCache = null;

            //show error
            nowPlayingViewModel.showError();

            //try again
            nowPlayingPresenter.loadMoreInfo();
        }
    }

}
