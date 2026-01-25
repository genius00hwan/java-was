package application.router.mapper;

import http.request.HttpRequest;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Optional;

public class VoidMapper<T> implements ArgumentMapper<T> {

  private final Class<T> clazz;

  public VoidMapper(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T map(HttpRequest request) {
    try {
      if (!clazz.isRecord()) {
        return null;
      }
      Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
      Object[] args = Arrays.stream(constructor.getParameterTypes())
          .map(type -> type.equals(Optional.class) ? Optional.empty() : null)
          .toArray();
      return (T) constructor.newInstance(args);
    } catch (Exception e) {
      throw new RuntimeException("[VoidMapper] record 생성 실패", e);
    }
  }
}
