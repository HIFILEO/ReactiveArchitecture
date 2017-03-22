package example.com.mvpexample.service;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;

import example.com.mvpexample.categories.ContractTest;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

@Category(ContractTest.class)
public class ServiceApiTest {
    private static final String API_TOKEN = "6efc30f1fdcbe7425ab08503f07e2762";
    ServiceApi serviceApi;


    @Before
    public void setUp() {
        initMocks(this);

        Retrofit rest = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .build();

        serviceApi = rest.create(ServiceApi.class);
    }

    @Test
    public void nowPlaying() throws Exception {
        //
        //Arrange
        //
        Call<ServiceResponse> serviceResponseCall = serviceApi.nowPlaying(API_TOKEN, new HashMap<String, String>());

        //
        //Act
        //
        Response<ServiceResponse> response = serviceResponseCall.execute();

        //
        //Assert
        //
        assertThat(response.isSuccessful()).isEqualTo(true);

        ServiceResponse serviceResponse = response.body();
        assertThat(serviceResponse.getPage()).isEqualTo(1);
        assertThat(serviceResponse.getResults()).isNotNull();
        assertThat(serviceResponse.getDates()).isNotNull();
        assertThat(serviceResponse.getTotal_pages()).isGreaterThan(0);
        assertThat(serviceResponse.getTotal_results()).isGreaterThan(0);
     }

}