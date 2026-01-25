package application.usecase.rendering;

import annotation.Singleton;
import application.exception.server.ResourceMissException;
import application.model.User;
import http.body.StaticResourceBody;
import http.request.Path;
import java.util.Map;
import staticresource.StaticFileLoader;

@Singleton
public class WritingPage implements RenderUseCase<User> {

  private static final Path WRITING_PAGE = new Path("/article/index.html");
  private static final String NICKNAME_PLACEHOLDER = "{{nickname}}";

  @Override
  public StaticResourceBody render(User user) {
    StaticResourceBody resourceBody = StaticFileLoader.tryLoad(WRITING_PAGE)
        .orElseThrow(
            () -> new ResourceMissException(WRITING_PAGE.toString())
        );
    return RenderUseCase.replacePlaceholder(
        resourceBody,
        Map.of(
            NICKNAME_PLACEHOLDER, user.getNickname()
        )
    );
  }
}
