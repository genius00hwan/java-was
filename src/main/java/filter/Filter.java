package filter;


import http.request.HttpRequest;
import http.response.HttpResponse;

public interface Filter {
    HttpResponse doFilter(HttpRequest request, FilterChain chain);
}
