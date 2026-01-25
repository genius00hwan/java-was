package router.servlet;

import annotation.Singleton;
import http.request.HttpRequest;


import http.response.HttpResponse;
import http.response.HttpResponseFactory;
import http.response.HttpStatus;

import staticresource.StaticFileLoader;
import http.body.StaticResourceBody;

import java.io.IOException;
import java.util.Optional;

@Singleton
public class StaticServlet {

  public Optional<HttpResponse> service(HttpRequest request) throws IOException {
    Optional<StaticResourceBody> staticResource = StaticFileLoader.tryLoad(
        request.path()
    );

    if (staticResource.isEmpty()) {
      return Optional.empty();
    }

    StaticResourceBody resource = staticResource.get();
    HttpResponse response = HttpResponseFactory.responseForStaticFile(
        HttpStatus.OK,
        resource.contentType(),
        resource.rawBody()
    );
    return Optional.of(response);
  }
}


