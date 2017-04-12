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

package com.example.mvpexample.dagger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.example.mvpexample.R;
import com.example.mvpexample.gateway.ServiceGateway;
import com.example.mvpexample.gateway.ServiceGatewayImpl;
import com.example.mvpexample.interactor.NowPlayingInteractor;
import com.example.mvpexample.interactor.NowPlayingInteractorImpl;
import com.example.mvpexample.presenter.NowPlayingPresenter;
import com.example.mvpexample.presenter.NowPlayingPresenterImpl;
import com.example.mvpexample.presenter.NowPlayingViewModel;
import com.example.mvpexample.service.ServiceApi;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger Module for the {@link com.example.mvpexample.viewcontroller.NowPlayingActivity}
 */
@Module
public class NowPlayingActivityModule extends ActivityModule {
    private NowPlayingViewModel nowPlayingViewModel;

    public NowPlayingActivityModule(Activity activity, NowPlayingViewModel nowPlayingViewModel) {
        super(activity);
        this.nowPlayingViewModel = nowPlayingViewModel;
    }

    @Provides
    @ActivityScope
    public ServiceGateway providesServiceGateway(ServiceApi serviceApi) {
        return new ServiceGatewayImpl(serviceApi,
                activity().getString(R.string.api_key),
                activity().getString(R.string.image_url_path));
    }

    @Provides
    @ActivityScope
    public NowPlayingInteractor providesNowPlayingInteractor(ServiceGateway serviceGateway) {
        return new NowPlayingInteractorImpl(serviceGateway, new Handler());
    }

    @Provides
    @ActivityScope
    public NowPlayingPresenter providesNowPlayingPresenter(NowPlayingInteractor nowPlayingInteractor) {
        return new NowPlayingPresenterImpl(nowPlayingViewModel, nowPlayingInteractor);
    }
}
