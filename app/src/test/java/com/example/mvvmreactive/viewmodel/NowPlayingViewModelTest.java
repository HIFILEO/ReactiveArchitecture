package com.example.mvvmreactive.viewmodel;

import com.example.mvvmreactive.categories.UnitTest;
import com.example.mvvmreactive.gateway.ServiceGateway;
import com.example.mvvmreactive.model.MovieViewInfo;
import com.example.mvvmreactive.rx.RxJavaTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

@Category(UnitTest.class)
public class NowPlayingViewModelTest extends RxJavaTest {
    @Mock
    ServiceGateway mockServiceGateway;

    @Before
    public void setUp() {
        super.setUp();
        initMocks(this);
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
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        Boolean value = (Boolean) testObserver.getEvents().get(0).get(0);
        assertThat(value).isTrue();
    }

    @Test
    public void isFirstLoadInProgress_false() throws Exception {
        //
        //Arrange
        //
        TestObserver<Boolean> testObserver;
        NowPlayingViewModel nowPlayingViewModel = new NowPlayingViewModel(mockServiceGateway);
        NowPlayingViewModel spyNowPlayingViewModel = Mockito.spy(nowPlayingViewModel);

        Dan L - you left off here.
        //
        //Act
        //
        testObserver = nowPlayingViewModel.isFirstLoadInProgress().test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        Boolean value = (Boolean) testObserver.getEvents().get(0).get(0);
        assertThat(value).isTrue();
    }

    @Test
    public void getMovieViewInfoList() throws Exception {

    }

    @Test
    public void loadMoreInfo() throws Exception {

    }

    @Test
    public void getMovieViewInfo() throws Exception {

    }

}