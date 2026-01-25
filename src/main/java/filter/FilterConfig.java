package filter;


import annotation.Singleton;
import application.auth.AuthFilter;
import application.auth.LoginFilter;

import application.auth.LogoutFilter;
import java.util.List;


@Singleton
public class FilterConfig {

  private final List<Filter> filterList;

  // todo: 필터 자동 등록 만들기.
  public FilterConfig(
      LoginFilter loginFilter,
      LogoutFilter logoutFilter,
      AuthFilter authFilter) {
    this.filterList = List.of(
        authFilter,
        logoutFilter,
        loginFilter
    );
  }

  public List<Filter> getFilters() {
    return filterList;
  }
}
