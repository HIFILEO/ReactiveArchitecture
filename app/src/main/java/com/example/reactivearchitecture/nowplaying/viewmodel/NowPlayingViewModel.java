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

package com.example.reactivearchitecture.nowplaying.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableField;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;

import com.example.reactivearchitecture.R;
import com.example.reactivearchitecture.core.model.action.Action;
import com.example.reactivearchitecture.nowplaying.controller.ServiceController;
import com.example.reactivearchitecture.nowplaying.interactor.NowPlayingInteractor;
import com.example.reactivearchitecture.nowplaying.model.AdapterCommandType;
import com.example.reactivearchitecture.nowplaying.model.FilterManager;
import com.example.reactivearchitecture.nowplaying.model.FilterTransformer;
import com.example.reactivearchitecture.nowplaying.model.MovieInfo;


import com.example.reactivearchitecture.nowplaying.model.action.FilterAction;
import com.example.reactivearchitecture.nowplaying.model.action.RestoreAction;
import com.example.reactivearchitecture.nowplaying.model.action.ScrollAction;
import com.example.reactivearchitecture.nowplaying.model.event.FilterEvent;
import com.example.reactivearchitecture.nowplaying.model.event.RestoreEvent;
import com.example.reactivearchitecture.nowplaying.model.event.ScrollEvent;
import com.example.reactivearchitecture.nowplaying.model.event.UiEvent;
import com.example.reactivearchitecture.nowplaying.model.result.FilterResult;
import com.example.reactivearchitecture.nowplaying.model.result.RestoreResult;
import com.example.reactivearchitecture.nowplaying.model.result.Result;
import com.example.reactivearchitecture.nowplaying.model.result.ScrollResult;
import com.example.reactivearchitecture.nowplaying.model.uimodel.UiModel;
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfo;
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfoImpl;
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
    private ObservableTransformer<UiEvent, Action> transformEventsIntoActions;

    @NonNull
    private Application application;

    @NonNull
    private ServiceController serviceController;

    //Note - left here to show example of android data binding.
    @NonNull
    private final ObservableField<String> toolbarTitle = new ObservableField<>();

    @NonNull
    private PublishRelay<UiEvent> publishRelayUiEvents = PublishRelay.create();

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected NowPlayingInteractor nowPlayingInteractor;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected FilterManager filterManager;

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected FilterTransformer filterTransformer;

    /**
     * Constructor. Members are injected.
     * @param application -
     * @param serviceController -
     */
    @Inject
    public NowPlayingViewModel(@NonNull Application application, @NonNull ServiceController serviceController) {
        this.serviceController = serviceController;
        this.application = application;
        toolbarTitle.set(application.getString(R.string.now_playing));
        createNonInjectedData();
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
                UiModel.UiModelBuilder uiModelBuilder = new UiModel.UiModelBuilder(restoredUiModel);
                initialUiModel = uiModelBuilder.createUiModel();
                startEventsObservable = Observable.just((UiEvent) new RestoreEvent(initialUiModel.getPageNumber()));
            }
            filterManager.setFilterOn(initialUiModel.isFilterOn());
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
    protected void createNonInjectedData() {
        filterManager = new FilterManager(false);
        filterTransformer = new FilterTransformer(filterManager);
        nowPlayingInteractor =  new NowPlayingInteractor(serviceController, filterTransformer);
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
                .scan(initialUiModel, new BiFunction<UiModel, Result, UiModel>() {
                    @Override
                    public UiModel apply(UiModel uiModel, Result result) throws Exception {
                        Timber.i("Thread name: %s. Scan Results to UiModel", Thread.currentThread().getName());

                        if (result instanceof ScrollResult) {
                            return processScrollResult(uiModel, (ScrollResult) result);
                        } else if (result instanceof RestoreResult) {
                            return processRestoreResult(uiModel, (RestoreResult) result);
                        } else if (result instanceof FilterResult) {
                            return processFilterResult(uiModel, (FilterResult) result);
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

    /**
     * Setup the transformers used by this {@link NowPlayingViewModel}.
     */
    @MainThread
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

        final ObservableTransformer<FilterEvent, FilterAction> filterTransformer =
                new ObservableTransformer<FilterEvent, FilterAction>() {
                    @Override
                    public ObservableSource<FilterAction> apply(Observable<FilterEvent> upstream) {
                        return upstream.flatMap(new Function<FilterEvent, ObservableSource<FilterAction>>() {
                            @Override
                            public ObservableSource<FilterAction> apply(FilterEvent filterEvent) throws Exception {
                                return Observable.just(new FilterAction(filterEvent.isFilterOn()));
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
                                uiEventObservable.ofType(RestoreEvent.class).compose(restoreTransformer),
                                uiEventObservable.ofType(FilterEvent.class).compose(filterTransformer)
                        );
                    }
                });
            }
        };
    }

    /**
     * Update the {@link UiModel} based on input form a {@link ScrollResult}.
     * @param uiModel - model to update
     * @param scrollResult - results from {@link ScrollAction}
     * @return new updated {@link UiModel}
     */
    private UiModel processScrollResult(@NonNull UiModel uiModel, ScrollResult scrollResult) {
        UiModel.UiModelBuilder uiModelBuilder = new UiModel.UiModelBuilder(uiModel);

        switch (scrollResult.getType()) {
            case Result.ResultType.IN_FLIGHT:
                //In Progress
                uiModelBuilder
                        .setFirstTimeLoad(scrollResult.getPageNumber() == 1)
                        .setFailureMsg(null)
                        .setPageNumber(scrollResult.getPageNumber())
                        .setEnableScrollListener(false)
                        .setResultList(null)
                        .setAdapterCommandType(scrollResult.getPageNumber() == 1
                                ? AdapterCommandType.DO_NOTHING : AdapterCommandType.SHOW_IN_PROGRESS);
                break;
            case Result.ResultType.SUCCESS:
                List<MovieViewInfo> listToAdd = translateResultsForUi(scrollResult.getResult());
                List<MovieViewInfo> currentList = uiModel.getCurrentList();
                currentList.addAll(listToAdd);

                //Success
                uiModelBuilder
                        .setFirstTimeLoad(false)
                        .setFailureMsg(null)
                        .setPageNumber(scrollResult.getPageNumber())
                        .setEnableScrollListener(true)
                        .setCurrentList(currentList)
                        .setResultList(listToAdd)
                        .setAdapterCommandType(AdapterCommandType.ADD_DATA_REMOVE_IN_PROGRESS);
                break;
            case Result.ResultType.FAILURE:
                Timber.e(scrollResult.getError());

                //Failure
                uiModelBuilder
                        .setFirstTimeLoad(scrollResult.getPageNumber() == 1)
                        .setFailureMsg(application.getString(R.string.error_msg))
                        .setPageNumber(scrollResult.getPageNumber() - 1)
                        .setEnableScrollListener(false)
                        .setResultList(null)
                        .setAdapterCommandType(AdapterCommandType.DO_NOTHING);
                break;
            default:
                //Unknown result - throw error
                throw new IllegalArgumentException("Unknown ResultType: " + scrollResult.getType());
        }

        return uiModelBuilder.createUiModel();
    }

    /**
     * Update the {@link UiModel} based on input form a {@link RestoreResult}.
     * @param uiModel - model to update
     * @param restoreResult - results from {@link RestoreAction}
     * @return new updated {@link UiModel}
     */
    private UiModel processRestoreResult(@NonNull UiModel uiModel, RestoreResult restoreResult) {
        UiModel.UiModelBuilder uiModelBuilder = new UiModel.UiModelBuilder(uiModel);

        List<MovieViewInfo> listToAdd = null;
        List<MovieViewInfo> currentList = uiModel.getCurrentList();

        if (restoreResult.getResult() != null && !restoreResult.getResult().isEmpty()) {
            listToAdd = translateResultsForUi(restoreResult.getResult());
            currentList.addAll(listToAdd);
        }

        switch (restoreResult.getType()) {
            case Result.ResultType.IN_FLIGHT:
                //In Progress
                uiModelBuilder
                        .setFirstTimeLoad(true)
                        .setFailureMsg(null)
                        .setPageNumber(restoreResult.getPageNumber())
                        .setEnableScrollListener(false)
                        .setCurrentList(currentList)
                        .setResultList(listToAdd)
                        .setAdapterCommandType(listToAdd == null || listToAdd.isEmpty()
                                ? AdapterCommandType.DO_NOTHING : AdapterCommandType.ADD_DATA_ONLY);
                break;
            case Result.ResultType.SUCCESS:
                //Success
                uiModelBuilder
                        .setFirstTimeLoad(false)
                        .setFailureMsg(null)
                        .setPageNumber(restoreResult.getPageNumber())
                        .setEnableScrollListener(true)
                        .setCurrentList(currentList)
                        .setResultList(listToAdd)
                        .setAdapterCommandType(AdapterCommandType.ADD_DATA_ONLY);
                break;
            case Result.ResultType.FAILURE:
                Timber.e(restoreResult.getError());

                //Error
                uiModelBuilder
                        .setFirstTimeLoad(true)
                        .setFailureMsg(application.getString(R.string.error_msg))
                        .setPageNumber(restoreResult.getPageNumber() - 1)
                        .setEnableScrollListener(false)
                        .setResultList(null)
                        .setAdapterCommandType(AdapterCommandType.DO_NOTHING);
                break;
            default:
                //Unknown result - throw error
                throw new IllegalArgumentException("Unknown ResultType: " + restoreResult.getType());
        }

        return uiModelBuilder.createUiModel();
    }

    /**
     * Update the {@link UiModel} based on input form a {@link FilterResult}.
     * @param uiModel - model to udate
     * @param filterResult - results from {@link FilterResult}
     * @return new updated {@link UiModel}
     */
    private UiModel processFilterResult(@NonNull UiModel uiModel, FilterResult filterResult) {
        UiModel.UiModelBuilder uiModelBuilder = new UiModel.UiModelBuilder(uiModel);

        switch (filterResult.getType()) {
            case Result.ResultType.IN_FLIGHT:
                Timber.i("Filter - IN_FLIGHT");
                break;
            case Result.ResultType.SUCCESS:
                //Success
                uiModelBuilder
                        .setCurrentList(translateResultsForUi(filterResult.getFilteredList()))
                        .setFilterOn(filterResult.isFilterOn())
                        .setResultList(null)
                        .setAdapterCommandType(AdapterCommandType.SWAP_LIST_DUE_TO_NEW_FILTER);
                break;
            case Result.ResultType.FAILURE:
                Timber.e("Failure during filter. Throw error, this should never happen.");
                throw new IllegalArgumentException("Failure during filter. This should never happen.");
            default:
                //Unknown result - throw error
                throw new IllegalArgumentException("Unknown ResultType: " + filterResult.getType());
        }

        return uiModelBuilder.createUiModel();
    }
}
