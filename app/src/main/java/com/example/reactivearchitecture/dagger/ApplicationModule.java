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

package com.example.reactivearchitecture.dagger;

import android.app.Application;

import com.example.reactivearchitecture.R;
import com.example.reactivearchitecture.controller.ServiceController;
import com.example.reactivearchitecture.controller.ServiceControllerImpl;
import com.example.reactivearchitecture.service.ServiceApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Dagger2 {@link Module} providing application-level dependency bindings.
 */
@Module(includes = ViewModelModule.class)
public class ApplicationModule {

    @Singleton
    @Provides
    public Gson providesGson(GsonBuilder builder) {
        return builder.create();
    }

    @Provides
    public GsonBuilder providesGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder;
    }

    @Singleton
    @Provides
    public OkHttpClient.Builder providesOkHttpBuilder() {
        return new OkHttpClient.Builder();
    }

    @Singleton
    @Provides
    public OkHttpClient providesOkHttpClient(OkHttpClient.Builder builder, HttpLoggingInterceptor.Level level) {
        //Log HTTP request and response data in debug mode
        final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(level);
        builder.addInterceptor(loggingInterceptor);

        return builder.build();
    }

    @Singleton
    @Provides
    public Retrofit.Builder providesRetrofitBuilder(OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson));
    }

    @Singleton
    @Provides
    public HttpLoggingInterceptor.Level providesHttpLoggingInterceptorLevel() {
        return HttpLoggingInterceptor.Level.NONE;
    }

    @Provides
    @Singleton
    public ServiceApi providesInfoServiceApi(Retrofit.Builder retrofit) {
        return retrofit.baseUrl("https://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ServiceApi.class);
    }

    @Provides
    @Singleton
    public ServiceController providesGatewayInfo(ServiceApi serviceApi, Application application) {
        return new ServiceControllerImpl(serviceApi,
                application.getString(R.string.api_key),
                application.getString(R.string.image_url_path));
    }

}
