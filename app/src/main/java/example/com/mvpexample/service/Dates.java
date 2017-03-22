package example.com.mvpexample.service;

/**
 * Service response based on:
 * https://developers.themoviedb.org/3/movies/get-now-playing
 */
public class Dates {
    private String maximum;
    private String minimum;

    public String getMaximum() {
        return maximum;
    }

    public String getMinimum() {
        return minimum;
    }
}
