package http.request;

import application.auth.Cookie;
import http.HttpMessage;
import http.body.HttpBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record HttpRequest(
        HttpMethod method,
        Path path,
        String version,
        Map<String, String> headers,
        List<Cookie> cookies,
        Map<String, String> queryParams,
        HttpBody httpBody
) implements HttpMessage {
    public String cookieValue(String name) {
        return cookies.stream()
            .filter(c -> c.name().equals(name))
            .map(Cookie::value)
            .findFirst()
            .orElse(null);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n").append(method.name()).append(" ").append(path).append(" ").append(version).append("\r\n");

        if (!queryParams.isEmpty()) {
            sb.append("?").append(queryParams.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue()+"\r\n")
                    .collect(Collectors.joining("&")));
        }


        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        sb.append("\r\n");

        if (httpBody != null) {
            sb.append(httpBody.toString()).append("\r\n");
        }

        return sb.toString();
    }
}
