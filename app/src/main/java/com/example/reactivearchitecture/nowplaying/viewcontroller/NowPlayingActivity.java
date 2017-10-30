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

package com.example.reactivearchitecture.nowplaying.viewcontroller;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.reactivearchitecture.R;

import com.example.reactivearchitecture.core.view.DividerItemDecoration;
import com.example.reactivearchitecture.core.viewcontroller.BaseActivity;

import com.example.reactivearchitecture.databinding.ActivityNowPlayingBinding;
import com.example.reactivearchitecture.nowplaying.adapter.ScrollEventCalculator;
import com.example.reactivearchitecture.nowplaying.adapter.filter.FilterAdapter;
import com.example.reactivearchitecture.nowplaying.adapter.nowplaying.NowPlayingListAdapter;
import com.example.reactivearchitecture.nowplaying.model.AdapterCommandType;
import com.example.reactivearchitecture.nowplaying.model.event.FilterEvent;
import com.example.reactivearchitecture.nowplaying.model.event.ScrollEvent;
import com.example.reactivearchitecture.nowplaying.model.uimodel.UiModel;
import com.example.reactivearchitecture.nowplaying.view.FilterView;
import com.example.reactivearchitecture.nowplaying.view.MovieViewInfo;
import com.example.reactivearchitecture.nowplaying.viewmodel.NowPlayingViewModel;
import com.jakewharton.rxbinding2.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding2.widget.RxAdapterView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import timber.log.Timber;

/**
 * This is the only activity for the application.
 */
public class NowPlayingActivity extends BaseActivity {
    private static final String LAST_SCROLL_POSITION = "LAST_SCROLL_POSITION";
    private static final String LAST_UIMODEL = "LAST_UIMODEL";

    private NowPlayingListAdapter nowPlayingListAdapter;
    private NowPlayingViewModel nowPlayingViewModel;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ActivityNowPlayingBinding nowPlayingBinding;
    private Disposable scrollDisposable;
    private Parcelable savedRecyclerLayoutState;
    private UiModel latestUiModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        //Create / Retrieve ViewModel
        nowPlayingViewModel = ViewModelProviders.of(this, viewModelFactory).get(NowPlayingViewModel.class);

        //Create & Set Binding
        nowPlayingBinding = DataBindingUtil.setContentView(this, R.layout.activity_now_playing);
        nowPlayingBinding.setViewModel(nowPlayingViewModel);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(nowPlayingBinding.toolbar);

