package com.example.mvvmreactive.viewmodel;

import com.example.mvvmreactive.categories.UnitTest;
import com.example.mvvmreactive.gateway.ServiceGateway;
import com.example.mvvmreactive.gateway.ServiceGatewayImpl;
import com.example.mvvmreactive.model.MovieInfo;
import com.example.mvvmreactive.model.MovieInfoImpl;
import com.example.mvvmreactive.model.MovieViewInfo;
import com.example.mvvmreactive.model.NowPlayingInfo;
import com.example.mvvmreactive.model.NowPlayingInfoImpl;
import com.example.mvvmreactive.rx.RxJavaTest;
import com.example.mvvmreactive.service.ServiceResponse;
import com.example.mvvmreactive.util.TestResourceFileHelper;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.observers.TestObserver;

import static com.ibm.icu.impl.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Category(UnitTest.class)
public class NowPlayingViewModelTest extends RxJavaTest {
    @Mock
    ServiceGateway mockServiceGateway;

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
        TestObserver<Boolean> testObserver;
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockServiceGateway);

        //
        //Act
        //
        testObserver = nowPlayingViewModel.isFirstLoadInProgress().test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        Boolean isFirstLoadInProgress = (Boolean) testObserver.getEvents().get(0).get(0);
        assertThat(isFirstLoadInProgress).isTrue();
    }

    @Test
    public void isFirstLoadInProgress_false() throws Exception {
        //
        //Arrange
        //
        TestObserver<Boolean> testObserver;
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockServiceGateway);

        //
        //Act
        //
        testObserver = nowPlayingViewModel.isFirstLoadInProgress().test();
        nowPlayingViewModel.getMovieViewInfo().test();
        testScheduler.triggerActions();
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(2);

        Boolean isFirstLoadInProgress = (Boolean) testObserver.getEvents().get(0).get(1);
        assertThat(isFirstLoadInProgress).isFalse();
    }

    @Test
    public void getMovieViewInfoList() throws Exception {
        //
        //Arrange
        //
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockServiceGateway);

        //
        //Act
        //
        List<MovieViewInfo> movieViewInfos = nowPlayingViewModel.getMovieViewInfoList();

        //
        //Assert
        //
        assertThat(movieViewInfos.size()).isEqualTo(0);
    }

    @Test
    public void loadMoreInfo_isEmpty() throws Exception {
        //
        //Arrange
        //
        TestObserver<List<MovieViewInfo>> testObserver;
        TestObserver<Boolean> firstLoadTestObserver;

        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockServiceGateway);

        //
        //Act
        //
        firstLoadTestObserver = nowPlayingViewModel.isFirstLoadInProgress().test();

        nowPlayingViewModel.loadMoreInfo().test();
        testObserver = nowPlayingViewModel.loadMoreInfo().test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertNoValues();

        firstLoadTestObserver.assertNoErrors();
        firstLoadTestObserver.assertValueCount(1);

        Boolean isFirstLoadInProgress = (Boolean) firstLoadTestObserver.getEvents().get(0).get(0);
        assertThat(isFirstLoadInProgress).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void loadMoreInfo_hasValue() throws Exception {
        //
        //Arrange
        //
        TestObserver<List<MovieViewInfo>> testObserver;
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockServiceGateway);

        //
        //Act
        //
        nowPlayingViewModel.getMovieViewInfo().test();
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        testObserver = nowPlayingViewModel.loadMoreInfo().test();
        testScheduler.triggerActions();
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        ArrayList<MovieViewInfo> arrayList = (ArrayList<MovieViewInfo>) testObserver.getEvents().get(0).get(0);
        MovieViewInfo movieViewInfo = arrayList.get(0);
        assertThat(movieViewInfo.getPictureUrl()).isEqualToIgnoringCase("www.mypicture.com");
        assertThat(movieViewInfo.getTitle()).isEqualToIgnoringCase("title");
    }

    @Test
    public void getMovieViewInfo_hasValue() throws Exception {
        //
        //Arrange
        //
        TestObserver<List<MovieViewInfo>> testObserver;
        TestObserver<Boolean> firstLoadTestObserver;
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockServiceGateway);

        //
        //Act
        //
        firstLoadTestObserver = nowPlayingViewModel.isFirstLoadInProgress().test();
        testObserver = nowPlayingViewModel.getMovieViewInfo().test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        firstLoadTestObserver.assertNoErrors();
        firstLoadTestObserver.assertValueCount(1);
        testObserver.assertNoErrors();
        testObserver.assertNotComplete();

        Boolean isFirstLoadInProgress = (Boolean) firstLoadTestObserver.getEvents().get(0).get(0);
        assertThat(isFirstLoadInProgress).isTrue();
    }

    @Test
    public void getMovieViewInfo_isEmpty() throws Exception {
        //
        //Arrange
        //
        TestObserver<List<MovieViewInfo>> testObserver;
        TestObserver<Boolean> firstLoadTestObserver;
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockServiceGateway);

        //
        //Act
        //
        firstLoadTestObserver = nowPlayingViewModel.isFirstLoadInProgress().test();
        nowPlayingViewModel.getMovieViewInfo().test();
        testScheduler.triggerActions();
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        testObserver = nowPlayingViewModel.getMovieViewInfo().test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        firstLoadTestObserver.assertNoErrors();
        firstLoadTestObserver.assertValueCount(2);
        testObserver.assertNoErrors();
        testObserver.assertEmpty();

        Boolean isFirstLoadInProgress = (Boolean) firstLoadTestObserver.getEvents().get(0).get(1);
        assertThat(isFirstLoadInProgress).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getMovieViewInfo_usesCache() throws Exception {
        //
        //Arrange
        //
        TestObserver<List<MovieViewInfo>> testObserver1;
        TestObserver<List<MovieViewInfo>> testObserver2;
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockServiceGateway);

        //
        //Act
        //
        testObserver1 = nowPlayingViewModel.getMovieViewInfo().test();
        testObserver2 = nowPlayingViewModel.getMovieViewInfo().test();

        testScheduler.triggerActions();
        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS);


        //
        //Assert
        //
        testObserver1.assertNoErrors();
        testObserver1.assertComplete();
        testObserver1.assertValueCount(1);

        testObserver2.assertNoErrors();
        testObserver2.assertComplete();
        testObserver2.assertValueCount(1);

        ArrayList<MovieViewInfo> arrayList = (ArrayList<MovieViewInfo>) testObserver1.getEvents().get(0).get(0);
        MovieViewInfo movieViewInfo = arrayList.get(0);
        assertThat(movieViewInfo.getPictureUrl()).isEqualToIgnoringCase("www.mypicture.com");
        assertThat(movieViewInfo.getTitle()).isEqualToIgnoringCase("title");

        arrayList = (ArrayList<MovieViewInfo>) testObserver2.getEvents().get(0).get(0);
        movieViewInfo = arrayList.get(0);
        assertThat(movieViewInfo.getPictureUrl()).isEqualToIgnoringCase("www.mypicture.com");
        assertThat(movieViewInfo.getTitle()).isEqualToIgnoringCase("title");
    }

    @Test
    public void translateForUiFunction() throws Exception {
        //
        //Arrange
        //


        //
        //Act
        //

        //
        //Assert
        //
    }
}