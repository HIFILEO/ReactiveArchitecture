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

package com.example.reactivearchitecture.nowplaying.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.example.reactivearchitecture.core.model.action.Action;
import com.example.reactivearchitecture.nowplaying.controller.ServiceController;
import com.example.reactivearchitecture.nowplaying.model.FilterTransformer;
import com.example.reactivearchitecture.nowplaying.model.MovieInfo;
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfo;
import com.example.reactivearchitecture.nowplaying.model.action.FilterAction;
import com.example.reactivearchitecture.nowplaying.model.action.RestoreAction;
import com.example.reactivearchitecture.nowplaying.model.action.ScrollAction;
import com.example.reactivearchitecture.nowplaying.model.result.RestoreResult;
import com.example.reactivearchitecture.nowplaying.model.result.Result;
import com.example.reactivearchitecture.nowplaying.model.result.ScrollResult;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Interactor for Now Playing movies. Handles internal business logic interactions.
 */
public class NowPlayingInteractor {
    @NonNull
    private final ServiceController serviceController;

    @NonNull
    private final FilterTransformer filterTransformer;

    @NonNull
    private final ObservableTransformer<ScrollAction, ScrollResult> transformScrollActionToScrollResult;

    @NonNull
    private final ObservableTransformer<RestoreAction, RestoreResult> transformRestoreActionToRestoreResult;

    @NonNull
    private final ObservableTransformer<Action, Result> transformActionIntoResults;

    /**
     * Constructor.
     * @param serviceControllerIn - Gateway to fetch data from.
     * @param filterTransformerIn - {@link FilterTransformer}
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public NowPlayingInteractor(@NonNull ServiceController serviceControllerIn, @NonNull FilterTransformer filterTransformerIn) {
        this.serviceController = serviceControllerIn;
        this.filterTransformer = filterTransformerIn;

        transformScrollActionToScrollResult = upstream -> {
            Timber.i("Thread name: %s. Translate ScrollAction into ScrollResult.", Thread.currentThread().getName());

            return upstream.flatMap(scrollAction -> {
                Timber.i("Thread name: %s. Load Data, return ScrollResult.", Thread.currentThread().getName());
                final PublishSubject<ScrollResult> failureStream = PublishSubject.create();

                return serviceController.getNowPlaying(scrollAction.getPageNumber())
                        //Delay for 3 seconds to show spinner on screen.
                        .delay(3, TimeUnit.SECONDS)
                        //translate external to internal business logic (Example if we wanted to save to prefs)
                        .flatMap(new MovieListFetcher())
                        .flatMap(movieInfos -> Observable.just(ScrollResult.sucess(scrollAction.getPageNumber(), movieInfos)))
                        //handle retry, send status on failure stream.
                        .retry((retryNumber, throwable) -> {
                            failureStream.onNext(ScrollResult.failure(scrollAction.getPageNumber(), throwable));
                            return true;
                        })
                        .mergeWith(failureStream)
                        .startWith(ScrollResult.inFlight(scrollAction.getPageNumber()));
            });
        };

        transformRestoreActionToRestoreResult = upstream -> {
            Timber.i("Thread name: %s. Translate RestoreAction into RestoreResult.", Thread.currentThread().getName());

            return upstream.flatMap(restoreAction -> {
                //Set the number of pages to restore
                ArrayList<Integer> pagesToRestore = new ArrayList<>();
                for (int i = 1; i <= restoreAction.getPageNumberToRestore(); i++) {
                    pagesToRestore.add(i);
                }

                final PublishSubject<RestoreResult> failureStream = PublishSubject.create();

                //Execute
                return Observable.fromIterable(pagesToRestore)
                    //output sequence must be ordered. Fetch the 1st page, then second, etc etc.
                    .concatMap(pageNumber -> {
                        Timber.i("Thread name: %s. Restore Page #%s.", Thread.currentThread().getName(), pageNumber);
                        return serviceController.getNowPlaying(pageNumber)
                                .delay(3, TimeUnit.SECONDS)
                                //translate external to internal business logic (Ex if we wanted to save to prefs)
                                .flatMap(new MovieListFetcher())
                                .flatMap(movieInfos -> {
                                    Timber.i("Thread name: %s. Create Restore Results for page %s",
                                            Thread.currentThread().getName(), pageNumber);
                                    if (pageNumber == restoreAction.getPageNumberToRestore()) {
                                        return Observable.just(RestoreResult.sucess(pageNumber, movieInfos));
                                    } else {
                                        return Observable.just(RestoreResult.inFlight(pageNumber, movieInfos));
                                    }
                                })
                                //handle retry, send status on failure stream.
                                .retry((retryNumber, throwable) -> {
                                    failureStream.onNext(
                                            RestoreResult.failure(
                                                    restoreAction.getPageNumberToRestore(),
                                                    true,
                                                    throwable));
                                    return true;
                                })
                                .startWith(RestoreResult.inFlight(pageNumber, null));
                    })
                    .mergeWith(failureStream);
            });
        };

        transformActionIntoResults = upstream -> upstream.publish(new Function<Observable<Action>, ObservableSource<Result>>() {
            @Override
            public ObservableSource<Result> apply(Observable<Action> actionObservable) throws Exception {
                Timber.i("Thread name: %s. Translate Actions into Specific Actions.", Thread.currentThread().getName());

                return Observable.merge(
                        actionObservable.ofType(FilterAction.class),
                        actionObservable.ofType(ScrollAction.class).compose(transformScrollActionToScrollResult),
                        actionObservable.ofType(RestoreAction.class).compose(transformRestoreActionToRestoreResult))
                        .concatMap(object -> processFiltering(object));
            }
        });
    }

    /**
     * Process {@link Action}.
     * @param actions - action to process.
     * @return - {@link Result} of the asynchronous event.
     */
    public Observable<Result> processAction(Observable<Action> actions) {
        return actions.compose(transformActionIntoResults);
    }

    /**
     * Process Filtering for {@link ScrollResult}, {@link RestoreResult}, {@link FilterAction}.
     * @param object - object to apply filtering on.
     * @return {@link Observable} of {@link Result}
     */
    private Observable<Result> processFiltering(Object object) {
        return Observable.just(object)
                .publish(objectObservable -> Observable.merge(
                        objectObservable.ofType(FilterAction.class)
                                .compose(filterTransformer.getTransformFilterActionToFilterResult()),
                        objectObservable.ofType(ScrollResult.class)
                                .compose(filterTransformer.getTransformFilterScrollResult()),
                        objectObservable.ofType(RestoreResult.class)
                                .compose(filterTransformer.getTransformFilterRestoreResult())
                ));
    }

    /**
     * Fetch movies list from {@link NowPlayingInfo}.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    protected static class MovieListFetcher implements Function<NowPlayingInfo, ObservableSource<List<MovieInfo>>> {

        @Override
        public ObservableSource<List<MovieInfo>> apply(@io.reactivex.annotations.NonNull NowPlayingInfo nowPlayingInfo)
                throws Exception {
            Timber.i("Thread name: %s. Translate External Api Data into Business Internal Business Logic Data.",
                    Thread.currentThread().getName());
            return Observable.just(nowPlayingInfo.getMovies());
        }
    }
}
