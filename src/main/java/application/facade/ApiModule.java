package application.facade;

import application.router.MethodHandler;
import router.RouteKey;

import java.util.Map;

public interface ApiModule {
    String basePath();
    Map<RouteKey, MethodHandler<?>> routes();
}