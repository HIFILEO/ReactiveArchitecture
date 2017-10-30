package com.example.reactivearchitecture.model;

import com.example.reactivearchitecture.nowplaying.model.FilterManager;
import com.example.reactivearchitecture.nowplaying.model.MovieInfo;
import com.example.reactivearchitecture.nowplaying.model.MovieInfoImpl;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import static org.mockito.MockitoAnnotations.initMocks;

public class FilterManagerTest {

    List<MovieInfo> movieInfoFullList = new ArrayList<>();

    MovieInfo movieInfoHighRating = new MovieInfoImpl(
            "www.url.com",
            "Dan The Man",
            new Date(),
            9d);

    MovieInfo movieInfoLowRating = new MovieInfoImpl(
            "www.url_low.com",
            "Dan IS STILL The Man",
            new Date(),
            5d);

    @Before
    public void setUp() {
        initMocks(this);

        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                movieInfoFullList.add(movieInfoHighRating);
            } else {
                movieInfoFullList.add(movieInfoLowRating);
            }
        }
    }

    @Test
    public void filterList_filterOn() throws Exception {
        //
        //Arrange
        //
        FilterManager filterManager = new FilterManager(true);

        //
        //Act
        //
        List<MovieInfo> listToTest = filterManager.filterList(movieInfoFullList);

        //
        //Assert
        //
        assertThat(filterManager.isFilterOn()).isTrue();
        assertThat(listToTest).hasSize(5);
        assertThat(listToTest.get(0)).isEqualToComparingFieldByField(movieInfoHighRating);
    }

    @Test
    public void filterList_filterOff() throws Exception {
        //
        //Arrange
        //
        FilterManager filterManager = new FilterManager(false);

        //
        //Act
        //
        List<MovieInfo> listToTest = filterManager.filterList(movieInfoFullList);

        //
        //Assert
        //
        assertThat(filterManager.isFilterOn()).isFalse();
        assertThat(listToTest).hasSize(10);
        assertThat(listToTest.get(0)).isEqualToComparingFieldByField(movieInfoHighRating);
        assertThat(listToTest.get(9)).isEqualToComparingFieldByField(movieInfoLowRating);
    }

    @Test
    public void getFullList_filterOff() throws Exception {
        //
        //Arrange
        //
        FilterManager filterManager = new FilterManager(false);

        //
        //Act
        //
        filterManager.filterList(movieInfoFullList);
        List<MovieInfo> listToTest = filterManager.getFullList();

        //
        //Assert
        //
        assertThat(filterManager.isFilterOn()).isFalse();
        assertThat(listToTest).hasSize(10);
        assertThat(listToTest.get(0)).isEqualToComparingFieldByField(movieInfoHighRating);
        assertThat(listToTest.get(9)).isEqualToComparingFieldByField(movieInfoLowRating);
    }

    @Test
    public void getFullList_filterOn() throws Exception {
        //
        //Arrange
        //
        FilterManager filterManager = new FilterManager(true);

        //
        //Act
        //
        filterManager.filterList(movieInfoFullList);
        List<MovieInfo> listToTest = filterManager.getFullList();

        //
        //Assert
        //
        assertThat(filterManager.isFilterOn()).isTrue();
        assertThat(listToTest).hasSize(5);
        assertThat(listToTest.get(0)).isEqualToComparingFieldByField(movieInfoHighRating);
    }

}