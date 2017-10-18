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
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.example.reactivearchitecture.R;
import com.example.reactivearchitecture.gateway.ServiceGateway;
import com.example.reactivearchitecture.interactor.NowPlayingInteractor;
import com.example.reactivearchitecture.model.MovieInfo;
import com.example.reactivearchitecture.model.MovieViewInfo;
import com.example.reactivearchitecture.model.MovieViewInfoImpl;
import com.example.reactivearchitecture.model.UiModel;
import com.example.reactivearchitecture.model.action.Action;
import com.example.reactivearchitecture.model.action.ScrollAction;
import com.example.reactivearchitecture.model.event.ScrollEvent;
import com.example.reactivearchitecture.model.event.UiEvent;
import com.example.reactivearchitecture.model.result.Result;
import com.example.reactivearchitecture.model.result.ScrollResult;
import com.jakewharton.rxrelay2.PublishRelay;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * View interface to be implemented by the forward facing UI part of android. An activity or fragment.
 */
public class NowPlayingViewModel extends ViewModel {
    private NowPlayingInteractor nowPlayingInteractor;
    private Observable<UiModel> uiModelObservable;

    @NonNull
    private Application application;

    @NonNull
    private ServiceGateway serviceGateway;

    //Note - left here to show example of android data binding.
    @NonNull
    private final ObservableField<String> toolbarTitle = new ObservableField<>();

    @NonNull
    private PublishRelay<UiEvent> publishRelayUiEvents = PublishRelay.create();

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

        nowPlayingInteractor = new NowPlayingInteractor(serviceGateway);
        bind();
    }

    /**
     * Process events from the UI.
     * @param uiEvent - {@link UiEvent}
     */
    public void processUiEvent(UiEvent uiEvent) {
        Timber.i("Thread name: %s. Process UiEvent", Thread.currentThread().getName());
        publishRelayUiEvents.accept(uiEvent);
    }

    /**
     * Get the tool bar title.
     * @return - {@link ObservableField} that contains string for toolbar.
     */
    @NonNull
    public ObservableField<String> getToolbarTitle() {
        return toolbarTitle;
    }

    @NonNull
    public Observable<UiModel> getUiModels() {
        return uiModelObservable;
    }

    /**
     * Bind to {@link PublishRelay}.
     */
    private void bind() {
        uiModelObservable = publishRelayUiEvents
                .observeOn(Schedulers.computation())
                //Translate UiEvents into Actions
                .flatMap(new Function<UiEvent, ObservableSource<Action>>() {
                    @Override
                    public ObservableSource<Action> apply(@io.reactivex.annotations.NonNull UiEvent uiEvent) throws Exception {
                        Timber.i("Thread name: %s. Translate UiEvents into Actions.", Thread.currentThread().getName());
                        ScrollAction scrollAction = new ScrollAction(((ScrollEvent) uiEvent).getPageNumber());
                        return Observable.just((Action) scrollAction);
                    }
                })
                //Asynchronous Actions To Interactor
                .publish(new Function<Observable<Action>, ObservableSource<Result>>() {
                    @Override
                    public ObservableSource<Result> apply(@io.reactivex.annotations.NonNull Observable<Action> actionObservable)
                            throws Exception {
                        return  nowPlayingInteractor.processAction(actionObservable);
                    }
                })
                //Scan Results to Update UiModel
                .scan(UiModel.INITIAL_STATE, new BiFunction<UiModel, Result, UiModel>() {
                    @Override
                    public UiModel apply(UiModel uiModel, Result result) throws Exception {
                        Timber.i("Thread name: %s. Scan Results to UiModel", Thread.currentThread().getName());

                        ScrollResult scrollResult = (ScrollResult) result;

                        if (result.getType() == Result.ResultType.IN_FLIGHT) {
                            return UiModel.inProgressState(
                                    scrollResult.getPageNumber() == 1,
                                    scrollResult.getPageNumber(),
                                    uiModel.getCurrentList());
                        } else if (result.getType() == Result.ResultType.SUCCESS) {
                            List<MovieViewInfo> listToAdd = translateResultsForUi(scrollResult.getResult());
                            uiModel.getCurrentList().addAll(listToAdd);

                            return UiModel.successState(
                                    scrollResult.getPageNumber(),
                                    uiModel.getCurrentList(),
                                    listToAdd
                            );
                        } else if (result.getType() == Result.ResultType.FAILURE) {
                            Timber.e(scrollResult.getError());
                            return UiModel.failureState(
                                    scrollResult.getPageNumber() - 1,
                                    uiModel.getCurrentList(),
                                    application.getString(R.string.error_msg)
                            );
                        }

                        //Unknown result - throw error
                        throw new IllegalArgumentException("Unknown Result: " + result);
                    }
                })
                //Publish results to main thread.
                .observeOn(AndroidSchedulers.mainThread())
                //Save history for late subscribers.
                .replay(1)
                .autoConnect();
    }

    /**
     * Translate internal business logic to presenter logic.
     * @param movieInfoList - business list.
     * @return - translated list ready for UI
     */
    @WorkerThread
    private List<MovieViewInfo> translateResultsForUi(List<MovieInfo> movieInfoList) {
        List<MovieViewInfo> movieViewInfoList = new ArrayList<>();
        for (MovieInfo movieInfo : movieInfoList) {
            movieViewInfoList.add(new MovieViewInfoImpl(movieInfo));
        }

        return movieViewInfoList;
    }
}
