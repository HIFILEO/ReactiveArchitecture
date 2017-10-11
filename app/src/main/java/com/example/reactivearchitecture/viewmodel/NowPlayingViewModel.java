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

package com.example.reactivearchitecture.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.example.reactivearchitecture.R;
import com.example.reactivearchitecture.gateway.ServiceGateway;
import com.example.reactivearchitecture.model.AdapterUiCommand;
import com.example.reactivearchitecture.model.LoadMoreCommand;
import com.example.reactivearchitecture.model.MovieInfo;
import com.example.reactivearchitecture.model.MovieViewInfo;
import com.example.reactivearchitecture.model.MovieViewInfoImpl;
import com.example.reactivearchitecture.model.NowPlayingInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

import static com.example.reactivearchitecture.model.LoadMoreCommand.LoadMoreType.RESTORE;
import static com.example.reactivearchitecture.model.LoadMoreCommand.LoadMoreType.SCROLL;

/**
 * View interface to be implemented by the forward facing UI part of android. An activity or fragment.
 */
public class NowPlayingViewModel extends ViewModel {
    private static final int SLEEP_TIME = 3;
    private int sleepSeconds = SLEEP_TIME;
    private int pageNumber = 0;

    @NonNull
    private Application application;

    @NonNull
    private ServiceGateway serviceGateway;

    @NonNull
    private List<MovieViewInfo> movieViewInfoList = new ArrayList<>();

    @NonNull
    private final ObservableField<String> toolbarTitle = new ObservableField<>();

    @NonNull
    private final ObservableBoolean firstLoad = new ObservableBoolean(true);

    @NonNull
    private final BehaviorSubject<Boolean> loadMoreEnabledBehaviorSubject = BehaviorSubject.create();

    @NonNull
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @NonNull
    private final PublishSubject<AdapterUiCommand> adapterDataPublishSubject = PublishSubject.create();

    @NonNull
    private final PublishSubject<LoadMoreCommand> loadMorePublishSubject = PublishSubject.create();

    @NonNull
    private final PublishSubject<Boolean> showErrorPublishSubject = PublishSubject.create();

    /**
     * Constructor. Members are injected.
     * @param application -
     * @param serviceGateway -
     */
    @Inject
    public NowPlayingViewModel(@NonNull Application application, @NonNull ServiceGateway serviceGateway) {
        this.serviceGateway = serviceGateway;
        this.application = application;
        toolbarTitle.set(application.getString(R.string.now_playing));
        loadMoreEnabledBehaviorSubject.onNext(true);

        bind();
        loadMorePublishSubject.onNext(new LoadMoreCommand(RESTORE));
    }

    @Override
    public void onCleared() {
        super.onCleared();

        //unbind all
        compositeDisposable.clear();
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
     * Load more Now Playing movies.
     */
    public void loadMoreNowPlayingInfo() {
       loadMorePublishSubject.onNext(new LoadMoreCommand(SCROLL));
    }

    @NonNull
    public Observable<Boolean> getShowErrorObserver() {
        return showErrorPublishSubject;
    }

    @NonNull
    public ObservableField<String> getToolbarTitle() {
        return toolbarTitle;
    }

    @NonNull
    public ObservableBoolean getFirstLoad() {
        return firstLoad;
    }

    @NonNull
    public BehaviorSubject<Boolean> getLoadMoreEnabledBehaviorSubject() {
            return loadMoreEnabledBehaviorSubject;
    }

    @NonNull
    public PublishSubject<AdapterUiCommand> getAdapterDataPublishSubject() {
        return adapterDataPublishSubject;
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private void bind() {
        //bind on main UI thread
        compositeDisposable.add(loadMorePublishSubject
                //Filter any multiple on scrolls that happened before 250MS
                .debounce(new Function<LoadMoreCommand, ObservableSource<LoadMoreCommand>>() {
                    @Override
                    public ObservableSource<LoadMoreCommand> apply(
                            @io.reactivex.annotations.NonNull LoadMoreCommand loadMoreCommand) throws Exception {
                        if (loadMoreCommand.getLoadMoreType() == SCROLL) {
                            return Observable.<LoadMoreCommand>empty().delay(250, TimeUnit.MILLISECONDS);
                        } else {
                            return Observable.empty();
                        }
                    }
                })
                //
                //UPDATE UI -
                //
                .doOnNext(new Consumer<LoadMoreCommand>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull LoadMoreCommand loadMoreCommand) throws Exception {
                        //
                        //Update VM UI
                        //
                        loadMoreEnabledBehaviorSubject.onNext(false);

                        if (loadMoreCommand.getLoadMoreType() == SCROLL) {
                            adapterDataPublishSubject.onNext(AdapterUiCommand.createAddNull());
                            movieViewInfoList.add(null);
                        }
                    }
                })
                //observe down - fetch list on computation thread
                .observeOn(Schedulers.computation())
                //
                //Fetch Data
                //
                .flatMap(new Function<LoadMoreCommand, ObservableSource<NowPlayingInfo>>() {
                     @Override
                    public ObservableSource<NowPlayingInfo>  apply(
                            @io.reactivex.annotations.NonNull LoadMoreCommand loadMoreCommand) throws Exception {
                         return serviceGateway.getNowPlaying(++pageNumber);
                    }
                })
                //Delay for 3 seconds to show spinner on screen.
                .delay(sleepSeconds, TimeUnit.SECONDS)
                //translate external to internal business logic (Example if we wanted to save to prefs)
                .flatMap(new NowPlayingViewModel.MovieListFetcher())
                //translate internal business logic to UI represented
                .flatMap(new TranslateForUiFunction())
                //error with fetching data, reset page number
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                        pageNumber--;
                    }
                })
                //
                //UPDATE UI -
                //
                //observe down - handle subscription and updateUI on main thread
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<MovieViewInfo>>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull List<MovieViewInfo> movieViewInfos) throws Exception {
                        //remove null spinner (if present)
                        if (!firstLoad.get()) {
                            movieViewInfoList.remove(movieViewInfoList.size() - 1);
                            adapterDataPublishSubject.onNext(AdapterUiCommand.createRemoveNull());
                        }
                    }
                })
                .onErrorResumeNext(new ObservableSource<List<MovieViewInfo>>() {
                    @Override
                    public void subscribe(@io.reactivex.annotations.NonNull Observer<? super List<MovieViewInfo>> observer) {
                        //Error occurred but don't disengage from subject

                        //show error
                        showErrorPublishSubject.onNext(true);

                        //try again
                        loadMorePublishSubject.onNext(new LoadMoreCommand(SCROLL));
                    }
                })
                .subscribe(
                        //On-Next
                        new Consumer<List<MovieViewInfo>>() {
                               @Override
                               public void accept(
                                       @io.reactivex.annotations.NonNull List<MovieViewInfo> movieViewInfos) throws Exception {
                                   //add new data
                                   movieViewInfoList.addAll(movieViewInfos);
                                   adapterDataPublishSubject.onNext(
                                           new AdapterUiCommand(AdapterUiCommand.CommandType.ADD, movieViewInfos));

                                   //address first load
                                   if (!movieViewInfos.isEmpty()) {
                                       firstLoad.set(false);
                                   }

                                   //enable scroll
                                   if (!movieViewInfoList.isEmpty()) {
                                       loadMoreEnabledBehaviorSubject.onNext(true);
                                   }
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                                   throw new RuntimeException("Subject Crashed!");
                               }
                           }));
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
