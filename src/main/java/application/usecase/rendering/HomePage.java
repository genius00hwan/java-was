package application.usecase.rendering;

import annotation.Singleton;

import application.db.ArticleDao;
import application.exception.server.ResourceMissException;
import application.model.holder.ArticleWithWriter;
import application.model.User;

import http.body.StaticResourceBody;
import http.request.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import staticresource.StaticFileLoader;

@Singleton
public class HomePage implements RenderUseCase<Optional<User>> {

  private static final Path HOME_PATH = new Path("/");
  private static final String MY_PAGE_VALUE = "/user/mypage";
  private static final String LOGIN_PATH = "/login";

  private static final String BTN_TEMPLATE =
      "<a class='btn btn_contained btn_size_s' href='%s'>%s</a>";

  private static final String REGISTER_BUTTON =
      """
              <a class='btn btn_ghost btn_size_s' href='/register'>회원 가입</a>
          """;

  private static final String LOGOUT_BUTTON =
      """
               <form action="/logout" method="post" style="display:inline;">
                            <button class="btn btn_ghost btn_size_s" type="submit">
                              로그아웃
                            </button>
                          </form>
          """;
  private static final String ARTICLE_HTML_FORMAT =
      """
              <li class="article-list__item">
                <div class="article-list__user">
                  <span class="article-list__user__nickname">%s</span>
                  <a class="article-list__link" href="%s">
                    <span class="article-list__title">%s</span>
                  </a>
                </div>
              </li>
          """;

  private static final String LOGIN_SECTION = "{{loginSection}}";
  private static final String ARTICLE_LIST = "{{articleListSection}}";
  private static final String REGISTER_SECTION = "{{registerSection}}";

  private static final Logger log = LoggerFactory.getLogger(HomePage.class);


  private final ArticleDao articleDao;

  public HomePage(ArticleDao articleDao) {
    this.articleDao = articleDao;
  }

  @Override
  public StaticResourceBody render(Optional<User> user) {
    StaticResourceBody staticResource = StaticFileLoader.tryLoad(
        HOME_PATH).orElseThrow(
        () -> new ResourceMissException(HOME_PATH.toString())
    );

    return processResource(
        staticResource,
        user);
  }


  private StaticResourceBody processResource(
      StaticResourceBody resource,
      Optional<User> user
  ) {

    String loginSection = user
        .map(value -> String.format(
            BTN_TEMPLATE,
            MY_PAGE_VALUE, value.getNickname()
        ))
        .orElse(String.format(
            BTN_TEMPLATE,
            LOGIN_PATH, "로그인"
        ));

    String registerSection = user.map(
        value -> LOGOUT_BUTTON).orElse(REGISTER_BUTTON);

    return RenderUseCase.replacePlaceholder(resource,
        Map.of(
            LOGIN_SECTION, loginSection,
            ARTICLE_LIST, buildArticleHtml(),
            REGISTER_SECTION, registerSection
        )
    );
  }


  private String buildArticleHtml() {
    List<ArticleWithWriter> articles = articleDao.findAll();

    if (articles.isEmpty()) {
      return "";
    }
    return articles.stream()
        .map(article -> String.format(
            ARTICLE_HTML_FORMAT,
            article.writer(),
            article.getLink(),
            article.title()
        ))
        .collect(Collectors.joining("\n"));
  }

}
