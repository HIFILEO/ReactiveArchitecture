package example.com.mvpexample.dagger;

import javax.inject.Singleton;

import dagger.Component;
import example.com.mvpexample.application.MvpExampleApplication;

/**
 * Application-level Dagger2 {@link Component}.
 */
@Singleton
@Component(
        modules = {
                ApplicationModule.class,
        })
public interface ApplicationComponent {
    void inject(MvpExampleApplication application);
}
