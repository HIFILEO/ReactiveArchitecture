package com.example.mvpexample.presenter;

import android.os.Bundle;

import com.example.mvpexample.categories.UnitTest;
import com.example.mvpexample.interactor.NowPlayingInteractor;
import com.example.mvpexample.model.MovieInfo;
import com.example.mvpexample.model.MovieInfoImpl;
import com.example.mvpexample.model.MovieViewInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@Category(UnitTest.class)
public class NowPlayingPresenterImplTest {
    private MovieInfoImpl movieInfo = new MovieInfoImpl(
            "www.pictureurl.com",
            "title",
            new Date(),
            8.2
    );

    @Mock
    NowPlayingViewModel mockNowPlayingViewModel;
    @Mock
    NowPlayingInteractor mockNowPlayingInteractor;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void start_firstLaunch() {
        //
        //Arrange
        //
        NowPlayingPresenterImpl nowPlayingPresenter =
                new NowPlayingPresenterImpl(mockNowPlayingViewModel, mockNowPlayingInteractor);

        //
        //Act
        //
        nowPlayingPresenter.start(null);

        //
        //Assert
        //
        verify(mockNowPlayingInteractor).setNowPlayingResponseModel(nowPlayingPresenter);
        verify(mockNowPlayingInteractor).loadMoreInfo();
        verify(mockNowPlayingViewModel).showInProgress(true);
        verify(mockNowPlayingViewModel).createAdapter(null);
    }

    @Test
    public void start_restore() {
        //
        //Arrange
        //
        NowPlayingPresenterImpl nowPlayingPresenter =
                new NowPlayingPresenterImpl(mockNowPlayingViewModel, mockNowPlayingInteractor);
        Bundle mockBundle = Mockito.mock(Bundle.class);

        //
        //Act
        //
        nowPlayingPresenter.start(mockBundle);

        //
        //Assert
        //
        verify(mockNowPlayingInteractor).setNowPlayingResponseModel(nowPlayingPresenter);
        verify(mockNowPlayingInteractor, never()).loadMoreInfo();
        verify(mockNowPlayingViewModel).showInProgress(true);
        verify(mockNowPlayingViewModel).createAdapter(mockBundle);
        verify(mockNowPlayingViewModel).restoreState(mockBundle);
    }

    @Test
    public void infoLoaded() throws Exception {
        //
        //Arrange
        //
        NowPlayingPresenterImpl nowPlayingPresenter =
                new NowPlayingPresenterImpl(mockNowPlayingViewModel, mockNowPlayingInteractor);

        List<MovieInfo> movieInfoList = new ArrayList<>();
        movieInfoList.add(movieInfo);

        ArgumentCaptor<List<MovieViewInfo>> argumentCaptor = ArgumentCaptor.forClass(List.class);

        //
        //Act
        //
        nowPlayingPresenter.infoLoaded(movieInfoList);

        //
        //Assert
        //
        verify(mockNowPlayingViewModel).addToAdapter(anyListOf(MovieViewInfo.class));
        verify(mockNowPlayingViewModel).showInProgress(false);

        verify(mockNowPlayingViewModel).addToAdapter(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().size()).isEqualTo(1);
    }

    @Test
    public void errorLoadingInfoData() throws Exception {
        //
        //Arrange
        //
        NowPlayingPresenterImpl nowPlayingPresenter =
                new NowPlayingPresenterImpl(mockNowPlayingViewModel, mockNowPlayingInteractor);

        List<MovieInfo> movieInfoList = new ArrayList<>();
        movieInfoList.add(movieInfo);

        //
        //Act
        //
        nowPlayingPresenter.errorLoadingInfoData();

        //
        //Assert
        //
        verify(mockNowPlayingViewModel).showError();
        verify(mockNowPlayingInteractor).loadMoreInfo();
    }

}