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

package com.example.mvpexample.interactor;

import android.os.Handler;
import android.support.annotation.VisibleForTesting;

import com.example.mvpexample.gateway.ServiceGateway;
import com.example.mvpexample.model.NowPlayingInfo;

import timber.log.Timber;

/**
 * Info Interactor Implementation.
 */
public class NowPlayingInteractorImpl implements NowPlayingInteractor {
    private NowPlayingResponseModel nowPlayingResponseModel;
    private ServiceGateway serviceGateway;
    private Handler mainUiHandler;
    private int pageNumber = 0;

    public NowPlayingInteractorImpl(ServiceGateway serviceGateway, Handler mainUiHandler) {
        this.serviceGateway = serviceGateway;
        this.mainUiHandler = mainUiHandler;
    }

    public void setNowPlayingResponseModel(NowPlayingResponseModel nowPlayingResponseModel) {
        this.nowPlayingResponseModel = nowPlayingResponseModel;
    }

    @Override
    public void loadMoreInfo() {
        (new Thread(new LoadDataThread(
                ++pageNumber,
                mainUiHandler,
                serviceGateway,
                nowPlayingResponseModel))).start();
    }

    /**
     * Thread to load data.
     */
    @VisibleForTesting
    static class LoadDataThread implements Runnable {
        private static final int SLEEP_MSEC = 3000;
        private final int pageNumberToLoad;
        private final Handler mainUiHandler;
        private final ServiceGateway serviceGateway;
        private final NowPlayingResponseModel nowPlayingResponseModel;
        private int sleepMsec = SLEEP_MSEC;
        private NowPlayingInfo nowPlayingInfo = null;

        /**
         * Constructor.
         * @param pageNumberToLoad - page number to load
         * @param mainUiHandler - handler for responses
         * @param serviceGateway - gateway to call
         * @param nowPlayingResponseModel - response model to call
         */
        LoadDataThread(int pageNumberToLoad, Handler mainUiHandler, ServiceGateway serviceGateway,
                       NowPlayingResponseModel nowPlayingResponseModel) {
            this.pageNumberToLoad = pageNumberToLoad;
            this.mainUiHandler = mainUiHandler;
            this.serviceGateway = serviceGateway;
            this.nowPlayingResponseModel = nowPlayingResponseModel;
        }

        @Override
        public void run() {
            //
            //Delay for 3 seconds to show spinner on screen.
            //
            try {
                Thread.sleep(sleepMsec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //
            //Call Service
            //
            try {
                nowPlayingInfo = serviceGateway.getNowPlaying(pageNumberToLoad);
            } catch (Exception e) {
                Timber.e("Failed to fetch data: ", e);
            }

            //
            //Process Results
            //
            if (nowPlayingInfo == null || nowPlayingInfo.getPageNumber() != pageNumberToLoad) {
                mainUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        nowPlayingResponseModel.errorLoadingInfoData();
                    }
                });
            } else {
                mainUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        nowPlayingResponseModel.infoLoaded(nowPlayingInfo.getMovies());
                    }
                });
            }
        }

        @VisibleForTesting(otherwise = VisibleForTesting.NONE)
        public void setSleepMsec(int sleepMsec) {
            this.sleepMsec = sleepMsec;
        }
    }
}
