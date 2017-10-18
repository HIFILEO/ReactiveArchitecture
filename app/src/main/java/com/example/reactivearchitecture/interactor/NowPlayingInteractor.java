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

import android.support.annotation.VisibleForTesting;

import com.example.reactivearchitecture.gateway.ServiceGateway;
import com.example.reactivearchitecture.model.MovieInfo;
import com.example.reactivearchitecture.model.NowPlayingInfo;
import com.example.reactivearchitecture.model.action.Action;
import com.example.reactivearchitecture.model.action.ScrollAction;
import com.example.reactivearchitecture.model.result.Result;
import com.example.reactivearchitecture.model.result.ScrollResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import timber.log.Timber;

/**
 * Interactor for Now Playing movies. Handles internal business logic interactions.
 */
public class NowPlayingInteractor {
    private ServiceGateway serviceGateway;
    private ObservableTransformer<ScrollAction, ScrollResult> transformActionToResult;

    /**
     * Constructor.
     * @param serviceGateway - Gateway to fetch data from.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public NowPlayingInteractor(final ServiceGateway serviceGateway) {
        this.serviceGateway = serviceGateway;

        transformActionToResult = new ObservableTransformer<ScrollAction, ScrollResult>() {
            @Override
            public ObservableSource<ScrollResult> apply(@io.reactivex.annotations.NonNull Observable<ScrollAction> upstream) {
                return upstream.flatMap(new Function<ScrollAction, ObservableSource<ScrollResult>>() {
                    @Override
                    public ObservableSource<ScrollResult> apply(@NonNull final ScrollAction scrollAction) throws Exception {
                        Timber.i("Thread name: %s. Load Data, return ScrollResult.", Thread.currentThread().getName());

                        Observable<Integer> pageNumberObservable = Observable.just(scrollAction.getPageNumber());

                        Observable<List<MovieInfo>> serviceGatewayObservable =
                                serviceGateway.getNowPlaying(scrollAction.getPageNumber())
                                        //Delay for 3 seconds to show spinner on screen.
                                        .delay(3, TimeUnit.SECONDS)
                                        //translate external to internal business logic (Example if we wanted to save to prefs)
                                        .flatMap(new NowPlayingInteractor.MovieListFetcher());

                        //Combine the two observables into result. We need the page number combined w/ results (in case of error).
                        return Observable.zip(
                                pageNumberObservable,
                                serviceGatewayObservable,
                                new BiFunction<Integer, List<MovieInfo>, ScrollResult>() {
                                    @Override
                                    public ScrollResult apply(@NonNull Integer pageNumber, @NonNull List<MovieInfo> movieInfos)
                                            throws Exception {
                                        return ScrollResult.sucess(pageNumber, movieInfos);
                                    }
                                })
                                .onErrorReturn(new Function<Throwable, ScrollResult>() {
                                    @Override
                                    public ScrollResult apply(@NonNull Throwable throwable) throws Exception {
                                        return ScrollResult.failure(scrollAction.getPageNumber(), throwable);
                                    }
                                })
                                .startWith(ScrollResult.inFlight(scrollAction.getPageNumber()));
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
        return actions
                .flatMap(new Function<Action, ObservableSource<ScrollAction>>() {
                    @Override
                    public ObservableSource<ScrollAction> apply(@NonNull Action action) throws Exception {
                        Timber.i("Thread name: %s. Translate Actions into ScrollActions.", Thread.currentThread().getName());
                        return Observable.just((ScrollAction) action);
                    }
                })
                .compose(transformActionToResult)
                .flatMap(new Function<ScrollResult, ObservableSource<Result>>() {
                    @Override
                    public ObservableSource<Result> apply(@NonNull ScrollResult scrollResult) throws Exception {
                        Timber.i("Thread name: %s. Translate ScrollResult into Result.", Thread.currentThread().getName());
                        return Observable.just((Result) scrollResult);
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
