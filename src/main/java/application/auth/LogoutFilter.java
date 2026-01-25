package application.auth;

import annotation.Singleton;
import filter.Filter;
import filter.FilterChain;
import http.HttpHeader;
import http.request.HttpMethod;
import http.request.HttpRequest;
import http.request.Path;
import http.response.HttpResponse;
import http.response.HttpStatus;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class LogoutFilter implements Filter {

  private final AuthSession authSession;

  private static final Path HOME_PAGE = new Path("/");
  private static final Path LOGOUT_PATH = new Path("/logout");

  public LogoutFilter(AuthSession authSession) {
    this.authSession = authSession;
  }

  @Override
  public HttpResponse doFilter(HttpRequest request, FilterChain chain) {
    if (!isLogoutRequest(request)) {
      return chain.doChain(request);
    }

    Map<String, String> headers = new HashMap<>();
    return handleLogout(headers, extractSessionId(request));
  }

  private boolean isLogoutRequest(HttpRequest request) {
    return request.method() == HttpMethod.POST && request.path().equals(LOGOUT_PATH);
  }

  private String extractSessionId(HttpRequest request) {
    return request.cookieValue("sid");
  }

  private HttpResponse handleLogout(Map<String, String> headers, String sessionId) {
    if (sessionId != null) {
      authSession.removeSession(sessionId);
    }

    headers.put(HttpHeader.SET_COOKIE.value(),
        Cookie.defaultCookie("sid", "").toString() + "; Max-Age=0");
    headers.put(HttpHeader.LOCATION.value(), HOME_PAGE.value());
    headers.put(HttpHeader.CONTENT_LENGTH.value(), "0");

    return new HttpResponse(HttpStatus.FOUND, headers, new byte[0]);
  }

}
