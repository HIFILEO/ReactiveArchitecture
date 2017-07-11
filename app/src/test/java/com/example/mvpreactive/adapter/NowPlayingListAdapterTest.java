package com.example.mvpreactive.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.mvpreactive.categories.UnitTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
public class NowPlayingListAdapterTest {

    @Test
    public void testLoadMoreScrollListener() {
        //
        //Arrange
        //
        LinearLayoutManager mockLinearLayoutManager = Mockito.mock(LinearLayoutManager.class);
        NowPlayingListAdapter.OnLoadMoreListener mockOnLoadMoreListener =
                Mockito.mock(NowPlayingListAdapter.OnLoadMoreListener.class);
        NowPlayingListAdapter mockNowPlayingListAdapter = Mockito.mock(NowPlayingListAdapter.class);
        RecyclerView mockRecyclerView = Mockito.mock(RecyclerView.class);

        NowPlayingListAdapter.LoadMoreScrollListener loadMoreScrollListener =
                new NowPlayingListAdapter.LoadMoreScrollListener(
                        mockLinearLayoutManager,
                        mockOnLoadMoreListener,
                        mockNowPlayingListAdapter
                );

        when(mockLinearLayoutManager.getItemCount()).thenReturn(50);
        when(mockLinearLayoutManager.findLastVisibleItemPosition()).thenReturn(50);

        //
        //Act
        //
        loadMoreScrollListener.onScrolled(mockRecyclerView, 0, 100);

        //
        //Assert
        //
        verify(mockNowPlayingListAdapter).add(null);
        verify(mockOnLoadMoreListener).onLoadMore();

        assertThat(loadMoreScrollListener.isLoading()).isTrue();
    }
}