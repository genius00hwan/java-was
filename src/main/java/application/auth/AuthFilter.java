package application.auth;

import annotation.Singleton;
import filter.Filter;
import filter.FilterChain;
import http.request.HttpMethod;
import http.request.HttpRequest;

import http.request.Path;
import http.response.HttpResponse;
import http.response.HttpResponseFactory;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import staticresource.PathResolver;

@Singleton
public class AuthFilter implements Filter {

  private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
  private final AuthSession authSession;
  private static final Set<String> OPEN_URLS = Set.of(
      "/"
      , "/login/**", "/register/**", "/img/**"
  );

  private static final String FORBIDDEN_PARAM = "?forbidden=true";

  public AuthFilter(AuthSession authSession) {
    this.authSession = authSession;
  }


  @Override
  public HttpResponse doFilter(HttpRequest request, FilterChain chain) {
    if (isOpen(request.path(), request.method())) {
      return chain.doChain(request);
    }

    String sid = request.cookieValue("sid");
    if (sid == null || !authSession.isValid(sid)) {
      return HttpResponseFactory.toLoginPage(FORBIDDEN_PARAM);
    }

    return chain.doChain(request);
  }

  private boolean isOpen(Path rawPath, HttpMethod method) {
    String pathValue = PathResolver.resolveForApp(rawPath).value();
    if (isOneDepth(pathValue) && method == HttpMethod.GET) {
      return true;
    }
    return OPEN_URLS.stream().anyMatch(
        pattern -> matchPattern(pattern, pathValue));
  }

  private boolean matchPattern(String pattern, String path) {
    if (pattern.endsWith("/**")) {
      String prefix = pattern.substring(0, pattern.length() - 3);
      return path.startsWith(prefix);
    }
    return pattern.equals(path);
  }

  private boolean isOneDepth(String path) {
    return (path.chars().filter(ch -> ch == '/').count() == 1)&&(path.contains("."));
  }


}
