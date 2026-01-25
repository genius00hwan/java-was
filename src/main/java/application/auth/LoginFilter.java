package application.auth;

import annotation.Singleton;
import application.model.User;
import filter.Filter;
import filter.FilterChain;

import http.HttpHeader;
import http.body.FormBody;
import http.request.HttpMethod;
import http.request.HttpRequest;
import http.request.Path;
import http.response.HttpResponse;
import http.response.HttpStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * todo:
 * - 현재 비즈니스 로직이 필터에 직접 포함 -> 서비스 계층으로 분리
 * - FormBody 의존성 제거 및 DTO 변환 도입 고려
 */
@Singleton
public class LoginFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoginFilter.class);
    private final UserDetailLoader userDetailLoader;
    private final AuthSession authSession;

    private static final Path HOME_PATH = new Path("/");
    private static final Path LOGIN_PATH = new Path("/login");
    private static final String FAILURE_PARAM = "?failure=true";
    private static final String SUCCESS_PARAM = "?success=true";

    public LoginFilter(UserDetailLoader userDetailLoader, AuthSession authSession) {
        this.userDetailLoader = userDetailLoader;
        this.authSession = authSession;
    }

    @Override
    public HttpResponse doFilter(HttpRequest request, FilterChain chain) {
        if (!isLoginRequest(request)) {
            return chain.doChain(request);
        }

        FormBody formBody = (FormBody) request.httpBody();
        String userId = formBody.formData().get("userId");
        String password = formBody.formData().get("password");

        Map<String, String> headers = new HashMap<>();

        if (!isValidUser(userId, password)) {
            return handleInvalidLogin(headers);
        }


        return handleLogin(
                headers,
                authSession.addSession(userId)
        );
    }

    private boolean isLoginRequest(HttpRequest request) {
        return request.method() == HttpMethod.POST
                && request.path().equals(LOGIN_PATH);
    }

    private boolean isValidUser(String userId, String password) {
        User user = userDetailLoader.load(userId);
        return user != null && user.isValidPassword(password);
    }

    private HttpResponse handleInvalidLogin(Map<String, String> headers) {
        headers.put(HttpHeader.LOCATION.value(), LOGIN_PATH + FAILURE_PARAM);
        return new HttpResponse(HttpStatus.FOUND, headers, new byte[0]);
    }

    private HttpResponse handleLogin(Map<String, String> headers, String sessionId) {
        headers.put(HttpHeader.LOCATION.value(), HOME_PATH.value() + SUCCESS_PARAM);
        headers.put(HttpHeader.SET_COOKIE.value(), Cookie.defaultCookie("sid", sessionId).toString());
        headers.put(HttpHeader.CONTENT_LENGTH.value(), "0");

        return new HttpResponse(
                HttpStatus.FOUND,
                headers,
                new byte[0]
        );
    }
}