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

package com.example.reactivearchitecture.nowplaying.service;

import com.example.reactivearchitecture.rx.RxJavaTest;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;

import com.example.reactivearchitecture.categories.ContractTest;

import io.reactivex.observers.TestObserver;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Run {@link ServiceApi} Tests.
 */
@Category(ContractTest.class)
public class ServiceApiTest extends RxJavaTest {
    private static final String API_TOKEN = "6efc30f1fdcbe7425ab08503f07e2762";
    private ServiceApi serviceApi;

    @Before
    public void setUp() {
        super.setUp();
        initMocks(this);

        Retrofit rest = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        serviceApi = rest.create(ServiceApi.class);
    }

    @Test
    public void nowPlaying() throws Exception {
        //
        //Arrange
        //
        Map<String, Integer> mapToSend = new HashMap<>();
        mapToSend.put("page", 1);

        //
        //Act
        //
        TestObserver<ServiceResponse> testObserver
                = serviceApi.nowPlaying(API_TOKEN, mapToSend).test();
        testScheduler.triggerActions();

        //
        //Assert
        //
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);

        ServiceResponse serviceResponse = (ServiceResponse) testObserver.getEvents().get(0).get(0);
        assertThat(serviceResponse.getPage()).isEqualTo(1);
        assertThat(serviceResponse.getResults()).isNotNull();
        assertThat(serviceResponse.getDates()).isNotNull();
        assertThat(serviceResponse.getTotal_pages()).isGreaterThan(0);
        assertThat(serviceResponse.getTotal_results()).isGreaterThan(0);
     }
}
