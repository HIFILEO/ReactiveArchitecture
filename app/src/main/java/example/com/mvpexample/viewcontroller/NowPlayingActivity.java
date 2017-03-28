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

package example.com.mvpexample.viewcontroller;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import example.com.mvpexample.R;
import example.com.mvpexample.adapter.NowPlayingListAdapter;
import example.com.mvpexample.application.MvpExampleApplication;
import example.com.mvpexample.gateway.ServiceGateway;
import example.com.mvpexample.gateway.ServiceGatewayImpl;
import example.com.mvpexample.interactor.NowPlayingInteractor;
import example.com.mvpexample.interactor.NowPlayingInteractorImpl;
import example.com.mvpexample.model.MovieViewInfo;
import example.com.mvpexample.presenter.NowPlayingPresenter;
import example.com.mvpexample.presenter.NowPlayingPresenterImpl;
import example.com.mvpexample.presenter.NowPlayingViewModel;
import example.com.mvpexample.service.ServiceApi;
import example.com.mvpexample.view.DividerItemDecoration;

/**
 * This is the only activity for the application that links into the MVP architecture.
 */
public class NowPlayingActivity extends AppCompatActivity implements NowPlayingViewModel, NowPlayingListAdapter.OnLoadMoreListener {
    private NowPlayingPresenter nowPlayingPresenter;
    private NowPlayingListAdapter nowPlayingListAdapter;

    //Injected components
    @Inject
    ServiceApi serviceApi;

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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Inject Dagger Service
        MvpExampleApplication.getInstance().getComponent().inject(this);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.now_playing));

        //Create Interactor
        ServiceGateway serviceGateway = new ServiceGatewayImpl(serviceApi,
                getString(R.string.api_key),
                getString(R.string.image_url_path));
        NowPlayingInteractor nowPlayingInteractor =
                new NowPlayingInteractorImpl(serviceGateway, new Handler());

        //Create Presenter
        nowPlayingPresenter = new NowPlayingPresenterImpl(this, nowPlayingInteractor);

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
}
