package router.servlet;

import annotation.Singleton;
import application.exception.client.ClientException;
import application.exception.server.ServerException;

import application.facade.ArticleFacade;
import application.facade.HomeFacade;
import application.facade.UserFacade;

import application.router.MethodHandler;
import http.request.HttpRequest;
import http.response.HttpResponse;
import http.response.HttpResponseFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.AppRouteConfig;
import router.RouteKey;
import staticresource.PathResolver;

@Singleton
public class AppServlet{

  private static final Logger log = LoggerFactory.getLogger(AppServlet.class);
  private final Map<RouteKey, MethodHandler<?>> routeMap;

  // todo: 나중에 Facade 클래스 자동등록 만들기.
  public AppServlet(HomeFacade homeFacade, UserFacade userFacade, ArticleFacade articleFacade) {
    AppRouteConfig config = new AppRouteConfig(
        List.of(
            homeFacade,
            userFacade,
            articleFacade
        )
    );
    this.routeMap = config.buildRouteMap();
  }

  public Optional<HttpResponse> service(HttpRequest request) {
    RouteKey key = new RouteKey(
        PathResolver.resolveForApp(request.path()),
        request.method()
    );

    try {
      MethodHandler<?> handler = routeMap.get(key);
      if (handler == null) {
        return Optional.empty();
      }
      return Optional.of(
          handler.handle(request)
      );
    } catch (ClientException e) {
      return Optional.of(HttpResponseFactory.badRequest(e.getMessage()));
    } catch (ServerException e) {
      return Optional.of(HttpResponseFactory.internalServerError(e.getMessage()));
    }
  }
}
