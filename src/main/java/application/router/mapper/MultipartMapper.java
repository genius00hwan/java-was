package application.router.mapper;

import application.exception.client.IllegalContentTypeException;
import application.model.holder.Image;
import http.body.HttpBody;
import http.body.MultipartFormBody;
import http.request.HttpRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;

public class MultipartMapper<T> implements ArgumentMapper<T> {

  private final Class<T> clazz;

  public MultipartMapper(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T map(HttpRequest request) {
    HttpBody body = request.httpBody();
    validateBodyType(body);

    MultipartFormBody multipart = (MultipartFormBody) body;

    try {
      validateClassType(clazz);
      Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
      Object[] args = extractParameters(constructor.getParameters(),
          multipart.formFields(), multipart.fileFields(), multipart.fileTypes());
      return (T) constructor.newInstance(args);
    } catch (Exception e) {
      throw new RuntimeException("[매핑 실패] 클래스 이름: " + clazz.getSimpleName(), e);
    }
  }

  private void validateBodyType(HttpBody body) {
    if (!(body instanceof MultipartFormBody)) {
      throw new IllegalContentTypeException("지원하지 않는 타입입니다!!");
    }
  }

  private void validateClassType(Class<T> clazz) {
    if (!clazz.isRecord()) {
      throw new IllegalArgumentException("레코드만 매핑 가능: " + clazz.getSimpleName());
    }
  }

  private Object[] extractParameters(
      Parameter[] parameters,
      Map<String, String> formFields,
      Map<String, byte[]> fileFields,
      Map<String, String> fileTypes
  ) {
    return Arrays.stream(parameters)
        .map(param -> mapParameter(param, formFields, fileFields, fileTypes))
        .toArray();
  }

  private Object mapParameter(
      Parameter param,
      Map<String, String> formFields,
      Map<String, byte[]> fileFields,
      Map<String, String> fileTypes
  ) {
    String name = param.getName();
    Class<?> type = param.getType();

    if (type.equals(byte[].class)) {
      return mapByteArray(name, fileFields);
    }

    if (type.equals(String.class)) {
      return mapString(name, formFields, fileTypes);
    }

    if (type.getSimpleName().equals(Image.class.getSimpleName())) {
      return mapImage(type, name, fileFields, fileTypes);
    }

    return formFields.getOrDefault(name, null);
  }

  private byte[] mapByteArray(String name, Map<String, byte[]> fileFields) {
    return fileFields.get(name);
  }

  private String mapString(
      String name,
      Map<String, String> formFields,
      Map<String, String> fileTypes
  ) {
    if (fileTypes.containsKey(name)) {
      return fileTypes.get(name);
    }
    return formFields.getOrDefault(name, null);
  }

  private Object mapImage(
      Class<?> type,
      String name,
      Map<String, byte[]> fileFields,
      Map<String, String> fileTypes
  ) {
    byte[] data = fileFields.get(name);
    String mimeType = fileTypes.get(name);
    if (data == null || mimeType == null) {
      return null;
    }

    try {
      Constructor<?> ctor = type.getDeclaredConstructor(byte[].class, String.class);
      return ctor.newInstance(data, mimeType);
    } catch (Exception e) {
      throw new RuntimeException("이미지 타입 생성 실패: " + type.getSimpleName(), e);
    }
  }
}
