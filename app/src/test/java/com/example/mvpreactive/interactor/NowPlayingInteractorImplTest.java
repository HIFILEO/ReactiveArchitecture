package com.example.mvpreactive.interactor;

import com.example.mvpreactive.categories.UnitTest;
import com.example.mvpreactive.model.MovieInfo;
import com.example.mvpreactive.model.NowPlayingInfo;
import com.example.mvpreactive.rx.RxJavaTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
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