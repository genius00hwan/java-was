package application.auth;

import annotation.Singleton;
import application.db.UserDao;
import application.model.User;
import http.request.HttpRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

@Singleton
public class AuthInjector {

  private final AuthSession authSession;
  private final UserDao database;

  public AuthInjector(AuthSession authSession, UserDao database) {
    this.authSession = authSession;
    this.database = database;
  }

  public <T> T injectIfNeed(T dto, HttpRequest httpRequest) {
    String sid = httpRequest.cookieValue("sid");
    if (sid == null || !authSession.isValid(sid)) {
      return dto;
    }

    String userId = authSession.getUserId(sid);
    User user = database.findUserById(userId);
    if (user == null) {
      return dto;
    }

    if (!dto.getClass().isRecord()) {
      return dto; // record 아닌 경우 무시
    }

    try {
      Constructor<?> constructor = dto.getClass().getDeclaredConstructors()[0];
      Parameter[] params = constructor.getParameters();

      Object[] args = Arrays.stream(params)
          .map(p -> {
            Class<?> type = p.getType();
            if (type.equals(User.class)) {
              return user;
            } else if (type.equals(Optional.class)) {
              return Optional.of(user);
            }
            try {
              // 기존 dto에서 필드 가져오기
              Method accessor = dto.getClass().getDeclaredMethod(p.getName());
              return accessor.invoke(dto);
            } catch (Exception e) {
              return null; // null 허용
            }
          })
          .toArray();

      return (T) constructor.newInstance(args);

    } catch (Exception e) {
      throw new RuntimeException("[AuthInjector] record 주입 실패", e);
    }
  }

}
