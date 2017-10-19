package com.example.reactivearchitecture.viewmodel;

import android.app.Application;

import com.example.reactivearchitecture.categories.UnitTest;
import com.example.reactivearchitecture.gateway.ServiceGateway;
import com.example.reactivearchitecture.interactor.NowPlayingInteractor;
import com.example.reactivearchitecture.model.AdapterCommandType;
import com.example.reactivearchitecture.model.MovieInfo;
import com.example.reactivearchitecture.model.MovieInfoImpl;
import com.example.reactivearchitecture.model.MovieViewInfo;
import com.example.reactivearchitecture.model.UiModel;
import com.example.reactivearchitecture.model.action.Action;
import com.example.reactivearchitecture.model.action.ScrollAction;
import com.example.reactivearchitecture.model.event.ScrollEvent;
import com.example.reactivearchitecture.model.result.Result;
import com.example.reactivearchitecture.model.result.ScrollResult;
import com.example.reactivearchitecture.rx.RxJavaTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Category(UnitTest.class)
public class NowPlayingViewModelTest extends RxJavaTest {
    @Mock
    ServiceGateway mockServiceGateway;

    @Mock
    Application mockApplication;

    @Mock
    NowPlayingInteractor mockNowPlayingInteractor;

    @Mock
    TestTransformer mockTestTransformer;

    MovieInfo movieInfo = new MovieInfoImpl(
            "www.url.com",
            "Dan The Man",
            new Date(),
            9d);

    @Before
    public void setUp() {
        super.setUp();
        initMocks(this);

        //Mock Publish() return.
        when(mockNowPlayingInteractor.processAction(any(Observable.class))).thenAnswer(new Answer<Observable<Result>>() {
            @Override
            public Observable<Result> answer(InvocationOnMock invocation) throws Throwable {
                Observable<Action> actionObservable = (Observable<Action>) invocation.getArguments()[0];

                return actionObservable.flatMap(new Function<Action, ObservableSource<Result>>() {
                    @Override
                    public ObservableSource<Result> apply(@NonNull Action action) throws Exception {
                        return mockTestTransformer.transform(action);
                    }
                });
            }
        });

    }

    @Test
    public void initState() {
        //
        //Arrange
        //
        TestObserver<UiModel> testObserver;
        TestNowPlayingViewModel nowPlayingViewModel = new TestNowPlayingViewModel(mockApplication,
                mockServiceGateway, mockNowPlayingInteractor);

        //
        //Act
        //
        testObserver = nowPlayingViewModel.getUiModels().test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        UiModel uiModel = (UiModel) testObserver.getEvents().get(0).get(0);
        assertThat(uiModel).isNotNull();
        assertThat(uiModel.isFirstTimeLoad()).isTrue();
        assertThat(uiModel.getAdapterCommandType()).isEqualTo(AdapterCommandType.DO_NOTHING);
        assertThat(uiModel.getCurrentList()).isEmpty();
        assertThat(uiModel.getResultList()).isNull();
        assertThat(uiModel.getFailureMsg()).isNull();
        assertThat(uiModel.isEnableScrollListener()).isFalse();
        assertThat(uiModel.getPageNumber()).isEqualTo(0);
    }

