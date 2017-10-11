package com.example.reactivearchitecture.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.example.reactivearchitecture.model.LoadMoreCommand.LoadMoreType.RESTORE;
import static com.example.reactivearchitecture.model.LoadMoreCommand.LoadMoreType.SCROLL;

/**
 * Commands for the adapters associated with the recycler view.
 */
public class LoadMoreCommand {
    private final @LoadMoreType int loadMoreType;

    public LoadMoreCommand(int loadMoreType) {
        this.loadMoreType = loadMoreType;
    }

    public int getLoadMoreType() {
        return loadMoreType;
    }

    /**
     * Load command types.
     */
    @IntDef({RESTORE, SCROLL})
    @Retention(RetentionPolicy.SOURCE)
    @SuppressWarnings("checkstyle:abbreviationaswordinname")
    public @interface LoadMoreType {
        int RESTORE = 1;
        int SCROLL = 2;
    }
}
