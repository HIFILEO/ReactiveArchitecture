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

package com.example.reactivearchitecture.nowplaying.model;

import android.support.annotation.NonNull;

import com.example.reactivearchitecture.nowplaying.model.action.FilterAction;
import com.example.reactivearchitecture.nowplaying.model.result.FilterResult;
import com.example.reactivearchitecture.nowplaying.model.result.RestoreResult;
import com.example.reactivearchitecture.nowplaying.model.result.ScrollResult;

import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

/**
 * Holds the {@link io.reactivex.ObservableTransformer} for performing filtering with {@link FilterManager}.
 */
public class FilterTransformer {

    @NonNull
    private final FilterManager filterManager;

    @NonNull
    private final ObservableTransformer<ScrollResult, ScrollResult> transformFilterScrollResult;

    @NonNull
    private final ObservableTransformer<RestoreResult, RestoreResult> transformFilterRestoreResult;

    @NonNull
    private final ObservableTransformer<FilterAction, FilterResult> transformFilterActionToFilterResult;

    /**
     * Constructor.
     * @param filterManagerIn - {@link FilterManager} to use for transformers.
     */
    public FilterTransformer(@NonNull FilterManager filterManagerIn) {
        this.filterManager = filterManagerIn;

        transformFilterScrollResult = upstream -> upstream.map(new Function<ScrollResult, ScrollResult>() {
            @Override
            public ScrollResult apply(@io.reactivex.annotations.NonNull ScrollResult scrollResult) throws Exception {
                return new ScrollResult(
                        scrollResult.getType(),
                        scrollResult.isSuccessful(),
                        scrollResult.isLoading(),
                        scrollResult.getPageNumber(),
                        scrollResult.getResult() == null ? null : filterManager.filterList(scrollResult.getResult()),
                        scrollResult.getError()
                );
            }
        });

        transformFilterRestoreResult = upstream -> upstream.map(restoreResult -> new RestoreResult(
                restoreResult.getType(),
                restoreResult.isSuccessful(),
                restoreResult.isLoading(),
                restoreResult.getPageNumber(),
                restoreResult.getResult() == null ? null : filterManager.filterList(restoreResult.getResult()),
                restoreResult.getError()
        ));

        transformFilterActionToFilterResult = upstream -> upstream.map(new Function<FilterAction, FilterResult>() {
            @Override
            public FilterResult apply(@io.reactivex.annotations.NonNull FilterAction filterAction) throws Exception {
                filterManager.setFilterOn(filterAction.isFilterOn());

                return FilterResult.success(
                        filterManager.isFilterOn(),
                        filterManager.getFullList()
                );
            }
        });
    }

    @NonNull
    public ObservableTransformer<ScrollResult, ScrollResult> getTransformFilterScrollResult() {
        return transformFilterScrollResult;
    }

    @NonNull
    public ObservableTransformer<RestoreResult, RestoreResult> getTransformFilterRestoreResult() {
        return transformFilterRestoreResult;
    }

    @NonNull
    public ObservableTransformer<FilterAction, FilterResult> getTransformFilterActionToFilterResult() {
        return transformFilterActionToFilterResult;
    }
}
