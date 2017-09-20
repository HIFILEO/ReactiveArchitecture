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

package com.example.mvvmreactive.viewcontroller;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.mvvmreactive.R;
import com.example.mvvmreactive.adapter.LoadMoreScrollListener;
import com.example.mvvmreactive.adapter.NowPlayingListAdapter;
import com.example.mvvmreactive.databinding.ActivityNowPlayingBinding;
import com.example.mvvmreactive.model.AdapterUiCommand;


import com.example.mvvmreactive.view.DividerItemDecoration;
import com.example.mvvmreactive.viewmodel.NowPlayingViewModel;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;


/**
 * This is the only activity for the application.
 */
public class NowPlayingActivity extends BaseActivity implements LoadMoreScrollListener.OnLoadMoreListener {
    private static final String LAST_SCROLL_POSITION = "LAST_SCROLL_POSITION";
    private NowPlayingListAdapter nowPlayingListAdapter;
    private NowPlayingViewModel nowPlayingViewModel;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ActivityNowPlayingBinding nowPlayingBinding;
    private LoadMoreScrollListener loadMoreScrollListener;

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

        loadMoreScrollListener = new LoadMoreScrollListener(
                (LinearLayoutManager) nowPlayingBinding.recyclerView.getLayoutManager(),
                this);
    }

    @Override
    public void onLoadMore() {
        nowPlayingViewModel.loadMoreNowPlayingInfo();
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
                            nowPlayingBinding.recyclerView.addOnScrollListener(loadMoreScrollListener);
                        } else {
                            nowPlayingBinding.recyclerView.removeOnScrollListener(loadMoreScrollListener);
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
}
