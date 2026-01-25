package application.facade;

import annotation.Singleton;
import application.auth.AuthInjector;
import application.exception.client.WhoRUException;
import application.request.ArticleRequest;
import application.request.GeneralRequest.LoginUser;
import application.router.MethodHandler;
import application.router.mapper.MultipartMapper;
import application.router.mapper.QueryParameterMapper;
import application.router.mapper.VoidMapper;
import application.usecase.PostingUseCase;
import application.usecase.rendering.ArticlePage;
import application.usecase.rendering.WritingPage;
import http.ContentType;
import http.body.StaticResourceBody;
import http.request.HttpMethod;
import http.response.HttpResponse;
import http.response.HttpResponseFactory;
import http.response.HttpStatus;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.RouteKey;

@Singleton
public class ArticleFacade extends Facade {

  private static final String REQUEST_MAPPING = "/article";
  private static final String DEFAULT = "/";
  private static final String WRITE= "/write";
  private static final String HOME_PATH = "/";
  private static final Logger log = LoggerFactory.getLogger(ArticleFacade.class);

  private final WritingPage writingPage;
  private final PostingUseCase postingUseCase;
  private final ArticlePage articlePage;

  public ArticleFacade(
      AuthInjector authInjector,
      WritingPage writingPage,
      PostingUseCase postingUseCase,
      ArticlePage articlePage
  ) {
    super(authInjector);
    this.writingPage = writingPage;
    this.postingUseCase = postingUseCase;
    this.articlePage = articlePage;
  }

  @Override
  protected void createRouteMap() {
    registerRoute(WRITE, HttpMethod.GET,
        new VoidMapper<>(LoginUser.class),
        this::getWritingForm);

    registerRoute(DEFAULT, HttpMethod.POST,
        new MultipartMapper<>(ArticleRequest.CreateRequest.class),
        this::createArticle);

    registerRoute(DEFAULT, HttpMethod.GET,
        new QueryParameterMapper<>(ArticleRequest.GetRequest.class),
        this::getArticle);
  }

  private HttpResponse getWritingForm(LoginUser request) {
    if (request.user().isEmpty()) {
      throw new WhoRUException();
    }
    StaticResourceBody resource = writingPage.render(request.user().get());

    return HttpResponseFactory.responseForStaticFile(
        HttpStatus.OK,
        resource.contentType(),
        resource.asBytes()
    );
  }

  private HttpResponse createArticle(ArticleRequest.CreateRequest createRequest) {
    if (postingUseCase.postArticle(createRequest)) {
      return HttpResponseFactory.redirect(
          HOME_PATH);
    }
    return HttpResponseFactory.redirect(
        REQUEST_MAPPING + "?failure=true");
  }

  private HttpResponse getArticle(ArticleRequest.GetRequest request) {
    return HttpResponseFactory.responseForStaticFile(
        HttpStatus.OK, ContentType.HTML,
        articlePage.render(Long.parseLong(request.id())).asBytes()
    );
  }


  @Override
  public String basePath() {
    return REQUEST_MAPPING;
  }

  @Override
  public Map<RouteKey, MethodHandler<?>> routes() {
    return routeMap;
  }
}
