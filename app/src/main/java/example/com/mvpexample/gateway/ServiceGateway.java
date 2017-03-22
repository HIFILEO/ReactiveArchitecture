package example.com.mvpexample.gateway;

import example.com.mvpexample.model.Info;

public interface ServiceGateway {
    Info getNotPlaying(int pageNumber);
}
