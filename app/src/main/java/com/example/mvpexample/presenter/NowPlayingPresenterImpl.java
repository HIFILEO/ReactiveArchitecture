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

package com.example.mvpexample.presenter;

import com.example.mvpexample.interactor.NowPlayingInteractor;
import com.example.mvpexample.interactor.NowPlayingResponseModel;
import com.example.mvpexample.model.MovieInfo;
import com.example.mvpexample.model.MovieViewInfo;
import com.example.mvpexample.model.MovieViewInfoImpl;

import java.util.ArrayList;
import java.util.List;


/**
 * Implements the Presenter interface.
 */
public class NowPlayingPresenterImpl implements NowPlayingPresenter, NowPlayingResponseModel {
    private NowPlayingViewModel nowPlayingViewModel;
    private NowPlayingInteractor nowPlayingInteractor;

    public NowPlayingPresenterImpl(NowPlayingViewModel nowPlayingViewModel,
                                   NowPlayingInteractor nowPlayingInteractor) {
        this.nowPlayingInteractor = nowPlayingInteractor;
        this.nowPlayingViewModel = nowPlayingViewModel;
    }

    @Override
    public void loadMoreInfo() {
        nowPlayingInteractor.loadMoreInfo();
    }

    @Override
    public void start() {
        nowPlayingInteractor.setNowPlayingResponseModel(this);
        nowPlayingViewModel.showInProgress(true);
    }

    @Override
    public void infoLoaded(List<MovieInfo> movieInfoList) {
        /*
        Note - translate internal business logic to presenter logic
         */
        List<MovieViewInfo> movieViewInfoList = new ArrayList<>();
        for (MovieInfo movieInfo : movieInfoList) {
            movieViewInfoList.add(new MovieViewInfoImpl(movieInfo));
        }
        nowPlayingViewModel.addToAdapter(movieViewInfoList);
        nowPlayingViewModel.showInProgress(false);
    }

    @Override
    public void errorLoadingInfoData() {
        //show error
        nowPlayingViewModel.showError();

        //try again
        loadMoreInfo();
    }
}
