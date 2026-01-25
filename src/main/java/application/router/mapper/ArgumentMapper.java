package application.router.mapper;

import http.request.HttpRequest;

public interface ArgumentMapper<T> {
    T map (HttpRequest request);
}
