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
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;

import com.example.reactivearchitecture.R;
import com.example.reactivearchitecture.gateway.ServiceGateway;
import com.example.reactivearchitecture.interactor.NowPlayingInteractor;
import com.example.reactivearchitecture.model.AdapterCommandType;
import com.example.reactivearchitecture.model.MovieInfo;
import com.example.reactivearchitecture.model.MovieViewInfo;
import com.example.reactivearchitecture.model.MovieViewInfoImpl;
import com.example.reactivearchitecture.model.UiModel;
import com.example.reactivearchitecture.model.action.Action;
import com.example.reactivearchitecture.model.action.RestoreAction;
import com.example.reactivearchitecture.model.action.ScrollAction;
import com.example.reactivearchitecture.model.event.RestoreEvent;
import com.example.reactivearchitecture.model.event.ScrollEvent;
import com.example.reactivearchitecture.model.event.UiEvent;
import com.example.reactivearchitecture.model.result.RestoreResult;
import com.example.reactivearchitecture.model.result.Result;
import com.example.reactivearchitecture.model.result.ScrollResult;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * View interface to be implemented by the forward facing UI part of android. An activity or fragment.
 */
public class NowPlayingViewModel extends ViewModel {
    private Observable<UiModel> uiModelObservable;
    private UiModel initialUiModel;
    private Observable<UiEvent> startEventsObservable;

    @NonNull
    private Application application;

    @NonNull
    private ServiceGateway serviceGateway;

    //Note - left here to show example of android data binding.
    @NonNull
    private final ObservableField<String> toolbarTitle = new ObservableField<>();

    @NonNull
    private PublishRelay<UiEvent> publishRelayUiEvents = PublishRelay.create();

