package com.example.mvpexample.interactor;

import android.os.Handler;

import com.example.mvpexample.categories.UnitTest;
import com.example.mvpexample.gateway.ServiceGateway;
import com.example.mvpexample.model.MovieInfo;
import com.example.mvpexample.model.MovieInfoImpl;
import com.example.mvpexample.model.NowPlayingInfo;
import com.example.mvpexample.rx.RxJavaTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class NowPlayingInteractorImplTest extends RxJavaTest {

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

        when(mockServiceGateway.getNowPlaying(1)).thenReturn(Observable.just(mockNowPlayingInfo));
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

        loadDataThread.setSleepMsec(0);

        //
        //Act
        //
        loadDataThread.run();
        testScheduler.triggerActions();

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

        when(mockServiceGateway.getNowPlaying(1))
                .thenReturn(Observable.<NowPlayingInfo>error(new Throwable("Test Error")));

        ArgumentCaptor<Runnable> argumentCaptorRunnable = ArgumentCaptor.forClass(Runnable.class);

        loadDataThread.setSleepMsec(0);

        //
        //Act
        //
        loadDataThread.run();
        testScheduler.triggerActions();

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

    @Test
    public void registerCallback_noData() {
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
                        null
                );

        //
        //Act
        //
        loadDataThread.registerCallback(mockNowPlayingResponseModel);

        //
        //Assert
        //
        assertThat(loadDataThread.nowPlayingResponseModelWeakReference).isNotNull();
        assertThat(loadDataThread.nowPlayingResponseModelWeakReference.get()).isNotNull();
        assertThat(loadDataThread.nowPlayingResponseModelWeakReference.get())
                .isEqualTo(mockNowPlayingResponseModel);
        assertThat(loadDataThread.runnableCache).isNull();

        verify(mockHandler, never()).post(any(Runnable.class));
    }

    @Test
    public void registerCallback_withData() {
        //
        //Arrange
        //
        Handler mockHandler = Mockito.mock(Handler.class);
        ServiceGateway mockServiceGateway = Mockito.mock(ServiceGateway.class);
        NowPlayingResponseModel mockNowPlayingResponseModel = Mockito.mock(NowPlayingResponseModel.class);
        Runnable mockRunnable = Mockito.mock(Runnable.class);

        NowPlayingInteractorImpl.LoadDataThread loadDataThread =
                new NowPlayingInteractorImpl.LoadDataThread(
                        1,
                        mockHandler,
                        mockServiceGateway,
                        null
                );
        loadDataThread.runnableCache = mockRunnable;

        ArgumentCaptor<Runnable> argumentCaptorRunnable = ArgumentCaptor.forClass(Runnable.class);

        //
        //Act
        //
        loadDataThread.registerCallback(mockNowPlayingResponseModel);

        //
        //Assert
        //
        assertThat(loadDataThread.nowPlayingResponseModelWeakReference).isNotNull();
        assertThat(loadDataThread.nowPlayingResponseModelWeakReference.get()).isNotNull();
        assertThat(loadDataThread.nowPlayingResponseModelWeakReference.get())
                .isEqualTo(mockNowPlayingResponseModel);

        verify(mockHandler).post(argumentCaptorRunnable.capture());
        assertThat(argumentCaptorRunnable.getValue()).isEqualTo(mockRunnable);

        assertThat(loadDataThread.runnableCache).isNull();
    }

    @Test
    public void unregisterCallback() {
        //
        //Arrange
        //
        Handler mockHandler = Mockito.mock(Handler.class);
        ServiceGateway mockServiceGateway = Mockito.mock(ServiceGateway.class);
        NowPlayingResponseModel mockNowPlayingResponseModel = Mockito.mock(NowPlayingResponseModel.class);
        WeakReference<NowPlayingResponseModel> mockNowPlayingResponseModelWeakReference
                = Mockito.mock(WeakReference.class);

        NowPlayingInteractorImpl.LoadDataThread loadDataThread =
                new NowPlayingInteractorImpl.LoadDataThread(
                        1,
                        mockHandler,
                        mockServiceGateway,
                        mockNowPlayingResponseModel
                );
        loadDataThread.nowPlayingResponseModelWeakReference = mockNowPlayingResponseModelWeakReference;

        //
        //Act
        //
        loadDataThread.unregisterCallback();

        //
        //Assert
        //
        verify(mockNowPlayingResponseModelWeakReference).clear();
        assertThat(loadDataThread.nowPlayingResponseModelWeakReference).isNull();
    }
}