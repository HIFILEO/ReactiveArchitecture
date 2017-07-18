package com.example.mvvmreactive.presenter;

import android.os.Bundle;

import com.example.mvvmreactive.categories.UnitTest;
import com.example.mvvmreactive.interactor.NowPlayingInteractor;
import com.example.mvvmreactive.model.MovieInfo;
import com.example.mvvmreactive.model.MovieViewInfo;
import com.example.mvvmreactive.rx.RxJavaTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Category(UnitTest.class)
public class NowPlayingPresenterImplTest extends RxJavaTest {
    @Mock
    NowPlayingViewModel mockNowPlayingViewModel;
    @Mock
    NowPlayingInteractor mockNowPlayingInteractor;

    @Before
    public void setUp() {
        super.setUp();
        initMocks(this);
    }

    @Test
    public void onCreate_firstLaunch() {
        //
        //Arrange
        //
        NowPlayingPresenterImpl spyNowPlayingPresenterImpl = Mockito.spy(
                new NowPlayingPresenterImpl(mockNowPlayingViewModel, mockNowPlayingInteractor));
        doNothing().when(spyNowPlayingPresenterImpl).loadMoreInfo();

        //
        //Act
        //
        spyNowPlayingPresenterImpl.onCreate(null);

        //
        //Assert
        //
        verify(mockNowPlayingInteractor).setNowPlayingResponseModel(spyNowPlayingPresenterImpl);
        verify(mockNowPlayingViewModel).showInProgress(true);
        verify(mockNowPlayingViewModel).createAdapter(null);

        verify(spyNowPlayingPresenterImpl).loadMoreInfo();
        assertThat(NowPlayingPresenterImpl.requestCache).isNull();
        assertThat(NowPlayingPresenterImpl.pageNumber).isEqualTo(0);
    }

    @Test
    public void onCreate_restore_noSubscribe() {
        //
        //Arrange
        //
        NowPlayingPresenterImpl spyNowPlayingPresenterImpl = Mockito.spy(
                new NowPlayingPresenterImpl(mockNowPlayingViewModel, mockNowPlayingInteractor));

        Bundle mockBundle = Mockito.mock(Bundle.class);

        //
        //Act
        //
        spyNowPlayingPresenterImpl.onCreate(mockBundle);

        //
        //Assert
        //
        verify(mockNowPlayingInteractor).setNowPlayingResponseModel(spyNowPlayingPresenterImpl);
        verify(mockNowPlayingViewModel).showInProgress(true);
        verify(mockNowPlayingViewModel).createAdapter(mockBundle);
        verify(mockNowPlayingViewModel).restoreState(mockBundle);

        verify(spyNowPlayingPresenterImpl, never()).loadMoreInfo();
        verify(spyNowPlayingPresenterImpl, never()).subscribeToData();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onCreate_restore_subscribe() {
        //
        //Arrange
        //
        NowPlayingPresenterImpl spyNowPlayingPresenterImpl = Mockito.spy(
                new NowPlayingPresenterImpl(mockNowPlayingViewModel, mockNowPlayingInteractor));
        doNothing().when(spyNowPlayingPresenterImpl).subscribeToData();

        Bundle mockBundle = Mockito.mock(Bundle.class);
        NowPlayingPresenterImpl.requestCache = Mockito.mock(Observable.class);

        //
        //Act
        //
        spyNowPlayingPresenterImpl.onCreate(mockBundle);

        //
        //Assert
        //
        verify(mockNowPlayingInteractor).setNowPlayingResponseModel(spyNowPlayingPresenterImpl);
        verify(mockNowPlayingViewModel).showInProgress(true);
        verify(mockNowPlayingViewModel).createAdapter(mockBundle);
        verify(mockNowPlayingViewModel).restoreState(mockBundle);

        verify(spyNowPlayingPresenterImpl, never()).loadMoreInfo();
        verify(spyNowPlayingPresenterImpl).subscribeToData();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void TranslateForPresenterFunctionTest() throws Exception {
        //
        //Arrange
        //
        TestObserver<List<MovieViewInfo>> testObserver;

        NowPlayingPresenterImpl.TranslateForPresenterFunction translateForPresenterFunction =
                new NowPlayingPresenterImpl.TranslateForPresenterFunction();

        MovieInfo mockMovieInfo1 = Mockito.mock(MovieInfo.class);
        when(mockMovieInfo1.getTitle()).thenReturn("Dan");

        MovieInfo mockMovieInfo2 = Mockito.mock(MovieInfo.class);
        when(mockMovieInfo2.getTitle()).thenReturn("Leo");

        List<MovieInfo> movieInfoList = new ArrayList<>();
        movieInfoList.add(mockMovieInfo1);
        movieInfoList.add(mockMovieInfo2);

        //
        //Act
        //
        testObserver = ((Observable<List<MovieViewInfo>>)  translateForPresenterFunction.apply(movieInfoList)).test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        List<MovieViewInfo> movieViewInfoList = (List<MovieViewInfo>) testObserver.getEvents().get(0).get(0);
        assertThat(movieViewInfoList.size()).isEqualTo(2);
        assertThat(movieViewInfoList.get(0).getTitle()).isEqualToIgnoringCase("Dan");
        assertThat(movieViewInfoList.get(1).getTitle()).isEqualToIgnoringCase("Leo");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void UiUpdateConsumerTest() throws Exception {
        //
        //Arrange
        //
        NowPlayingPresenterImpl.UiUpdateConsumer uiUpdateConsumer =
                new NowPlayingPresenterImpl.UiUpdateConsumer(mockNowPlayingViewModel);

        List<MovieViewInfo> mockMovieViewInfoList = Mockito.mock(List.class);

        //
        //Act
        //
        uiUpdateConsumer.accept(mockMovieViewInfoList);

        //
        //Assert
        //
        verify(mockNowPlayingViewModel).addToAdapter(mockMovieViewInfoList);
        verify(mockNowPlayingViewModel).showInProgress(false);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void UiUpdateOnErrorConsumerTest() throws Exception {
        //
        //Arrange
        //
        NowPlayingPresenter mockNowPlayingPresenter = Mockito.mock(NowPlayingPresenter.class);

        NowPlayingPresenterImpl.UiUpdateOnErrorConsumer uiUpdateOnErrorConsumer =
                new NowPlayingPresenterImpl.UiUpdateOnErrorConsumer(mockNowPlayingPresenter, mockNowPlayingViewModel);

        NowPlayingPresenterImpl.requestCache = Mockito.mock(Observable.class);

        //
        //Act
        //
        uiUpdateOnErrorConsumer.accept(Mockito.mock(Throwable.class));

        //
        //Assert
        //
        assertThat(NowPlayingPresenterImpl.requestCache).isNull();
        verify(mockNowPlayingViewModel).showError();
        verify(mockNowPlayingPresenter).loadMoreInfo();
    }

}