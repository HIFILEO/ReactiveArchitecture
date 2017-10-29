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

package com.example.reactivearchitecture.interactor;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.example.reactivearchitecture.gateway.ServiceController;
import com.example.reactivearchitecture.model.FilterTransformer;
import com.example.reactivearchitecture.model.MovieInfo;
import com.example.reactivearchitecture.model.NowPlayingInfo;
import com.example.reactivearchitecture.model.action.Action;
import com.example.reactivearchitecture.model.action.FilterAction;
import com.example.reactivearchitecture.model.action.RestoreAction;
import com.example.reactivearchitecture.model.action.ScrollAction;
import com.example.reactivearchitecture.model.result.RestoreResult;
import com.example.reactivearchitecture.model.result.Result;
import com.example.reactivearchitecture.model.result.ScrollResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.BiPredicate;
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

        transformScrollActionToScrollResult = new ObservableTransformer<ScrollAction, ScrollResult>() {
            @Override
            public ObservableSource<ScrollResult> apply(@io.reactivex.annotations.NonNull Observable<ScrollAction> upstream) {
                Timber.i("Thread name: %s. Translate ScrollAction into ScrollResult.", Thread.currentThread().getName());

                return upstream.flatMap(new Function<ScrollAction, ObservableSource<ScrollResult>>() {
                    @Override
                    public ObservableSource<ScrollResult> apply(@NonNull final ScrollAction scrollAction) throws Exception {
                        Timber.i("Thread name: %s. Load Data, return ScrollResult.", Thread.currentThread().getName());
                        final PublishSubject<ScrollResult> failureStream = PublishSubject.create();

                        return serviceController.getNowPlaying(scrollAction.getPageNumber())
                                //Delay for 3 seconds to show spinner on screen.
                                .delay(3, TimeUnit.SECONDS)
                                //translate external to internal business logic (Example if we wanted to save to prefs)
                                .flatMap(new NowPlayingInteractor.MovieListFetcher())
                                .flatMap(new Function<List<MovieInfo>, ObservableSource<ScrollResult>>() {
                                    @Override
                                    public ObservableSource<ScrollResult> apply(List<MovieInfo> movieInfos) throws Exception {
                                        return Observable.just(ScrollResult.sucess(scrollAction.getPageNumber(), movieInfos));
                                    }
                                })
                                //handle retry, send status on failure stream.
                                .retry(new BiPredicate<Integer, Throwable>() {
                                    @Override
                                    public boolean test(Integer retryNumber, Throwable throwable) throws Exception {
                                        failureStream.onNext(ScrollResult.failure(scrollAction.getPageNumber(), throwable));
                                        return true;
                                    }
                                })
                                .mergeWith(failureStream)
                                .startWith(ScrollResult.inFlight(scrollAction.getPageNumber()));
                    }
                });
            }
        };

        transformRestoreActionToRestoreResult = new ObservableTransformer<RestoreAction, RestoreResult>() {
            @Override
            public ObservableSource<RestoreResult> apply(@io.reactivex.annotations.NonNull Observable<RestoreAction> upstream) {
                Timber.i("Thread name: %s. Translate RestoreAction into RestoreResult.", Thread.currentThread().getName());

                return upstream.flatMap(new Function<RestoreAction, ObservableSource<RestoreResult>>() {
                    @Override
                    public ObservableSource<RestoreResult> apply(final RestoreAction restoreAction) throws Exception {
                        //Set the number of pages to restore
                        ArrayList<Integer> pagesToRestore = new ArrayList<>();
                        for (int i = 1; i <= restoreAction.getPageNumberToRestore(); i++) {
                            pagesToRestore.add(i);
                        }

                        final PublishSubject<RestoreResult> failureStream = PublishSubject.create();

                        //Execute
                        return Observable.fromIterable(pagesToRestore)
                            //output sequence must be ordered. Fetch the 1st page, then second, etc etc.
                            .concatMap(new Function<Integer, ObservableSource<RestoreResult>>() {
                                @Override
                                public ObservableSource<RestoreResult> apply(final Integer pageNumber) throws Exception {
                                    Timber.i("Thread name: %s. Restore Page #%s.", Thread.currentThread().getName(), pageNumber);
                                    return serviceController.getNowPlaying(pageNumber)
                                            .delay(3, TimeUnit.SECONDS)
                                            //translate external to internal business logic (Ex if we wanted to save to prefs)
                                            .flatMap(new NowPlayingInteractor.MovieListFetcher())
                                            .flatMap(new Function<List<MovieInfo>, ObservableSource<RestoreResult>>() {
                                                @Override
                                                public ObservableSource<RestoreResult> apply(List<MovieInfo> movieInfos)
                                                        throws Exception {
                                                    Timber.i("Thread name: %s. Create Restore Results for page %s",
                                                            Thread.currentThread().getName(), pageNumber);
                                                    if (pageNumber == restoreAction.getPageNumberToRestore()) {
                                                        return Observable.just(RestoreResult.sucess(pageNumber, movieInfos));
                                                    } else {
                                                        return Observable.just(RestoreResult.inFlight(pageNumber, movieInfos));
                                                    }
                                                }
                                            })
                                            //handle retry, send status on failure stream.
                                            .retry(new BiPredicate<Integer, Throwable>() {
                                                @Override
                                                public boolean test(Integer retryNumber, Throwable throwable) throws Exception {
                                                    failureStream.onNext(
                                                            RestoreResult.failure(
                                                                    restoreAction.getPageNumberToRestore(),
                                                                    true,
                                                                    throwable));
                                                    return true;
                                                }
                                            })
                                            .startWith(RestoreResult.inFlight(pageNumber, null));
                                }
                            })
                            .mergeWith(failureStream);
                    }
                });
            }
        };

        transformActionIntoResults = new ObservableTransformer<Action, Result>() {
            @Override
            public ObservableSource<Result> apply(Observable<Action> upstream) {
                return upstream.publish(new Function<Observable<Action>, ObservableSource<Result>>() {
                    @Override
                    public ObservableSource<Result> apply(Observable<Action> actionObservable) throws Exception {
                        Timber.i("Thread name: %s. Translate Actions into Specific Actions.", Thread.currentThread().getName());

                        return Observable.merge(
                                actionObservable.ofType(FilterAction.class),
                                actionObservable.ofType(ScrollAction.class).compose(transformScrollActionToScrollResult),
                                actionObservable.ofType(RestoreAction.class).compose(transformRestoreActionToRestoreResult))
                                .concatMap(new Function<Object, ObservableSource<Result>>() {
                                    @Override
                                    public ObservableSource<Result> apply(Object object) throws Exception {
                                        return processFiltering(object);
                                    }
                                });
                    }
                });
            }
        };
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
                .publish(new Function<Observable<Object>, ObservableSource<Result>>() {
                    @Override
                    public ObservableSource<Result> apply(Observable<Object> objectObservable) throws Exception {
                        return Observable.merge(
                                objectObservable.ofType(FilterAction.class)
                                        .compose(filterTransformer.getTransformFilterActionToFilterResult()),
                                objectObservable.ofType(ScrollResult.class)
                                        .compose(filterTransformer.getTransformFilterScrollResult()),
                                objectObservable.ofType(RestoreResult.class)
                                        .compose(filterTransformer.getTransformFilterRestoreResult())
                        );
                    }
                });
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