        //restore
        UiModel savedUiModel = null;
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(LAST_SCROLL_POSITION);
            savedUiModel = savedInstanceState.getParcelable(LAST_UIMODEL);
        }

        //init viewModel
        nowPlayingViewModel.init(savedUiModel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bind();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LAST_SCROLL_POSITION, nowPlayingBinding.recyclerView.getLayoutManager().onSaveInstanceState());
        outState.putParcelable(LAST_UIMODEL, latestUiModel);
    }

    @Override
    public void onStop() {
        super.onStop();

        //un-subscribe
        compositeDisposable.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //un-bind (Android Databinding)
        nowPlayingBinding.unbind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //
        //Inflate
        //
        getMenuInflater().inflate(R.menu.menu_action_bar_spinner, menu);
        Spinner filterSpinner = (Spinner) menu.findItem(R.id.filterSpinner).getActionView();
        filterSpinner.setAdapter(createFilterAdapter());

        //
        //Restore
        //
        //Note - menu items get created AFTER onStart(). So must restore here.
        if (latestUiModel.isFilterOn()) {
            filterSpinner.setSelection(1);
        } else {
            filterSpinner.setSelection(0);
        }

        //
        //Bind to Spinner
        //
        compositeDisposable.add(RxAdapterView.itemSelections(filterSpinner)
                //When binding for the first time, the previous restore triggers a filter command twice. Not needed.
                .skip(2)
                .map(new Function<Integer, FilterEvent>() {
                    @Override
                    public FilterEvent apply(@NonNull Integer integer) throws Exception {
                        return new FilterEvent(integer == 1);
                    }
                })
                //Send FilterEvent to ViewModel
                .subscribe(new Consumer<FilterEvent>() {
                    @Override
                    public void accept(@NonNull FilterEvent filterEvent) throws Exception {
                        nowPlayingViewModel.processUiEvent(filterEvent);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throw new UnsupportedOperationException("Errors in filter event unsupported. Crash app."
                                + throwable.getLocalizedMessage());
                    }
                })
        );

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Create the adapter for {@link RecyclerView}.
     * @param adapterList - List that backs the adapter.
     */
    private void createAdapter(List<MovieViewInfo> adapterList) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        nowPlayingBinding.recyclerView.setLayoutManager(linearLayoutManager);
        nowPlayingBinding.recyclerView.addItemDecoration(new DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                getResources().getColor(android.R.color.black, null)));
        nowPlayingListAdapter = new NowPlayingListAdapter(adapterList);
        nowPlayingBinding.recyclerView.setAdapter(nowPlayingListAdapter);
    }

    /**
     * Bind to all data in {@link NowPlayingViewModel}.
     */
    private void bind() {
        //
        //Bind to UiModel
        //
        compositeDisposable.add(nowPlayingViewModel.getUiModels()
                .subscribe(new Consumer<UiModel>() {
                    @Override
                    public void accept(@NonNull UiModel uiModel) throws Exception {
                        processUiModel(uiModel);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throw new UnsupportedOperationException("Errors from Model Unsupported: "
                                + throwable.getLocalizedMessage());
                    }
                })
        );
    }

    /**
     * Bind to scroll events.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    private void bindToScrollEvent() {
        //
        //Guard
        //
        if (scrollDisposable != null) {
            return;
        }

        //
        //Bind
        //
        scrollDisposable = RxRecyclerView.scrollEvents(nowPlayingBinding.recyclerView)
                //Bind to RxBindings.
                .flatMap(new Function<RecyclerViewScrollEvent, ObservableSource<ScrollEvent>>() {
                    @Override
                    public ObservableSource<ScrollEvent> apply(@NonNull RecyclerViewScrollEvent recyclerViewScrollEvent)
                            throws Exception {
                        ScrollEventCalculator scrollEventCalculator =
                                new ScrollEventCalculator(recyclerViewScrollEvent);

                        //Only handle 'is at end' of list scroll events
                        if (scrollEventCalculator.isAtScrollEnd()) {
                            ScrollEvent scrollEvent = new ScrollEvent();
                            scrollEvent.setPageNumber(latestUiModel.getPageNumber() + 1);
                            return Observable.just(scrollEvent);
                        } else {
                            return Observable.empty();
                        }
                    }
                })
                //Filter any multiple events before 250MS
                .debounce(new Function<ScrollEvent, ObservableSource<ScrollEvent>>() {
                    @Override
                    public ObservableSource<ScrollEvent> apply(@NonNull ScrollEvent scrollEvent) throws Exception {
                        return Observable.<ScrollEvent>empty().delay(250, TimeUnit.MILLISECONDS);
                    }
                })
                //Send ScrollEvent to ViewModel
                .subscribe(new Consumer<ScrollEvent>() {
                    @Override
                    public void accept(@NonNull ScrollEvent scrollEvent) throws Exception {
                        nowPlayingViewModel.processUiEvent(scrollEvent);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throw new UnsupportedOperationException("Errors in scroll event unsupported. Crash app."
                                + throwable.getLocalizedMessage());
                    }
                });

        compositeDisposable.add(scrollDisposable);
    }

    /**
     * Unbind from scroll events.
     */
    private void unbindFromScrollEvent() {
        if (scrollDisposable != null) {
            scrollDisposable.dispose();
            compositeDisposable.delete(scrollDisposable);
            scrollDisposable = null;
        }
    }

    /**
     * Bind to {@link UiModel}.
     * @param uiModel - the {@link UiModel} from {@link NowPlayingViewModel} that backs the UI.
     */
    private void processUiModel(UiModel uiModel) {
        /*
        Note - Keep the logic here as SIMPLE as possible.
         */
        Timber.i("Thread name: %s. Update UI based on UiModel.", Thread.currentThread().getName());
        this.latestUiModel = uiModel;

        //
        //Update progressBar
        //
        nowPlayingBinding.progressBar.setVisibility(
                uiModel.isFirstTimeLoad() ? View.VISIBLE : View.GONE);

        //
        //Scroll Listener
        //
        if (uiModel.isEnableScrollListener()) {
            bindToScrollEvent();
        } else {
            unbindFromScrollEvent();
        }

        //
        //Update adapter
        //
        if (nowPlayingListAdapter == null) {
            //Note, get returns a shallow-copy
            ArrayList<MovieViewInfo> adapterData = (ArrayList<MovieViewInfo>) uiModel.getCurrentList();

            //Process last adapter command
            if (uiModel.getAdapterCommandType() == AdapterCommandType.ADD_DATA_ONLY
                    || uiModel.getAdapterCommandType() == AdapterCommandType.ADD_DATA_REMOVE_IN_PROGRESS
                    || uiModel.getAdapterCommandType() == AdapterCommandType.SWAP_LIST_DUE_TO_NEW_FILTER) {
                adapterData.addAll(uiModel.getResultList());
            } else if (uiModel.getAdapterCommandType() == AdapterCommandType.SHOW_IN_PROGRESS) {
                adapterData.add(null);
            }

            //create adapter
            createAdapter(adapterData);

            //Restore adapter state
            if (savedRecyclerLayoutState != null) {
                nowPlayingBinding.recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
            }

        } else {
            switch (uiModel.getAdapterCommandType()) {
                case AdapterCommandType.ADD_DATA_REMOVE_IN_PROGRESS:
                    //Remove Null Spinner
                    if (nowPlayingListAdapter.getItemCount() > 0) {
                        nowPlayingListAdapter.remove(
                                nowPlayingListAdapter.getItem(nowPlayingListAdapter.getItemCount() - 1));
                    }

                    //Add Data
                    nowPlayingListAdapter.addList(uiModel.getResultList());
                    break;
                case AdapterCommandType.ADD_DATA_ONLY:
                    //Add Data
                    nowPlayingListAdapter.addList(uiModel.getResultList());
                    break;
                case AdapterCommandType.SHOW_IN_PROGRESS:
                    //Add null to adapter. Null shows spinner in Adapter logic.
                    nowPlayingListAdapter.add(null);
                    nowPlayingBinding.recyclerView.scrollToPosition(nowPlayingListAdapter.getItemCount() - 1);
                    break;
                case AdapterCommandType.SWAP_LIST_DUE_TO_NEW_FILTER:
                    List<MovieViewInfo> currentList = uiModel.getCurrentList();

                    //Check if loading was in progress
                    int itemCount = nowPlayingListAdapter.getItemCount();
                    if (itemCount > 0 && nowPlayingListAdapter.getItem(itemCount - 1) == null) {
                        currentList.add(null);
                    }

                    nowPlayingListAdapter.replace(uiModel.getCurrentList());
                    break;
                default:
                    //No-Op
            }
        }

        //
        //Error Messages
        //
        if (uiModel.getFailureMsg() != null && !uiModel.getFailureMsg().isEmpty()) {
            Toast.makeText(NowPlayingActivity.this, R.string.error_msg, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create the filter list adapter.
     * @return - adapter to show for filtering.
     */
    private FilterAdapter createFilterAdapter() {
        FilterView filterViewOn = new FilterView(
                getString(R.string.filter_on),
                R.drawable.star_filled,
                true);

        FilterView filterViewOff = new FilterView(
                getString(R.string.filter_off),
                R.drawable.star_empty,
                false);

        List<FilterView> filterViewList = new ArrayList<>();
        filterViewList.add(filterViewOff);
        filterViewList.add(filterViewOn);

        return new FilterAdapter(this, filterViewList);
    }
}
