package example.com.mvpexample.gateway;


import example.com.mvpexample.model.Info;
import example.com.mvpexample.service.ServiceApi;

public class ServiceGatewayImpl implements ServiceGateway{
    private ServiceApi serviceApi;

    public ServiceGatewayImpl(ServiceApi serviceApi) {
        this.serviceApi = serviceApi;
    }

    @Override
    public Info getNotPlaying(int pageNumber) {

    }


}
