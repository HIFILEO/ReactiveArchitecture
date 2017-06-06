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

import com.example.mvpexample.dagger.ActivityScope;
import com.example.mvpexample.gateway.ServiceGateway;
import com.example.mvpexample.model.NowPlayingInfo;

import java.lang.ref.WeakReference;
import javax.inject.Inject;

import timber.log.Timber;

/**
 * Info Interactor Implementation.
 */
@ActivityScope
public class NowPlayingInteractorImpl implements NowPlayingInteractor {
    @VisibleForTesting
    protected static LoadDataThread loadDataThread;

    private NowPlayingResponseModel nowPlayingResponseModel;
    private ServiceGateway serviceGateway;
    private Handler mainUiHandler;
    private static int pageNumber = 0;

    @Inject
    public NowPlayingInteractorImpl(ServiceGateway serviceGateway, Handler mainUiHandler) {
        this.serviceGateway = serviceGateway;
        this.mainUiHandler = mainUiHandler;
    }

    public void setNowPlayingResponseModel(NowPlayingResponseModel nowPlayingResponseModel) {
        this.nowPlayingResponseModel = nowPlayingResponseModel;
    }

    @Override
    public void registerCallbacks() {
        if (loadDataThread != null) {
            loadDataThread.registerCallback(nowPlayingResponseModel);
        }
    }

    @Override
    public void unregisterCallbacks() {
        if (loadDataThread != null) {
            loadDataThread.unregisterCallback();
        }
    }

    @Override
    public void loadMoreInfo() {
        if (loadDataThread == null) {
            loadDataThread = new LoadDataThread(
                    ++pageNumber,
                    mainUiHandler,
                    serviceGateway,
                    nowPlayingResponseModel);
            (new Thread(loadDataThread)).start();
        } else {
            loadDataThread.registerCallback(nowPlayingResponseModel);
        }
    }

    /**
     * Thread to load data from service.
     */
    static class LoadDataThread implements Runnable {
        private static final int SLEEP_MSEC = 3000;

        @VisibleForTesting
        protected Runnable runnableCache;

        @VisibleForTesting
        protected WeakReference<NowPlayingResponseModel> nowPlayingResponseModelWeakReference;

        private final int pageNumberToLoad;
        private final Handler mainUiHandler;
        private final ServiceGateway serviceGateway;
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
            this.nowPlayingResponseModelWeakReference =
                    new WeakReference<>(nowPlayingResponseModel);
        }

        /**
         * Register callback.
         * @param nowPlayingResponseModel - callback to register
         */
        @VisibleForTesting
        public void registerCallback(NowPlayingResponseModel nowPlayingResponseModel) {
            this.nowPlayingResponseModelWeakReference =
                    new WeakReference<>(nowPlayingResponseModel);

            if (runnableCache != null) {
                mainUiHandler.post(runnableCache);
                NowPlayingInteractorImpl.loadDataThread = null;
                runnableCache = null;
            }
        }

        /**
         * Unregister callback.
         */
        @VisibleForTesting
        public void unregisterCallback() {
            if (nowPlayingResponseModelWeakReference != null) {
                this.nowPlayingResponseModelWeakReference.clear();
            }
            this.nowPlayingResponseModelWeakReference = null;
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
                Timber.e(e, "Failed to fetch data:");
            }

            //
            //Process Results - use cache when needed
            //
            if (nowPlayingInfo == null || nowPlayingInfo.getPageNumber() != pageNumberToLoad) {
                runnableCache = new Runnable() {
                    @Override
                    public void run() {
                        nowPlayingResponseModelWeakReference.get().errorLoadingInfoData();
                    }
                };
            } else {
                runnableCache = new Runnable() {
                    @Override
                    public void run() {
                        nowPlayingResponseModelWeakReference.get().infoLoaded(
                                nowPlayingInfo.getMovies());
                    }
                };
            }

            if (nowPlayingResponseModelWeakReference != null &&
                    nowPlayingResponseModelWeakReference.get() != null) {
                mainUiHandler.post(runnableCache);
                NowPlayingInteractorImpl.loadDataThread = null;
                runnableCache = null;
            }
        }

        @VisibleForTesting(otherwise = VisibleForTesting.NONE)
        public void setSleepMsec(int sleepMsec) {
            this.sleepMsec = sleepMsec;
        }
    }
}
