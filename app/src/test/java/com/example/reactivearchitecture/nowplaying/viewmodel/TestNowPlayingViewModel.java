package com.example.reactivearchitecture.nowplaying.viewmodel;

import android.app.Application;
import android.support.annotation.NonNull;

import com.example.reactivearchitecture.nowplaying.controller.ServiceController;
import com.example.reactivearchitecture.nowplaying.interactor.NowPlayingInteractor;
import com.example.reactivearchitecture.nowplaying.model.FilterManager;

/**
 * Test class to override the created objects during construction that we don't want passed in via dagger.
 */
public class TestNowPlayingViewModel extends NowPlayingViewModel {

    /**
     * Constructor. Members are injected.
     *
     * @param application    -
     * @param serviceController -
     */
    public TestNowPlayingViewModel(@NonNull Application application, @NonNull ServiceController serviceController,
                                   @NonNull NowPlayingInteractor nowPlayingInteractor,
                                   @NonNull FilterManager filterManager) {
        super(application, serviceController);
        super.nowPlayingInteractor = nowPlayingInteractor;
        super.filterManager = filterManager;
    }

    @Override
    protected void createNonInjectedData() {
        //Do Nothing
    }
}
