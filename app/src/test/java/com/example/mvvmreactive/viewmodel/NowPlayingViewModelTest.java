package com.example.mvvmreactive.viewmodel;

import android.app.Application;

import com.example.mvvmreactive.categories.UnitTest;
import com.example.mvvmreactive.gateway.ServiceGateway;
import com.example.mvvmreactive.model.MovieInfo;
import com.example.mvvmreactive.model.MovieInfoImpl;
import com.example.mvvmreactive.model.MovieViewInfo;
import com.example.mvvmreactive.model.NowPlayingInfo;
import com.example.mvvmreactive.model.NowPlayingInfoImpl;
import com.example.mvvmreactive.rx.RxJavaTest;

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

    @Test
    public void isFirstLoadInProgress_true() throws Exception {
        //
        //Arrange
        //
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockApplication, mockServiceGateway);

        //
        //Act
        //

        //
        //Assert
        //
        assertThat(nowPlayingViewModel.getFirstLoad().get()).isTrue();
    }

    @Test
    public void isFirstLoadInProgress_false() throws Exception {
        //
        //Arrange
        //
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockApplication, mockServiceGateway);

        //
        //Act
        //
        testScheduler.triggerActions();
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        //
        //Assert
        //
        assertThat(nowPlayingViewModel.getFirstLoad().get()).isFalse();
    }

    @Test
    public void getMovieViewInfoList() throws Exception {
        //
        //Arrange
        //
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockApplication, mockServiceGateway);

        //
        //Act
        //
        List<MovieViewInfo> movieViewInfos = nowPlayingViewModel.getMovieViewInfoList();

        //
        //Assert
        //
        assertThat(movieViewInfos.size()).isEqualTo(0);
    }
}