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

import com.example.mvpexample.gateway.ServiceGateway;
import com.example.mvpexample.interactor.NowPlayingInteractor;
import com.example.mvpexample.presenter.NowPlayingPresenter;
import com.example.mvpexample.presenter.NowPlayingPresenterImpl_IdlingResource;
import com.example.mvpexample.service.ServiceApi;

import org.mockito.Mockito;

import dagger.Module;
import dagger.Provides;

/**
 * Mock {@link NowPlayingActivityModule}
 * Note - you cannot extend the {@link NowPlayingActivityModule}. So the work around, pass in via constructor.
 */
@Module
public class TestNowPlayingActivityModule {
    private NowPlayingActivityModule nowPlayingActivityModule;

    public TestNowPlayingActivityModule(NowPlayingActivityModule nowPlayingActivityModule) {
        this.nowPlayingActivityModule = nowPlayingActivityModule;
    }

    @Provides
    @ActivityScope
    public ServiceGateway providesServiceGateway(ServiceApi serviceApi) {
        return nowPlayingActivityModule.providesServiceGateway(serviceApi);
    }

    @Provides
    @ActivityScope
    public NowPlayingInteractor providesNowPlayingInteractor(ServiceGateway serviceGateway) {
        return nowPlayingActivityModule.providesNowPlayingInteractor(serviceGateway);
    }

    @Provides
    @ActivityScope
    public NowPlayingPresenter providesNowPlayingPresenter(NowPlayingInteractor nowPlayingInteractor) {
        //In order to make sure espresso idles the view checks, we put the IdlingResource on the presenter.
        return Mockito.spy(new NowPlayingPresenterImpl_IdlingResource(nowPlayingActivityModule.getNowPlayingViewModel(),
                nowPlayingInteractor));
    }
}
