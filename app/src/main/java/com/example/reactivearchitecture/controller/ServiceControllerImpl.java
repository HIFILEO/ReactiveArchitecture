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

package com.example.reactivearchitecture.controller;

import android.annotation.SuppressLint;
import android.support.annotation.VisibleForTesting;

import com.example.reactivearchitecture.model.MovieInfo;
import com.example.reactivearchitecture.model.MovieInfoImpl;
import com.example.reactivearchitecture.model.NowPlayingInfo;
import com.example.reactivearchitecture.model.NowPlayingInfoImpl;
import com.example.reactivearchitecture.service.ServiceApi;
import com.example.reactivearchitecture.service.ServiceResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Implementation of {@link ServiceController}.
 */
public class ServiceControllerImpl implements ServiceController {
    private final ServiceApi serviceApi;
    private final String apiKey;
    private final String imageUrlPath;

    /**
     * Constructor.
     * @param serviceApi - Retrofit service.
     * @param apiKey - access key.
     * @param imageUrlPath - url base path for showing images.
     */
    public ServiceControllerImpl(@NonNull ServiceApi serviceApi, @NonNull String apiKey,
                                 @NonNull String imageUrlPath) {
        this.serviceApi = serviceApi;
        this.apiKey = apiKey;
        this.imageUrlPath = imageUrlPath;
    }

    @Override
    public Observable<NowPlayingInfo> getNowPlaying(int pageNumber) {
        Timber.i("Thread name: %s. Get NowPlaying for Page #%s.", Thread.currentThread().getName(), pageNumber);

        Map<String, Integer> mapToSend = new HashMap<>();
        mapToSend.put("page", pageNumber);

        /*
        Notes - Load data from web on scheduler thread. Translate the web response to our
        internal business response on computation thread. Return observable.
         */
        return serviceApi.nowPlaying(apiKey, mapToSend)
                //subscribe up - call api using io scheduler.
                .subscribeOn(Schedulers.io())
                //observe down - translate on computation scheduler.
                .observeOn(Schedulers.computation())
                .flatMap(new TranslateNowPlayingSubscriptionFunc(imageUrlPath))
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                        Timber.e("Failed to get data from service. %s", throwable.toString());
                        throw new Exception("Service failed to get data from API.");
                    }
                });
    }

    /**
     * Class to translate external {@link ServiceResponse} to internal data for {@link NowPlayingInfo}.
     */
    @VisibleForTesting
    static class TranslateNowPlayingSubscriptionFunc
            implements Function<ServiceResponse, ObservableSource<NowPlayingInfo>> {
        @SuppressLint("SimpleDateFormat")
        private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        private final String imageUrlPath;

        /**
         * Constructor.
         * @param imageUrlPath - base path to downloading images.
         */
        TranslateNowPlayingSubscriptionFunc(String imageUrlPath) {
            this.imageUrlPath = imageUrlPath;
        }

        @Override
        public Observable<NowPlayingInfo> apply(@NonNull ServiceResponse serviceResponse) throws Exception {
            Timber.i("Thread name: %s for class %s",
                    Thread.currentThread().getName(),
                    getClass().getSimpleName());
            List<MovieInfo> movieInfoList = new ArrayList<>();

            for (int i = 0; i < serviceResponse.getResults().length; i++) {
                MovieInfo movieInfo = new MovieInfoImpl(
                        imageUrlPath + serviceResponse.getResults()[i].getPoster_path(),
                        serviceResponse.getResults()[i].getTitle(),
                        dateFormat.parse(serviceResponse.getResults()[i].getRelease_date()),
                        serviceResponse.getResults()[i].getVote_average());

                movieInfoList.add(movieInfo);
            }

            return Observable.just((NowPlayingInfo) new NowPlayingInfoImpl(movieInfoList,
                    serviceResponse.getPage(),
                    serviceResponse.getTotal_pages()));
        }
    }
}
