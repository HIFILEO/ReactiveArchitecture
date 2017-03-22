package example.com.mvpexample.service;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Retrofit Interface.
 */
public interface ServiceApi {

    @GET("/3/movie/now_playing")
    Call<ServiceResponse> nowPlaying(
            @Query("api_key") String api_key,
            @QueryMap Map<String, String> query);
}
