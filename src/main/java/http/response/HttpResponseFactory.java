package http.response;

import http.ContentType;
import http.HttpHeader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseFactory {

  private static final String BAD_REQUEST_TEMPLATE;
  private static final String INTERNAL_ERROR_TEMPLATE;

  static {
    BAD_REQUEST_TEMPLATE = loadStaticTemplate("static/BadRequest.html");
    INTERNAL_ERROR_TEMPLATE = loadStaticTemplate("static/InternalError.html");
  }

  private static String loadStaticTemplate(String resourcePath) {
    try (InputStream is = HttpResponseFactory.class.getClassLoader()
        .getResourceAsStream(resourcePath)) {
      return (is != null) ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : "";
    } catch (IOException e) {
      return "";
    }
  }

  public static HttpResponse redirect(String location) {
    byte[] emptyBody = new byte[0];
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeader.LOCATION.value(), location);
    return create(HttpStatus.FOUND, headers, emptyBody);
  }

  public static HttpResponse toLoginPage(String parameter) {
    return HttpResponseFactory.redirect("/login" + parameter);
  }

  public static HttpResponse notFound() {
    String body = BAD_REQUEST_TEMPLATE.replace("${message}", "not found");
    return responseForStaticFile(HttpStatus.NOT_FOUND, ContentType.HTML,
        body.getBytes(StandardCharsets.UTF_8));
  }

  public static HttpResponse badRequest(String message) {
    String body = BAD_REQUEST_TEMPLATE.replace("${message}", message);
    return responseForStaticFile(HttpStatus.BAD_REQUEST, ContentType.HTML,
        body.getBytes(StandardCharsets.UTF_8));
  }

  public static HttpResponse internalServerError(String message) {
    String body = INTERNAL_ERROR_TEMPLATE.replace("${message}", message);
    return responseForStaticFile(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.HTML,
        body.getBytes(StandardCharsets.UTF_8));
  }

  public static HttpResponse responseForStaticFile(HttpStatus status, ContentType contentType,
      byte[] body) {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeader.CONTENT_TYPE.value(), contentType.value());
    headers.put(HttpHeader.CONTENT_LENGTH.value(), String.valueOf(body.length));
    return create(status, headers, body);
  }

  private static HttpResponse create(HttpStatus httpStatus, Map<String, String> headers,
      byte[] body) {
    return new HttpResponse(httpStatus, headers, body);
  }
}
