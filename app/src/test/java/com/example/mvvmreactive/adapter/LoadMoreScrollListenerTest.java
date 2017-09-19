package com.example.mvvmreactive.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.mvvmreactive.categories.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class LoadMoreScrollListenerTest {

    @Test
    public void testLoadMoreScrollListener() {
        //
        //Arrange
        //
        LinearLayoutManager mockLinearLayoutManager = Mockito.mock(LinearLayoutManager.class);
        LoadMoreScrollListener.OnLoadMoreListener mockOnLoadMoreListener =
                Mockito.mock(LoadMoreScrollListener.OnLoadMoreListener.class);
        RecyclerView mockRecyclerView = Mockito.mock(RecyclerView.class);

        LoadMoreScrollListener loadMoreScrollListener =
                new LoadMoreScrollListener(mockLinearLayoutManager, mockOnLoadMoreListener);

        when(mockLinearLayoutManager.getItemCount()).thenReturn(50);
        when(mockLinearLayoutManager.findLastVisibleItemPosition()).thenReturn(50);

        //
        //Act
        //
        loadMoreScrollListener.onScrolled(mockRecyclerView, 0, 100);

        //
        //Assert
        //
        verify(mockOnLoadMoreListener).onLoadMore();
    }
}