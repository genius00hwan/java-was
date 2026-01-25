package application.facade;

import application.auth.AuthInjector;
import application.router.MethodHandler;
import application.router.mapper.ArgumentMapper;
import http.request.HttpMethod;
import http.request.Path;
import http.response.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import router.RouteKey;

public abstract class Facade implements ApiModule {

  protected final Map<RouteKey, MethodHandler<?>> routeMap = new HashMap<>();
  protected final AuthInjector authInjector;

  protected Facade(AuthInjector authInjector) {
    this.authInjector = authInjector;
    createRouteMap();
  }

  @Override
  public Map<RouteKey, MethodHandler<?>> routes() {
    return routeMap;
  }

  protected abstract void createRouteMap();

  protected <T> void registerRoute(
      String subPath,
      HttpMethod method,
      ArgumentMapper<T> mapper,
      Function<T, HttpResponse> handler) {
    routeMap.put(
        new RouteKey(new Path(subPath), method),
        new MethodHandler<>(mapper, handler, authInjector)
    );
  }
}
