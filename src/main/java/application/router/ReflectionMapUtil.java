package application.router;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;

public class ReflectionMapUtil {


  /**
   * Record 타입만 사용 가능
   */
  public static <T> T mapByParameterName(Class<T> clazz, Map<String, ?> sourceMap) {
    validateClassType(clazz);
    try {
      Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
      Object[] args = extractParameters(constructor.getParameters(), sourceMap);
      return (T) constructor.newInstance(args);
    } catch (Exception e) {
      throw new RuntimeException("[매핑 실패] 클래스 이름 :" + clazz.getSimpleName(), e);
    }
  }

  private static <T> void validateClassType(Class<T> clazz) {
    if (!clazz.isRecord()) {
      throw new IllegalArgumentException(
          "레코드만 매핑 가능: " + clazz.getSimpleName()
      );
    }
  }

  private static Object[] extractParameters(Parameter[] parameters, Map<String, ?> source) {
    return Arrays.stream(parameters).map(param -> source.getOrDefault(
        param.getName(), null
    )).toArray();
  }
}
