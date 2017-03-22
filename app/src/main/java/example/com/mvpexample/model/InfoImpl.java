package example.com.mvpexample.model;

import java.util.Date;

public class InfoImpl implements Info {
    private String pictureUrl;
    private String title;
    private Date releaseDate;
    private double rating;

    public InfoImpl(String pictureUrl, String title, Date releaseDate, double rating) {
        this.pictureUrl = pictureUrl;
        this.title = title;
        this.releaseDate = releaseDate;
        this.rating = rating;
    }

    @Override
    public String getPictureUrl() {
        return pictureUrl;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Date getReleaseDate() {
        return releaseDate;
    }

    @Override
    public double getRating() {
        return rating;
    }
}
