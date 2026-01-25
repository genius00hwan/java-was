package filter;

import http.response.HttpResponse;

public abstract class FilterException extends RuntimeException {
    public abstract HttpResponse toResponse();
}
