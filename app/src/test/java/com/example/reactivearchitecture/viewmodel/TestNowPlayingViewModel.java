package com.example.reactivearchitecture.viewmodel;

import android.app.Application;
import android.support.annotation.NonNull;

import com.example.reactivearchitecture.gateway.ServiceGateway;
import com.example.reactivearchitecture.interactor.NowPlayingInteractor;
import com.example.reactivearchitecture.model.FilterManager;

import org.mockito.Mockito;

/**
 * Test class to override the created objects during construction that we don't want passed in via dagger.
 */
public class TestNowPlayingViewModel extends NowPlayingViewModel {

    /**
     * Constructor. Members are injected.
     *
     * @param application    -
     * @param serviceGateway -
     */
    public TestNowPlayingViewModel(@NonNull Application application, @NonNull ServiceGateway serviceGateway,
                                   @NonNull NowPlayingInteractor nowPlayingInteractor,
                                   @NonNull FilterManager filterManager) {
        super(application, serviceGateway);
        super.nowPlayingInteractor = nowPlayingInteractor;
        super.filterManager = filterManager;
    }

    @Override
    protected void createNonInjectedData() {
        //Do Nothing
    }
}