    @NonNull
    private ObservableTransformer<UiEvent, Action> transformEventsIntoActions;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected NowPlayingInteractor nowPlayingInteractor;

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
        createNowPlayingInteractor();
        setupTransformers();
    }

    /**
     * Init the view model using the last saved UiModel.
     * <p>
     * Can only be called once. Must be called before {@link NowPlayingViewModel#processUiEvent(UiEvent)}.
     * </p>
     * @param restoredUiModel - model to restore, or null.
     */
    public void init(@Nullable  UiModel restoredUiModel) {
        if (initialUiModel == null) {
            if (restoredUiModel == null) {
                initialUiModel = UiModel.initState();
                startEventsObservable = Observable.just((UiEvent) new ScrollEvent(initialUiModel.getPageNumber() + 1));
            } else {
                //restore required
                initialUiModel = UiModel.restoreState(restoredUiModel.getPageNumber(), new ArrayList<MovieViewInfo>(), null);
                startEventsObservable = Observable.just((UiEvent) new RestoreEvent(initialUiModel.getPageNumber()));
            }
            bind();
        }
    }

    /**
     * Process events from the UI.
     * @param uiEvent - {@link UiEvent}
     */
    public void processUiEvent(UiEvent uiEvent) {
        //
        //Guard
        //
        if (uiModelObservable == null) {
            throw new IllegalStateException("Model Observer not ready. Did you forget to call init()?");
        }

        //
        //Process UiEvent
        //
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected void createNowPlayingInteractor() {
        nowPlayingInteractor =  new NowPlayingInteractor(serviceGateway);
    }

    /**
     * Bind to {@link PublishRelay}.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected void bind() {
        uiModelObservable = publishRelayUiEvents
                .observeOn(Schedulers.computation())
                //Merge with start events
                .mergeWith(startEventsObservable)
                //Translate UiEvents into Actions
                .compose(transformEventsIntoActions)
                //Asynchronous Actions To Interactor
                .publish(new Function<Observable<Action>, ObservableSource<Result>>() {
                    @Override
                    public ObservableSource<Result> apply(@io.reactivex.annotations.NonNull Observable<Action> actionObservable)
                            throws Exception {
                        return  nowPlayingInteractor.processAction(actionObservable);
                    }
                })
                //Scan Results to Update UiModel
                .scan(UiModel.initState(), new BiFunction<UiModel, Result, UiModel>() {
                    @Override
                    public UiModel apply(UiModel uiModel, Result result) throws Exception {
                        Timber.i("Thread name: %s. Scan Results to UiModel", Thread.currentThread().getName());

                        if (result instanceof ScrollResult) {
                            ScrollResult scrollResult = (ScrollResult) result;

                            if (result.getType() == Result.ResultType.IN_FLIGHT) {
                                return UiModel.inProgressState(
                                        scrollResult.getPageNumber() == 1,
                                        scrollResult.getPageNumber(),
                                        uiModel.getCurrentList()
                                );
                            } else if (result.getType() == Result.ResultType.SUCCESS) {
                                List<MovieViewInfo> listToAdd = translateResultsForUi(scrollResult.getResult());
                                List<MovieViewInfo> currentList = uiModel.getCurrentList();
                                currentList.addAll(listToAdd);

                                return UiModel.successState(
                                        scrollResult.getPageNumber(),
                                        currentList,
                                        listToAdd,
                                        AdapterCommandType.ADD_DATA_REMOVE_IN_PROGRESS
                                );
                            } else if (result.getType() == Result.ResultType.FAILURE) {
                                Timber.e(scrollResult.getError());
                                return UiModel.failureState(
                                        scrollResult.getPageNumber() == 1,
                                        scrollResult.getPageNumber() - 1,
                                        uiModel.getCurrentList(),
                                        application.getString(R.string.error_msg)
                                );
                            }

                        } else {
                            RestoreResult restoreResult = (RestoreResult) result;

                            List<MovieViewInfo> listToAdd = null;
                            List<MovieViewInfo> currentList = uiModel.getCurrentList();
                            if (restoreResult.getResult() != null && !restoreResult.getResult().isEmpty()) {
                                listToAdd = translateResultsForUi(restoreResult.getResult());
                                currentList.addAll(listToAdd);
                            }

                            if (result.getType() == Result.ResultType.IN_FLIGHT) {
                                return UiModel.restoreState(
                                        restoreResult.getPageNumber(),
                                        currentList,
                                        listToAdd
                                );
                            } else if (result.getType() == Result.ResultType.SUCCESS) {
                                return UiModel.successState(
                                        restoreResult.getPageNumber(),
                                        currentList,
                                        listToAdd,
                                        AdapterCommandType.ADD_DATA_ONLY
                                );
                            } else if (result.getType() == Result.ResultType.FAILURE) {
                                Timber.e(restoreResult.getError());
                                return UiModel.failureState(
                                        true,
                                        restoreResult.getPageNumber() - 1,
                                        currentList,
                                        application.getString(R.string.error_msg)
                                );
                            }
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

    private void setupTransformers() {
        final ObservableTransformer<ScrollEvent, ScrollAction> scrollTransformer =
                new ObservableTransformer<ScrollEvent, ScrollAction>() {
            @Override
            public ObservableSource<ScrollAction> apply(Observable<ScrollEvent> upstream) {
                return upstream.flatMap(new Function<ScrollEvent, ObservableSource<ScrollAction>>() {
                    @Override
                    public ObservableSource<ScrollAction> apply(ScrollEvent scrollEvent) throws Exception {
                        return Observable.just(new ScrollAction(scrollEvent.getPageNumber()));
                    }
                });
            }
        };

        final ObservableTransformer<RestoreEvent, RestoreAction> restoreTransformer =
                new ObservableTransformer<RestoreEvent, RestoreAction>() {
            @Override
            public ObservableSource<RestoreAction> apply(@io.reactivex.annotations.NonNull Observable<RestoreEvent> upstream) {
                return upstream.flatMap(new Function<RestoreEvent, ObservableSource<RestoreAction>>() {
                    @Override
                    public ObservableSource<RestoreAction> apply(RestoreEvent restoreEvent) throws Exception {
                        return Observable.just(new RestoreAction(restoreEvent.getPageNumber()));
                    }
                });
            }
        };

        transformEventsIntoActions = new ObservableTransformer<UiEvent, Action>() {
            @Override
            public ObservableSource<Action> apply(@io.reactivex.annotations.NonNull Observable<UiEvent> upstream) {
                return upstream.publish(new Function<Observable<UiEvent>, ObservableSource<Action>>() {
                    @Override
                    public ObservableSource<Action> apply(Observable<UiEvent> uiEventObservable) throws Exception {
                        return Observable.merge(
                                uiEventObservable.ofType(ScrollEvent.class).compose(scrollTransformer),
                                uiEventObservable.ofType(RestoreEvent.class).compose(restoreTransformer));
                    }
                });
            }
        };
    }
}
