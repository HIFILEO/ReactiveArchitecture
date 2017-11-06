/*
Copyright 2017 LEO LLC

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.example.reactivearchitecture.nowplaying.interactor;

import com.example.reactivearchitecture.nowplaying.controller.ServiceController;
import com.example.reactivearchitecture.nowplaying.model.FilterManager;
import com.example.reactivearchitecture.nowplaying.model.FilterTransformer;
import com.example.reactivearchitecture.nowplaying.model.MovieInfo;
import com.example.reactivearchitecture.nowplaying.model.MovieInfoImpl;
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfo;
import com.example.reactivearchitecture.nowplaying.model.NowPlayingInfoImpl;
import com.example.reactivearchitecture.core.model.action.Action;
import com.example.reactivearchitecture.nowplaying.model.action.FilterAction;
import com.example.reactivearchitecture.nowplaying.model.action.RestoreAction;
import com.example.reactivearchitecture.nowplaying.model.action.ScrollAction;
import com.example.reactivearchitecture.nowplaying.model.result.FilterResult;
import com.example.reactivearchitecture.nowplaying.model.result.RestoreResult;
import com.example.reactivearchitecture.nowplaying.model.result.Result;
import com.example.reactivearchitecture.nowplaying.model.result.ScrollResult;
import com.example.reactivearchitecture.rx.RxJavaTest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Test the business logic from the interactor.
 */
public class NowPlayingInteractorTest extends RxJavaTest {

    @Mock
    ServiceController mockServiceController;

    @Mock
    FilterManager mockFilterManager;

    FilterTransformer filterTransformer;

    final MovieInfo movieInfo = new MovieInfoImpl(
            "www.url.com",
            "Dan The Man",
            new Date(),
            9d);

    final int pageNumber = 1;
    final int totalPageNumber = 10;

