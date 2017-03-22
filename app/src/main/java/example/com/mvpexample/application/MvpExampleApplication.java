package example.com.mvpexample.application;

import android.app.Application;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import example.com.mvpexample.dagger.ApplicationComponent;
import example.com.mvpexample.dagger.ApplicationModule;
import example.com.mvpexample.dagger.DaggerApplicationComponent;
import timber.log.Timber;

/**
 * This is the MVP application class for setting up Dagger 2 and Timber.
 */
public class MvpExampleApplication extends Application {
    private static MvpExampleApplication mvpExampleApplication;
    private ApplicationComponent component;

    public static MvpExampleApplication getInstance() {
        return mvpExampleApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MvpExampleApplication.mvpExampleApplication = this;
        setupComponent();
        setupTimber();
    }

    /**
     * Get the {@link ApplicationComponent}.
     *
     * @return The single {@link ApplicationComponent} object.
     */
    public ApplicationComponent getComponent() {
        return component;
    }

    /**
     * Setup the Timber logging tree.
     */
    void setupTimber() {
        Timber.plant(new CrashReportingTree());
    }

    /**
     * Setup the Dagger2 component graph.
     */
    @VisibleForTesting
    void setupComponent() {
        if (component == null) {

            component = DaggerApplicationComponent.builder()
                    .applicationModule(getApplicationModule())
                    .build();
            component.inject(this);
        } else {
            Log.d(MvpExampleApplication.class.getSimpleName(), "setupComponent() called.  ApplicationComponent already set.");
        }
    }

    /**
     * Get application module.
     * @return - ApplicationModule
     */
    protected ApplicationModule getApplicationModule() {
        return new ApplicationModule(this);
    }

}
