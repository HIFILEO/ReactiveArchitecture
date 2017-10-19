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

package com.example.reactivearchitecture.interactor;

import com.example.reactivearchitecture.gateway.ServiceGateway;
import com.example.reactivearchitecture.model.MovieInfo;
import com.example.reactivearchitecture.model.MovieInfoImpl;
import com.example.reactivearchitecture.model.NowPlayingInfo;
import com.example.reactivearchitecture.model.NowPlayingInfoImpl;
import com.example.reactivearchitecture.model.action.Action;
import com.example.reactivearchitecture.model.action.ScrollAction;
import com.example.reactivearchitecture.model.result.Result;
import com.example.reactivearchitecture.model.result.ScrollResult;
import com.example.reactivearchitecture.rx.RxJavaTest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Test the business logic from the interactor.
 */
public class NowPlayingInteractorTest extends RxJavaTest {

    @Mock
    ServiceGateway mockServiceGateway;

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
    }

    @Test
    public void testScrollAction_pass() {
        //
        //Arrange
        //
        TestObserver<Result> testObserver;
        NowPlayingInteractor nowPlayingInteractor = new NowPlayingInteractor(mockServiceGateway);

        List<MovieInfo> movieInfoList = new ArrayList<MovieInfo>();
        for (int i = 0; i < 5; i++) {
            movieInfoList.add(movieInfo);
        }
        NowPlayingInfo nowPlayingInfo = new NowPlayingInfoImpl(movieInfoList, pageNumber,
                totalPageNumber);

        when(mockServiceGateway.getNowPlaying(anyInt())).thenReturn(
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
        NowPlayingInteractor nowPlayingInteractor = new NowPlayingInteractor(mockServiceGateway);

        String msg = "Error Message";
        Throwable throwable = new Throwable(msg);

        when(mockServiceGateway.getNowPlaying(anyInt())).thenReturn(
                Observable.<NowPlayingInfo>error(throwable));

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

        //FAILURE
        Result failureResult = (Result) testObserver.getEvents().get(0).get(1);
        assertThat(failureResult).isNotNull();
        assertThat(failureResult).isInstanceOf(ScrollResult.class);

        ScrollResult scrollResultFailure = (ScrollResult) failureResult;
        assertThat(scrollResultFailure.getPageNumber()).isEqualTo(pageNumber);
        assertThat(scrollResultFailure.getError()).isNotNull();
        assertThat(scrollResultFailure.getError().getMessage()).isEqualTo(msg);
        assertThat(scrollResultFailure.getResult()).isNull();
        assertThat(scrollResultFailure.getType()).isEqualTo(Result.ResultType.FAILURE);
    }
}