    @Before
    public void setUp() {
        super.setUp();
        initMocks(this);

        when(mockFilterManager.filterList(anyList())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                return invocation.getArguments()[0];
            }
        });

        //Use real transformers.
        filterTransformer = new FilterTransformer(mockFilterManager);
    }

    @Test
    public void testScrollAction_pass() {
        //
        //Arrange
        //
        TestObserver<Result> testObserver;
        NowPlayingInteractor nowPlayingInteractor = new NowPlayingInteractor(mockServiceController, filterTransformer);

        List<MovieInfo> movieInfoList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            movieInfoList.add(movieInfo);
        }
        NowPlayingInfo nowPlayingInfo = new NowPlayingInfoImpl(movieInfoList, pageNumber,
                totalPageNumber);

        when(mockServiceController.getNowPlaying(anyInt())).thenReturn(
                Observable.just(nowPlayingInfo));

        //
        //Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just((Action) new ScrollAction(pageNumber))).test();
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(2);

        //IN_FLIGHT Test
        Result result = (Result) testObserver.getEvents().get(0).get(0);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(ScrollResult.class);

        ScrollResult scrollResult = (ScrollResult) result;
        assertThat(scrollResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(scrollResult.getError()).isNull();
        assertThat(scrollResult.getResult()).isNull();
        assertThat(scrollResult.getType()).isEqualTo(Result.ResultType.IN_FLIGHT);

        //SUCCESS
        Result resultSuccess = (Result) testObserver.getEvents().get(0).get(1);
        assertThat(resultSuccess).isNotNull();
        assertThat(resultSuccess).isInstanceOf(ScrollResult.class);

        ScrollResult scrollResultSuccess = (ScrollResult) resultSuccess;
        assertThat(scrollResultSuccess.getPageNumber()).isEqualTo(pageNumber);
        assertThat(scrollResultSuccess.getError()).isNull();
        assertThat(scrollResultSuccess.getResult()).isNotEmpty();
        assertThat(scrollResultSuccess.getResult()).hasSize(5);
        assertThat(scrollResultSuccess.getType()).isEqualTo(Result.ResultType.SUCCESS);

        for (int i =0; i < 5; i++) {
            assertThat(scrollResultSuccess.getResult().get(i)).isEqualTo(movieInfo);
        }
    }

    @Test
    public void testScrollAction_fail() {
        //
        //Arrange
        //
        TestObserver<Result> testObserver;
        NowPlayingInteractor nowPlayingInteractor = new NowPlayingInteractor(mockServiceController, filterTransformer);

        List<MovieInfo> movieInfoList = new ArrayList<MovieInfo>();
        for (int i = 0; i < 5; i++) {
            movieInfoList.add(movieInfo);
        }
        NowPlayingInfo nowPlayingInfo = new NowPlayingInfoImpl(movieInfoList, pageNumber, totalPageNumber);

        String errorMessage = "Error Message";

        TestFailure testFailure = new TestFailure(nowPlayingInfo, errorMessage);
        when(mockServiceController.getNowPlaying(1)).thenReturn(testFailure.getNowPlayingInfoObservable());

        //
        //Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just((Action) new ScrollAction(pageNumber))).test();
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(3);

        //IN_FLIGHT Test
        Result result = (Result) testObserver.getEvents().get(0).get(0);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(ScrollResult.class);

        ScrollResult scrollResult = (ScrollResult) result;
        assertThat(scrollResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(scrollResult.getError()).isNull();
        assertThat(scrollResult.getResult()).isNull();
        assertThat(scrollResult.getType()).isEqualTo(Result.ResultType.IN_FLIGHT);

        //FAILURE
        result = (Result) testObserver.getEvents().get(0).get(1);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(ScrollResult.class);

        scrollResult = (ScrollResult) result;
        assertThat(scrollResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(scrollResult.getError()).isNotNull();
        assertThat(scrollResult.getError().getMessage()).isEqualTo(errorMessage);
        assertThat(scrollResult.getResult()).isNull();
        assertThat(scrollResult.getType()).isEqualTo(Result.ResultType.FAILURE);

        ///SUCCESS
        result = (Result) testObserver.getEvents().get(0).get(2);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(ScrollResult.class);

        scrollResult = (ScrollResult) result;
        assertThat(scrollResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(scrollResult.getError()).isNull();
        assertThat(scrollResult.getResult()).isNotEmpty();
        assertThat(scrollResult.getResult()).hasSize(5);
        assertThat(scrollResult.getType()).isEqualTo(Result.ResultType.SUCCESS);

        for (int i =0; i < 5; i++) {
            assertThat(scrollResult.getResult().get(i)).isEqualTo(movieInfo);
        }
    }

    @Test
    public void testResultAction_pass() {
        //
        //Arrange
        //
        TestObserver<Result> testObserver;
        NowPlayingInteractor nowPlayingInteractor = new NowPlayingInteractor(mockServiceController, filterTransformer);

        List<MovieInfo> movieInfoList = new ArrayList<MovieInfo>();
        for (int i = 0; i < 5; i++) {
            movieInfoList.add(movieInfo);
        }
        NowPlayingInfo nowPlayingInfo = new NowPlayingInfoImpl(movieInfoList, pageNumber,
                totalPageNumber);

        when(mockServiceController.getNowPlaying(anyInt())).thenReturn(
                Observable.just(nowPlayingInfo));

        //
        //Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just((Action) new RestoreAction(pageNumber))).test();
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(2);

        //IN_FLIGHT Test
        Result result = (Result) testObserver.getEvents().get(0).get(0);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(RestoreResult.class);

        RestoreResult restoreResult = (RestoreResult) result;
        assertThat(restoreResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(restoreResult.getError()).isNull();
        assertThat(restoreResult.getResult()).isNull();
        assertThat(restoreResult.getType()).isEqualTo(Result.ResultType.IN_FLIGHT);

        //SUCCESS
        result = (Result) testObserver.getEvents().get(0).get(1);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(RestoreResult.class);

        restoreResult = (RestoreResult) result;
        assertThat(restoreResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(restoreResult.getError()).isNull();
        assertThat(restoreResult.getResult()).isNotEmpty();
        assertThat(restoreResult.getResult()).hasSize(5);
        assertThat(restoreResult.getType()).isEqualTo(Result.ResultType.SUCCESS);
    }

    @Test
    public void testResultAction_fail() {
        //
        //Arrange
        //
        TestObserver<Result> testObserver;
        NowPlayingInteractor nowPlayingInteractor = new NowPlayingInteractor(mockServiceController, filterTransformer);

        List<MovieInfo> movieInfoList = new ArrayList<MovieInfo>();
        for (int i = 0; i < 5; i++) {
            movieInfoList.add(movieInfo);
        }
        NowPlayingInfo nowPlayingInfo = new NowPlayingInfoImpl(movieInfoList, pageNumber, totalPageNumber);

        String errorMessage = "Error Message";

        TestFailure testFailure = new TestFailure(nowPlayingInfo, errorMessage);
        when(mockServiceController.getNowPlaying(1)).thenReturn(testFailure.getNowPlayingInfoObservable());

        //
        //Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just((Action) new RestoreAction(pageNumber))).test();
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(3);

        //IN_FLIGHT Test
        Result result = (Result) testObserver.getEvents().get(0).get(0);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(RestoreResult.class);

        RestoreResult restoreResult = (RestoreResult) result;
        assertThat(restoreResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(restoreResult.getError()).isNull();
        assertThat(restoreResult.getResult()).isNull();
        assertThat(restoreResult.getType()).isEqualTo(Result.ResultType.IN_FLIGHT);

        //FAILURE
        result = (Result) testObserver.getEvents().get(0).get(1);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(RestoreResult.class);

        restoreResult = (RestoreResult) result;
        assertThat(restoreResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(restoreResult.getError()).isNotNull();
        assertThat(restoreResult.getError().getMessage()).isEqualTo(errorMessage);
        assertThat(restoreResult.getResult()).isNull();
        assertThat(restoreResult.getType()).isEqualTo(Result.ResultType.FAILURE);

        ///SUCCESS
        result = (Result) testObserver.getEvents().get(0).get(2);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(RestoreResult.class);

        restoreResult = (RestoreResult) result;
        assertThat(restoreResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(restoreResult.getError()).isNull();
        assertThat(restoreResult.getResult()).isNotEmpty();
        assertThat(restoreResult.getResult()).hasSize(5);
        assertThat(restoreResult.getType()).isEqualTo(Result.ResultType.SUCCESS);

        for (int i =0; i < 5; i++) {
            assertThat(restoreResult.getResult().get(i)).isEqualTo(movieInfo);
        }
    }

    @Test
    public void testResultAction_pass_multiple_results() {
        //
        //Arrange
        //
        TestObserver<Result> testObserver;
        NowPlayingInteractor nowPlayingInteractor = new NowPlayingInteractor(mockServiceController, filterTransformer);

        int pageNumber = 2;

        List<MovieInfo> movieInfoList = new ArrayList<MovieInfo>();
        for (int i = 0; i < 5; i++) {
            movieInfoList.add(movieInfo);
        }
        NowPlayingInfo nowPlayingInfo = new NowPlayingInfoImpl(movieInfoList, pageNumber,
                totalPageNumber);

        when(mockServiceController.getNowPlaying(anyInt())).thenReturn(
                Observable.just(nowPlayingInfo));

        //
        //Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just((Action) new RestoreAction(pageNumber))).test();
        testScheduler.advanceTimeBy(8, TimeUnit.SECONDS);
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(4);
    }

    @Test
    public void testFilter_turnFilterOn() {
        //
        //Arrange
        //
        TestObserver<Result> testObserver;
        NowPlayingInteractor nowPlayingInteractor = new NowPlayingInteractor(mockServiceController, filterTransformer);

        List<MovieInfo> movieInfoList = new ArrayList<MovieInfo>();
        for (int i = 0; i < 5; i++) {
            movieInfoList.add(movieInfo);
        }

        when(mockFilterManager.isFilterOn()).thenReturn(true);
        when(mockFilterManager.getFullList()).thenReturn(movieInfoList);

        //
        //Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just((Action) new FilterAction(true))).test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        //IN_FLIGHT Test
        Result result = (Result) testObserver.getEvents().get(0).get(0);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(FilterResult.class);

        FilterResult filterResult = (FilterResult) result;
        assertThat(filterResult.getFilteredList().containsAll(movieInfoList));
        assertThat(filterResult.isFilterOn()).isTrue();
        assertThat(filterResult.getType()).isEqualTo(Result.ResultType.SUCCESS);
    }

    @Test
    public void testFilter_ScrollResults() {
        //
        //Arrange
        //
        TestObserver<Result> testObserver;
        NowPlayingInteractor nowPlayingInteractor = new NowPlayingInteractor(mockServiceController, filterTransformer);

        int pageNumber = 2;

        List<MovieInfo> movieInfoList = new ArrayList<MovieInfo>();
        for (int i = 0; i < 5; i++) {
            movieInfoList.add(movieInfo);
        }
        NowPlayingInfo nowPlayingInfo = new NowPlayingInfoImpl(movieInfoList, pageNumber,
                totalPageNumber);

        when(mockServiceController.getNowPlaying(anyInt())).thenReturn(
                Observable.just(nowPlayingInfo));

        when(mockFilterManager.isFilterOn()).thenReturn(true);
        when(mockFilterManager.filterList(anyList())).thenReturn(new ArrayList<MovieInfo>());

        //
        //Act
        //
        testObserver = nowPlayingInteractor.processAction(
                Observable.just((Action) new ScrollAction(pageNumber))).test();
        testScheduler.advanceTimeBy(4, TimeUnit.SECONDS);
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertNoErrors();
        testObserver.assertValueCount(2);

        //IN_FLIGHT Test
        Result result = (Result) testObserver.getEvents().get(0).get(0);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(ScrollResult.class);

        ScrollResult scrollResult = (ScrollResult) result;
        assertThat(scrollResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(scrollResult.getError()).isNull();
        assertThat(scrollResult.getResult()).isNull();
        assertThat(scrollResult.getType()).isEqualTo(Result.ResultType.IN_FLIGHT);

        //SUCCESS
        result = (Result) testObserver.getEvents().get(0).get(1);
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(ScrollResult.class);

        scrollResult = (ScrollResult) result;
        assertThat(scrollResult.getPageNumber()).isEqualTo(pageNumber);
        assertThat(scrollResult.getError()).isNull();
        assertThat(scrollResult.getResult()).isEmpty();
        assertThat(scrollResult.getType()).isEqualTo(Result.ResultType.SUCCESS);
    }



    /**
     * Test class used to test Rx {@link io.reactivex.internal.operators.observable.ObservableRetryPredicate}.
     */
    private class TestFailure {
        private final NowPlayingInfo nowPlayingInfo;
        private final String errorMessage;
        private boolean didFailOnce = false;

        /**
         * Constructor.
         * @param nowPlayingInfo
         * @param errorMessage
         */
        protected TestFailure(NowPlayingInfo nowPlayingInfo, String errorMessage) {
            this.nowPlayingInfo = nowPlayingInfo;
            this.errorMessage = errorMessage;
        }

        /**
         * Use Mockito to trigger this during test.
         * @return - {@link Exception} first time it's called, {@link NowPlayingInfo} the second.
         */
        Observable<NowPlayingInfo> getNowPlayingInfoObservable() {
            return Observable.fromCallable(new Callable<NowPlayingInfo>() {
                @Override
                public NowPlayingInfo call() throws Exception {
                    if (didFailOnce) {
                        return nowPlayingInfo;
                    } else {
                        didFailOnce = true;
                        throw new Exception(errorMessage);
                    }
                }
            });
        }
    }
}