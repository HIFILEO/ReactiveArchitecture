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

package com.example.mvpexample.viewcontroller;


import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mvpexample.R;
import com.example.mvpexample.adapter.NowPlayingListAdapter;
import com.example.mvpexample.dagger.ApplicationComponent;
import com.example.mvpexample.dagger.DaggerNowPlayingActivityComponent;
import com.example.mvpexample.dagger.NowPlayingActivityComponent;
import com.example.mvpexample.dagger.NowPlayingActivityModule;
import com.example.mvpexample.model.MovieViewInfo;
import com.example.mvpexample.presenter.NowPlayingPresenter;
import com.example.mvpexample.presenter.NowPlayingViewModel;
import com.example.mvpexample.view.DividerItemDecoration;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;

/**
 * This is the only activity for the application that links into the MVP architecture.
 */
public class NowPlayingActivity extends BaseActivity implements NowPlayingViewModel,
        NowPlayingListAdapter.OnLoadMoreListener {
    private NowPlayingListAdapter nowPlayingListAdapter;

    @Inject
    NowPlayingPresenter nowPlayingPresenter;

    //Bind Views
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        toolbar.setTitle(getString(R.string.now_playing));
        setSupportActionBar(toolbar);

        //Start Presenter so Interactor response model is setup correctly.
        nowPlayingPresenter.start();

        //Load Info
        nowPlayingPresenter.loadMoreInfo();
    }

    @Override
    public void showInProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError() {
        Toast.makeText(this, R.string.error_msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void addToAdapter(List<MovieViewInfo> movieViewInfoList) {
        if (nowPlayingListAdapter == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addItemDecoration(new DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL_LIST,
                    getResources().getColor(android.R.color.black, null)));
            nowPlayingListAdapter = new NowPlayingListAdapter(movieViewInfoList, this, recyclerView);
            recyclerView.setAdapter(nowPlayingListAdapter);

        } else {
            if (!movieViewInfoList.isEmpty()) {
                nowPlayingListAdapter.addList(movieViewInfoList);
            } else {
                nowPlayingListAdapter.disableLoadMore();
            }
        }
    }

    @Override
    public void onLoadMore() {
        nowPlayingPresenter.loadMoreInfo();
    }

    @Override
    public void injectDaggerMembers(ApplicationComponent applicationComponent) {
        NowPlayingActivityComponent nowPlayingActivityComponent =
                DaggerNowPlayingActivityComponent.builder()
                        .applicationComponent(applicationComponent)
                        .nowPlayingActivityModule(new NowPlayingActivityModule(this, this))
                        .build();
        nowPlayingActivityComponent.inject(this);
    }
}
