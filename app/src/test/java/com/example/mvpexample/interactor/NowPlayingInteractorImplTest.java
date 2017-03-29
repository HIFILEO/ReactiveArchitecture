package com.example.mvpexample.interactor;

import android.os.Handler;

import com.example.mvpexample.categories.UnitTest;
import com.example.mvpexample.gateway.ServiceGateway;
import com.example.mvpexample.model.MovieInfo;
import com.example.mvpexample.model.MovieInfoImpl;
import com.example.mvpexample.model.NowPlayingInfo;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class NowPlayingInteractorImplTest {

    @Test
    public void loadData() throws Exception {
        //
        //Arrange
        //
        Handler mockHandler = Mockito.mock(Handler.class);
        ServiceGateway mockServiceGateway = Mockito.mock(ServiceGateway.class);
        NowPlayingResponseModel mockNowPlayingResponseModel = Mockito.mock(NowPlayingResponseModel.class);

        NowPlayingInteractorImpl.LoadDataThread loadDataThread =
                new NowPlayingInteractorImpl.LoadDataThread(
                        1,
                        mockHandler,
                        mockServiceGateway,
                        mockNowPlayingResponseModel
                );

        NowPlayingInfo mockNowPlayingInfo = Mockito.mock(NowPlayingInfo.class);

        when(mockServiceGateway.getNowPlaying(1)).thenReturn(mockNowPlayingInfo);
        when(mockNowPlayingInfo.getPageNumber()).thenReturn(1);

        List<MovieInfo> movieInfoList = new ArrayList<>();
        String pictureUrl = "www.mypicture.com";
        String title = "Dan's Awesome Title";
        Date releaseDate = new Date();
        double rating = 8.1;

        MovieInfoImpl movieInfoImpl = new MovieInfoImpl(pictureUrl, title, releaseDate, rating);
        movieInfoList.add(movieInfoImpl);

        when(mockNowPlayingInfo.getMovies()).thenReturn(movieInfoList);

        ArgumentCaptor<Runnable> argumentCaptorRunnable = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<List<MovieInfo>> argumentCaptorListMovieInfo = ArgumentCaptor.forClass(List.class);

        //
        //Act
        //
        loadDataThread.run();

        //Act-2
        verify(mockHandler).post(argumentCaptorRunnable.capture());
        argumentCaptorRunnable.getValue().run();

        //
        //Assert
        //
        verify(mockNowPlayingResponseModel).infoLoaded(argumentCaptorListMovieInfo.capture());

        verify(mockServiceGateway).getNowPlaying(1);
        verify(mockHandler).post(any(Runnable.class));
        verify(mockNowPlayingResponseModel).infoLoaded(movieInfoList);

        List<MovieInfo> movieInfoListToVerify = argumentCaptorListMovieInfo.getValue();
        assertThat(movieInfoListToVerify.size()).isEqualTo(1);

        MovieInfo movieInfo = movieInfoListToVerify.get(0);
        assertThat(movieInfo.getPictureUrl()).isEqualToIgnoringCase(pictureUrl);
        assertThat(movieInfo.getTitle()).isEqualToIgnoringCase(title);
        assertThat(movieInfo.getReleaseDate()).isEqualTo(releaseDate);
        assertThat(movieInfo.getRating()).isEqualTo(rating);
    }

    @Test
    public void loadData_fails() throws Exception {
        //
        //Arrange
        //
        Handler mockHandler = Mockito.mock(Handler.class);
        ServiceGateway mockServiceGateway = Mockito.mock(ServiceGateway.class);
        NowPlayingResponseModel mockNowPlayingResponseModel = Mockito.mock(NowPlayingResponseModel.class);

        NowPlayingInteractorImpl.LoadDataThread loadDataThread =
                new NowPlayingInteractorImpl.LoadDataThread(
                        1,
                        mockHandler,
                        mockServiceGateway,
                        mockNowPlayingResponseModel
                );

        when(mockServiceGateway.getNowPlaying(1)).thenReturn(null);

        ArgumentCaptor<Runnable> argumentCaptorRunnable = ArgumentCaptor.forClass(Runnable.class);

        //
        //Act
        //
        loadDataThread.run();

        //Act-2
        verify(mockHandler).post(argumentCaptorRunnable.capture());
        argumentCaptorRunnable.getValue().run();

        //
        //Assert
        //
        verify(mockServiceGateway).getNowPlaying(1);
        verify(mockHandler).post(any(Runnable.class));
        verify(mockNowPlayingResponseModel).errorLoadingInfoData();
    }
}