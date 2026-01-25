package application.usecase;

import annotation.Singleton;
import application.db.UserDao;
import application.model.User;
import application.request.UserRequest;
import java.util.Collection;


@Singleton
public class RegisterUseCase {

  private UserDao userDao;

  public RegisterUseCase(UserDao userDao) {
    this.userDao = userDao;
  }

  public boolean register(UserRequest.RegisterRequest dto) {
    if (existsUserId(dto.userId())) {
      return false;
    }
    User user = toUser(dto);

    userDao.addUser(user);
    user = userDao.findUserById(user.userId());
    return true;
  }

  private User toUser(UserRequest.RegisterRequest dto) {
    return new User(
        dto.userId(),
        dto.password(),
        dto.nickname(),
        dto.email()
    );
  }

  private boolean existsUserId(String userId) {
    Collection<String> userIds = userDao.findAllIds();
    for (String id : userIds) {
      if (userId.equals(id)) {
        return true;
      }
    }
    return false;
  }
}
