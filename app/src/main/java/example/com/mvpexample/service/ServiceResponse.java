package example.com.mvpexample.service;

/**
 * Service response based on:
 * https://developers.themoviedb.org/3/movies/get-now-playing
 */
public class ServiceResponse {
    private int page;
    private Results[] results;
    private Dates dates;
    private int total_pages;
    private int total_results;

    public int getPage() {
        return page;
    }

    public Results[] getResults() {
        return results;
    }

    public Dates getDates() {
        return dates;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public int getTotal_results() {
        return total_results;
    }
}
