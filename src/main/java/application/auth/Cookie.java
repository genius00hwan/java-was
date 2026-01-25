package application.auth;

public record Cookie(String name, String value, String path, Integer maxAge // 초 단위, null이면 세션 쿠키
) {


  public static Cookie defaultCookie(String name, String value) {
    return new Cookie(name, value, "/", -1);
  }

  /**
   * 공부한 거 RFC 와 Spring ResponseCookie 정책이 서로 달라 모호함.
   * [참조](https://github.com/spring-projects/spring-framework/pull/35216)
   */
  private Cookie(String name, String value) {
    this(name, value, "/", -1);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(name).append("=").append(value);

    if (path != null) {
      sb.append("; Path=").append(path);
    }

    if (maxAge != null && maxAge >= 0) {
      sb.append("; Max-Age=").append(maxAge);
    }

    return sb.toString();
  }
}
