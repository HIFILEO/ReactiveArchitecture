package example.com.mvpexample.application;

import android.util.Log;

import timber.log.Timber;

/**
 * A Timber Tree which logs nothing....
 */
public class CrashReportingTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable throwable) {
        switch (priority) {
            case Log.INFO:
                break;
            case Log.WARN:
                break;
            case Log.ERROR:
                break;
            default:
                break;
        }
    }
}
