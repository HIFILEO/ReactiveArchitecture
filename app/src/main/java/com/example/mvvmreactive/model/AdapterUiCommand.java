package com.example.mvvmreactive.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Commands for the adapters associated with the recycler view.
 */
public class AdapterUiCommand {
    private final @CommandType int commandType;
    private final List<MovieViewInfo> movieViewInfoList;

    /**
     * Factory method to create an add null command.
     * @return -
     */
    public static AdapterUiCommand createAddNull() {
        List<MovieViewInfo> movieViewInfoList = new ArrayList<>();
        movieViewInfoList.add(null);

        return new AdapterUiCommand(
                CommandType.ADD,
                movieViewInfoList);
    }

    /**
     * Factory method to create a remove null command.
     * @return -
     */
    public static AdapterUiCommand createRemoveNull() {
        List<MovieViewInfo> movieViewInfoList = new ArrayList<>();
        movieViewInfoList.add(null);

        return new AdapterUiCommand(
                CommandType.REMOVE,
                movieViewInfoList);
    }

    /**
     * Constructor.
     * @param commandType -
     * @param movieViewInfoList -
     */
    public AdapterUiCommand(@CommandType int commandType, List<MovieViewInfo> movieViewInfoList) {
        this.commandType = commandType;
        this.movieViewInfoList = movieViewInfoList;
    }

    public @CommandType int getCommandType() {
        return commandType;
    }

    public List<MovieViewInfo> getMovieViewInfoList() {
        return movieViewInfoList;
    }

    /**
     * Command Types.
     */
    @IntDef({CommandType.ADD, CommandType.REMOVE})
    @Retention(RetentionPolicy.SOURCE)
    @SuppressWarnings("checkstyle:abbreviationaswordinname")
    public @interface CommandType {
        int ADD = 1;
        int REMOVE = 2;
    }
}
