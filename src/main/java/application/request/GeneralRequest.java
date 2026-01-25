package application.request;

import application.model.User;
import java.util.Optional;

public class GeneralRequest {
  public record LoginUser(
      Optional<User> user
  ){}

}
