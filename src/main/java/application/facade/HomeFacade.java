package application.facade;

import annotation.Singleton;
import application.auth.AuthInjector;
import application.request.GeneralRequest;
import application.request.GeneralRequest.LoginUser;
import application.request.UserRequest;
import application.router.mapper.FormDataMapper;
import application.router.MethodHandler;
import application.router.mapper.VoidMapper;
import application.usecase.rendering.HomePage;
import application.usecase.RegisterUseCase;
import http.body.StaticResourceBody;
import http.request.HttpMethod;
import http.response.HttpResponse;
import http.response.HttpResponseFactory;
import http.response.HttpStatus;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.RouteKey;

/**
 * /,/register 처리
 */
@Singleton
public class HomeFacade extends Facade {

  private static final String REQUEST_MAPPING = "/";
  private static final String HOME_PATH = "/";
  private static final String REGISTER_PATH = "/register";
  private static final Logger log = LoggerFactory.getLogger(HomeFacade.class);

  private final HomePage homePage;
  private final RegisterUseCase registerUseCase;

  public HomeFacade(
      AuthInjector authInjector,
      HomePage homePage,
      RegisterUseCase registerUseCase
  ) {
    super(authInjector);
    this.homePage = homePage;
    this.registerUseCase = registerUseCase;
  }

  @Override
  protected void createRouteMap() {
    registerRoute(HOME_PATH, HttpMethod.GET,
        new VoidMapper<>(LoginUser.class),
        this::home);

    registerRoute(REGISTER_PATH, HttpMethod.POST,
        new FormDataMapper<>(UserRequest.RegisterRequest.class),
        this::register);
  }

  private HttpResponse home(LoginUser dto) {
    StaticResourceBody resource = homePage.render(dto.user());

    return HttpResponseFactory.responseForStaticFile(
        HttpStatus.OK,
        resource.contentType(),
        resource.asBytes()
    );
  }

  private HttpResponse register(UserRequest.RegisterRequest dto) {
    if(registerUseCase.register(dto)) {
      return HttpResponseFactory.redirect(
          HOME_PATH
      );
    }

    return HttpResponseFactory.redirect(
        REGISTER_PATH+"?duplicate=true"
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

