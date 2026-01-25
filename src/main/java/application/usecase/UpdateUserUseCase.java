package application.usecase;

import annotation.Singleton;
import application.db.UserDao;
import application.model.User;
import application.request.UserRequest;

@Singleton
public class UpdateUserUseCase {

  private UserDao userDao;

  public UpdateUserUseCase(UserDao userDao) {
    this.userDao = userDao;
  }

  public boolean update(UserRequest.UpdateRequest dto) {
    User user = dto.user();
    if (!user.isValidPassword(dto.oldPassword())) {
      return false;
    }
    if (!correctNewPassword(dto.newPassword(), dto.confirmPassword())) {
      return false;
    }
    user.update(dto.nickname(), dto.newPassword(), dto.image());
    userDao.updateUser(user);
    return true;
  }

  private boolean correctNewPassword(
      String newPassword, String confirmedPassword) {
    return newPassword.equals(confirmedPassword);
  }
}