    @Test
    public void inFlightState() {
        //
        //Arrange
        //
        TestObserver<UiModel> testObserver;
        TestNowPlayingViewModel nowPlayingViewModel = new TestNowPlayingViewModel(mockApplication,
                mockServiceGateway, mockNowPlayingInteractor);

        final int pageNumber = 1;
        ScrollEvent scrollEvent = new ScrollEvent();
        scrollEvent.setPageNumber(pageNumber);

        ScrollResult scrollResult = ScrollResult.inFlight(pageNumber);

        ArgumentCaptor<Action> argumentCaptor = ArgumentCaptor.forClass(Action.class);
        when(mockTestTransformer.transform(argumentCaptor.capture())).thenReturn(Observable.just((Result) scrollResult));


        //
        //Act
        //
        testObserver = nowPlayingViewModel.getUiModels().test();
        nowPlayingViewModel.processUiEvent(scrollEvent);
        testScheduler.triggerActions();

        //
        //Assert
        //
        //Observer Test
        testObserver.assertNoErrors();
        testObserver.assertValueCount(2);

        //Model Test
        UiModel uiModel = (UiModel) testObserver.getEvents().get(0).get(1);
        assertThat(uiModel).isNotNull();
        assertThat(uiModel.isFirstTimeLoad()).isTrue();
        assertThat(uiModel.getAdapterCommandType()).isEqualTo(AdapterCommandType.DO_NOTHING);
        assertThat(uiModel.getCurrentList()).isEmpty();
        assertThat(uiModel.getResultList()).isNull();
        assertThat(uiModel.getFailureMsg()).isNull();
        assertThat(uiModel.isEnableScrollListener()).isFalse();
        assertThat(uiModel.getPageNumber()).isEqualTo(pageNumber);

        //Action translation test
        Action action = argumentCaptor.getValue();
        assertThat(action).isNotNull();
        assertThat(action).isInstanceOf(ScrollAction.class);

        ScrollAction scrollAction = (ScrollAction) action;
        assertThat(scrollAction.getPageNumber()).isEqualTo(pageNumber);
    }

    @Test
    public void inSuccessState() {
        //
        //Arrange
        //
        TestObserver<UiModel> testObserver;
        TestNowPlayingViewModel nowPlayingViewModel = new TestNowPlayingViewModel(mockApplication,
                mockServiceGateway, mockNowPlayingInteractor);

        final int pageNumber = 1;
        ScrollEvent scrollEvent = new ScrollEvent();
        scrollEvent.setPageNumber(pageNumber);

        ScrollResult scrollResultInFlight = ScrollResult.inFlight(pageNumber);

        List<MovieInfo> movieInfoList = new ArrayList<>();
        movieInfoList.add(movieInfo);

        ScrollResult scrollResultSuccess = ScrollResult.sucess(pageNumber, movieInfoList);

        ArgumentCaptor<Action> argumentCaptor = ArgumentCaptor.forClass(Action.class);
        when(mockTestTransformer.transform(argumentCaptor.capture())).thenReturn(Observable.just(
                (Result) scrollResultInFlight,
                (Result) scrollResultSuccess));

        //
        //Act
        //
        testObserver = nowPlayingViewModel.getUiModels().test();
        nowPlayingViewModel.processUiEvent(scrollEvent);
        testScheduler.triggerActions();

        //
        //Assert
        //
        //Observer Test
        testObserver.assertNoErrors();
        testObserver.assertValueCount(3);

        //Model Test
        UiModel uiModel = (UiModel) testObserver.getEvents().get(0).get(2);
        assertThat(uiModel).isNotNull();
        assertThat(uiModel.isFirstTimeLoad()).isFalse();
        assertThat(uiModel.getAdapterCommandType()).isEqualTo(AdapterCommandType.ADD_DATA);
        assertThat(uiModel.getCurrentList()).isNotEmpty();
        assertThat(uiModel.getCurrentList()).hasSize(1);
        assertThat(uiModel.getResultList()).isNotEmpty();
        assertThat(uiModel.getResultList()).hasSize(1);
        assertThat(uiModel.getFailureMsg()).isNull();
        assertThat(uiModel.isEnableScrollListener()).isTrue();
        assertThat(uiModel.getPageNumber()).isEqualTo(pageNumber);

        //Test List Data
        MovieViewInfo movieViewInfo = uiModel.getResultList().get(0);
        assertThat(movieViewInfo.getPictureUrl()).isEqualToIgnoringCase(movieInfo.getPictureUrl());
        assertThat(movieViewInfo.getTitle()).isEqualToIgnoringCase(movieInfo.getTitle());
        assertThat(movieViewInfo.getRating()).isEqualToIgnoringCase(String.valueOf(Math.round(movieInfo.getRating()) + "/10"));
        assertThat(movieViewInfo.isHighRating()).isTrue();

    }

    private class TestTransformer {
        Observable<Result> transform(Action action) {
            return Observable.empty();
        }
    }
}