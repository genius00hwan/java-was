package application.usecase.rendering;

import annotation.Singleton;
import application.exception.server.ResourceMissException;
import application.model.User;
import http.body.StaticResourceBody;
import http.request.Path;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import staticresource.StaticFileLoader;

@Singleton
public class MyPage implements RenderUseCase<User> {

  private static final Path USER_INFO_PATH = new Path("/user/mypage/index.html");
  private static final String NICKNAME_PLACEHOLDER = "{{nickname}}";
  private static final String ALERT_PLACEHOLDER = "{{alertMessage}}";
  private static final String PROFILE_IMAGE_PLACEHOLDER = "{{profileImage}}";
  private static final Logger log = LoggerFactory.getLogger(MyPage.class);

  @Override
  public StaticResourceBody render(User user) {
    StaticResourceBody resource = StaticFileLoader
        .tryLoad(USER_INFO_PATH)
        .orElseThrow(() -> new ResourceMissException(USER_INFO_PATH.toString()));

    log.info("user : {}", user.toString());
    return RenderUseCase.replacePlaceholder(
        resource,
        Map.of(
            NICKNAME_PLACEHOLDER, user.getNickname(),
            PROFILE_IMAGE_PLACEHOLDER, user.getImage().encodeImage()
        )
    );
  }

  public StaticResourceBody renderWithAlert(User user, String alertMessage) {
    StaticResourceBody resource = StaticFileLoader
        .tryLoad(USER_INFO_PATH)
        .orElseThrow(() -> new ResourceMissException(USER_INFO_PATH.toString()));

    return RenderUseCase.replacePlaceholder(
        resource,
        Map.of(
            NICKNAME_PLACEHOLDER, user.getNickname(),
            PROFILE_IMAGE_PLACEHOLDER, user.getImage().encodeImage(),
            ALERT_PLACEHOLDER, alertMessage != null ? alertMessage : "")
    );
  }
}
