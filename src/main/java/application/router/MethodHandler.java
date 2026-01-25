package application.router;

import application.auth.AuthInjector;
import application.router.mapper.ArgumentMapper;
import http.request.HttpRequest;
import http.response.HttpResponse;

import java.util.function.Function;

public class MethodHandler<T> {

  private final ArgumentMapper<T> mapper;
  private final Function<T, HttpResponse> handler;
  private final AuthInjector authInjector;

  public MethodHandler(
      ArgumentMapper<T> mapper,
      Function<T, HttpResponse> handler,
      AuthInjector authInjector
  ) {
    this.mapper = mapper;
    this.handler = handler;
    this.authInjector = authInjector;
  }

  public HttpResponse handle(HttpRequest request) {
    T dto = authInjector.injectIfNeed(
        mapper.map(request),
        request);
    return handler.apply(dto);
  }
}
