package com.example.reactivearchitecture.model.result;

import com.example.reactivearchitecture.model.MovieInfo;

import java.util.List;

/**
 * Results from {@link com.example.reactivearchitecture.model.action.FilterAction} requests.
 */
public class FilterResult extends Result {
    private @ResultType int resultType;
    private boolean filterOn;
    private boolean filterInProgress;
    private List<MovieInfo> filteredList;

    /**
     * In progress creator.
     * @param filterOn -
     * @return new {@link FilterResult}
     */
    public static FilterResult inProgress(boolean filterOn) {
        return new FilterResult(
                ResultType.IN_FLIGHT,
                filterOn,
                true,
                null);
    }

    /**
     * Success Creator.
     * @param filterOn -
     * @param listToShow -
     * @return new {@link FilterResult}
     */
    public static FilterResult success(boolean filterOn, List<MovieInfo> listToShow) {
        return new FilterResult(
                ResultType.SUCCESS,
                filterOn,
                true,
                listToShow);
    }

    private FilterResult(int resultType, boolean filterOn, boolean filterInProgress, List<MovieInfo> filteredList) {
        this.resultType = resultType;
        this.filterOn = filterOn;
        this.filterInProgress = filterInProgress;
        this.filteredList = filteredList;
    }

    @Override
    public int getType() {
        return resultType;
    }


    public boolean isFilterOn() {
        return filterOn;
    }

    public List<MovieInfo> getFilteredList() {
        return filteredList;
    }
}
