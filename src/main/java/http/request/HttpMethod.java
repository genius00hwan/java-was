package http.request;

import java.util.Arrays;

public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH;

    public static HttpMethod from(String value) {
        return Arrays.stream(values())
                .filter(m -> m.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported HTTP method: " + value));
    }
}
