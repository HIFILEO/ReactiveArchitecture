package example.com.mvpexample.model;

import java.util.Date;

/**
 * Internal business logic representation of information
 */
public interface Info {
    String getPictureUrl();
    String getTitle();
    Date getReleaseDate();
    double getRating();
}
