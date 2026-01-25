package application.router.mapper;

import application.router.ReflectionMapUtil;
import http.request.HttpRequest;
import java.util.Map;

public class QueryParameterMapper<T> implements ArgumentMapper<T> {

  private final Class<T> clazz;

  public QueryParameterMapper(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T map(HttpRequest request) {
    validateQueryParams(request.queryParams());
    return ReflectionMapUtil.mapByParameterName(clazz, request.queryParams());
  }

  private void validateQueryParams(Map<String, String> queryParams) {
    if (queryParams == null || queryParams.isEmpty()) {
      throw new IllegalArgumentException("Query Parameter가 존재하지 않습니다.");
    }
  }
}
