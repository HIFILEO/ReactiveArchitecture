package com.example.reactivearchitecture.viewmodel;

import android.app.Application;

import com.example.reactivearchitecture.categories.UnitTest;
import com.example.reactivearchitecture.gateway.ServiceGateway;
import com.example.reactivearchitecture.model.MovieInfo;
import com.example.reactivearchitecture.model.MovieInfoImpl;
import com.example.reactivearchitecture.model.MovieViewInfo;
import com.example.reactivearchitecture.model.NowPlayingInfo;
import com.example.reactivearchitecture.model.NowPlayingInfoImpl;
import com.example.reactivearchitecture.rx.RxJavaTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Category(UnitTest.class)
public class NowPlayingViewModelTest extends RxJavaTest {
    @Mock
    ServiceGateway mockServiceGateway;

    @Mock
    Application mockApplication;

    @Before
    public void setUp() {
        super.setUp();
        initMocks(this);

        MovieInfoImpl movieInfoImpl = new MovieInfoImpl("www.mypicture.com", "title", new Date(), 10.0);
        List<MovieInfo> movieInfoList = new ArrayList<>();
        movieInfoList.add(movieInfoImpl);

        NowPlayingInfoImpl nowPlayingInfo = new NowPlayingInfoImpl(movieInfoList, 1, 1);

        Observable<NowPlayingInfo> nowPlayingInfoObservable = Observable.just((NowPlayingInfo) nowPlayingInfo);
        when(mockServiceGateway.getNowPlaying(anyInt())).thenReturn(nowPlayingInfoObservable);
    }

    //TODO - write tests for UiModel cases while mocking Interactor


}