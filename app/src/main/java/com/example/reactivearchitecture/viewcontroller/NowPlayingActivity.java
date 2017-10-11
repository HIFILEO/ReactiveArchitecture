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

package com.example.reactivearchitecture.viewcontroller;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.reactivearchitecture.R;
import com.example.reactivearchitecture.adapter.NowPlayingListAdapter;
import com.example.reactivearchitecture.adapter.ScrollEventCalculator;
import com.example.reactivearchitecture.databinding.ActivityNowPlayingBinding;
import com.example.reactivearchitecture.model.AdapterUiCommand;


import com.example.reactivearchitecture.view.DividerItemDecoration;
import com.example.reactivearchitecture.viewmodel.NowPlayingViewModel;
import com.jakewharton.rxbinding2.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;


/**
 * This is the only activity for the application.
 */
public class NowPlayingActivity extends BaseActivity {
    private static final String LAST_SCROLL_POSITION = "LAST_SCROLL_POSITION";
    private NowPlayingListAdapter nowPlayingListAdapter;
    private NowPlayingViewModel nowPlayingViewModel;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ActivityNowPlayingBinding nowPlayingBinding;
    private Disposable scrollDisposable;

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

        //Create Adapter
        createAdapter();

        //restore adapter
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
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

        //un-bind
        nowPlayingBinding.unbind();
    }

    /**
     * Restore the state of the screen.
     * @param savedInstanceState -
     */
    public void restoreState(Bundle savedInstanceState) {
        Parcelable savedRecyclerLayoutState =
                savedInstanceState.getParcelable(LAST_SCROLL_POSITION);
        nowPlayingBinding.recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
    }

    /**
     * Create the adapter for {@link RecyclerView}.
     */
    private void createAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        nowPlayingBinding.recyclerView.setLayoutManager(linearLayoutManager);
        nowPlayingBinding.recyclerView.addItemDecoration(new DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                getResources().getColor(android.R.color.black, null)));
        nowPlayingListAdapter = new NowPlayingListAdapter(
                //shallow-copy
                new ArrayList<>(nowPlayingViewModel.getMovieViewInfoList()));
        nowPlayingBinding.recyclerView.setAdapter(nowPlayingListAdapter);
    }

    /**
     * Bind to all data in {@link NowPlayingViewModel}.
     */
    private void bind() {
        compositeDisposable.add(nowPlayingViewModel.getLoadMoreEnabledBehaviorSubject()
                //subscribe down - main UI to update views
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean enabled) throws Exception {
                        if (enabled) {
                            bindScrollEvents();
                        } else {
                            if (scrollDisposable != null) {
                                scrollDisposable.dispose();
                                compositeDisposable.delete(scrollDisposable);
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        //No-OP
                    }
                }));

        compositeDisposable.add(nowPlayingViewModel.getAdapterDataPublishSubject()
                //observer down - main UI to update views
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<AdapterUiCommand>() {
                    @Override
                    public void accept(@NonNull AdapterUiCommand adapterUiCommand) throws Exception {
                        Timber.i("Thread name: %s.", Thread.currentThread().getName());

                        if (adapterUiCommand.getCommandType() == AdapterUiCommand.CommandType.ADD) {
                            if (adapterUiCommand.getMovieViewInfoList().get(0) == null) {
                                nowPlayingListAdapter.add(null);
                                nowPlayingBinding.recyclerView.scrollToPosition(nowPlayingListAdapter.getItemCount() - 1);
                            } else {
                                nowPlayingListAdapter.addList(adapterUiCommand.getMovieViewInfoList());
                            }

                        } else {
                            nowPlayingListAdapter.remove(adapterUiCommand.getMovieViewInfoList().get(0));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Timber.e("Error on adapter subscription." + throwable.getLocalizedMessage());
                    }
                }));

        compositeDisposable.add(nowPlayingViewModel.getShowErrorObserver()
                //observer down
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean showError) throws Exception {
                        if (showError) {
                            Toast.makeText(NowPlayingActivity.this, R.string.error_msg, Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Timber.e("Error on error subscription." + throwable.getLocalizedMessage());
                    }
                }));
    }

    /**
     * Bind Scroll Events to {@link RecyclerView}.
     */
    private void bindScrollEvents() {
        Observable<RecyclerViewScrollEvent> scrollEventObservable = RxRecyclerView.scrollEvents(
                nowPlayingBinding.recyclerView);

        scrollDisposable = scrollEventObservable.subscribe(new Consumer<RecyclerViewScrollEvent>() {
            @Override
            public void accept(@NonNull RecyclerViewScrollEvent recyclerViewScrollEvent) throws Exception {

                ScrollEventCalculator scrollEventCalculator =
                        new ScrollEventCalculator(recyclerViewScrollEvent);

                if (scrollEventCalculator.isAtScrollEnd()) {
                    nowPlayingViewModel.loadMoreNowPlayingInfo();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                //this should never happen. Throw error.
                throw new Error("RxBindings Error.");
            }
        });
        compositeDisposable.add(scrollDisposable);
    }
}
