package application.model;


import application.auth.AuthTarget;
import application.exception.client.BadRequestException;
import application.model.holder.Image;
import application.utils.EncryptUtil;
import application.utils.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User extends AuthTarget {

  private static final Logger log = LoggerFactory.getLogger(User.class);
  private String nickname;
  private final String email;
  private Image image;


  public User(String userId, String password, String nickname, String email) {
    super(userId, password);
    validateUserProperties(this.userId, this.password, nickname, email);
    this.nickname = nickname;
    this.email = email;
  }

  private User(
      String userId, String encryptedPassword, String nickname, String email, Image image
  ) {
    super(userId, encryptedPassword);
    this.password = encryptedPassword;
    this.nickname = nickname;
    this.email = email;
    this.image = image;
  }

  public static User fromDb(
      String userId, String encryptedPassword, String nickname, String email, Image image
  ) {
    return new User(
        userId,
        encryptedPassword,
        nickname,
        email,
        image
    );
  }


  public String getNickname() {
    return nickname;
  }

  public String getEmail() {
    return email;
  }

  public Image getImage() {
    return image;
  }

  public void update(String nickname, String newPassword, Image image) {
    validateUserProperties(this.userId, password, nickname, this.email);
    this.password = confirmPassword(newPassword);
    this.nickname = nickname;
    this.image = image;
    log.info("image {}", image.toString());
  }

  @Override
  public String toString() {
    return "User [userId=" + userId + ", password=" + password + ",nickname=" + nickname
        + ", email=" + email + "]";
  }

  @Override
  protected String encrypt(String rawPassword) {
    return EncryptUtil.hash(rawPassword);
  }

  @Override
  public boolean isValidPassword(String password) {
    return this.password.equals(encrypt(password));
  }

  private void validateUserProperties(String userId, String password, String nickname,
      String email) {

    if (ValidateUtil.isNullOrBlank(userId, password, nickname, email)) {
      String message = "[CREATE USER] INVALID PARAMETER" + " userId=" + userId +
          " password=" + password +
          " nickname=" + nickname +
          " email=" + email;

      throw new BadRequestException(message);
    }
    ;
  }

  private String confirmPassword(String newPassword) {
    if (newPassword.isEmpty()) {
      return this.password;
    }
    if (this.password.equals(encrypt(newPassword))) {
      return this.password;
    }
    return encrypt(newPassword);
  }
}
