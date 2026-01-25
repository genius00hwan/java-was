package filter;


import http.request.HttpRequest;
import http.response.HttpResponse;
import router.Dispatcher;

import java.util.List;

public class FilterChain {

  private final FilterConfig filterConfig;
  private final Dispatcher dispatcher;
  private int index;

  public FilterChain(FilterConfig filterConfig, Dispatcher dispatcher) {
    this.filterConfig = filterConfig;
    this.dispatcher = dispatcher;
    index = 0;
  }

  public HttpResponse doChain(HttpRequest request) {
    List<Filter> filters = filterConfig.getFilters();
    if (index < filters.size()) {
      return filters.get(index++).doFilter(request, this);
    }
    return dispatcher.dispatch(request);
  }
}
