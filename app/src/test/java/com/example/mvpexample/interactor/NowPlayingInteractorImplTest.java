package com.example.mvpexample.interactor;

import android.os.Handler;

import com.example.mvpexample.categories.UnitTest;
import com.example.mvpexample.gateway.ServiceGateway;
import com.example.mvpexample.model.MovieInfo;
import com.example.mvpexample.model.MovieInfoImpl;
import com.example.mvpexample.model.NowPlayingInfo;
import com.example.mvpexample.model.NowPlayingInfoImpl;
import com.example.mvpexample.rx.RxJavaTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class NowPlayingInteractorImplTest extends RxJavaTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    @SuppressWarnings("unchecked")
    public void MovieListFetcherTest() throws Exception {
        //
        //Arrange
        //
        TestObserver<List<MovieInfo>> testObserver;

        NowPlayingInfo nowPlayingInfo = Mockito.mock(NowPlayingInfo.class);
        when(nowPlayingInfo.getMovies()).thenReturn(new ArrayList<MovieInfo>());

        NowPlayingInteractorImpl.MovieListFetcher movieListFetcher = new NowPlayingInteractorImpl.MovieListFetcher();

        //
        //Act
        //
        testObserver =  ((Observable<List<MovieInfo>>) movieListFetcher.apply(nowPlayingInfo)).test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        assertThat((List<MovieInfo>) testObserver.getEvents().get(0).get(0)).isEmpty();
    }

    @Test
    public void MovieListFetcherTest_OnError() throws Exception {
        //
        //Arrange
        //
        NowPlayingInfo nowPlayingInfo = Mockito.mock(NowPlayingInfo.class);
        when(nowPlayingInfo.getMovies()).thenReturn(null);

        NowPlayingInteractorImpl.MovieListFetcher movieListFetcher = new NowPlayingInteractorImpl.MovieListFetcher();

        //
        //Act / Assert
        //
        exception.expectMessage("The item is null");
        ((Observable<List<MovieInfo>>) movieListFetcher.apply(nowPlayingInfo)).test();
    }
}