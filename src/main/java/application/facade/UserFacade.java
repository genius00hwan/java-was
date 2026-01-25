package application.facade;

import annotation.Singleton;
import application.auth.AuthInjector;
import application.exception.client.WhoRUException;
import application.request.GeneralRequest.LoginUser;
import application.request.UserRequest;

import application.router.mapper.MultipartMapper;
import application.router.mapper.VoidMapper;
import application.usecase.UpdateUserUseCase;
import application.usecase.rendering.MyPage;
import http.body.StaticResourceBody;
import http.request.HttpMethod;
import http.response.HttpResponse;
import http.response.HttpResponseFactory;
import http.response.HttpStatus;



@Singleton
public class UserFacade extends Facade {

  private static final String REQUEST_MAPPING = "/user";
  private static final String MY_PAGE = "/mypage";
  private static final String UPDATE_PATH = "/update";
  private final MyPage myPage;
  private final UpdateUserUseCase updateUserUseCase;

  public UserFacade(AuthInjector authInjector,
      MyPage myPage,
      UpdateUserUseCase updateUserUseCase
  ) {
    super(authInjector);
    this.myPage = myPage;
    this.updateUserUseCase = updateUserUseCase;
  }

  @Override
  protected void createRouteMap() {
    registerRoute(MY_PAGE, HttpMethod.GET,
        new VoidMapper<>(LoginUser.class),
        this::getUserInfo);

    registerRoute(UPDATE_PATH,HttpMethod.POST,
        new MultipartMapper<>(UserRequest.UpdateRequest.class),
        this::updateUserInfo);
  }

  private HttpResponse getUserInfo(LoginUser request) {
    if (request.user().isEmpty()){
      throw new WhoRUException();
    }
    StaticResourceBody resource = myPage.render(request.user().get());
    return HttpResponseFactory.responseForStaticFile(
        HttpStatus.OK,
        resource.contentType(),
        resource.asBytes()
    );

  }

  private HttpResponse updateUserInfo(UserRequest.UpdateRequest request) {
    String alertMessage = extractAlertMessage(
        updateUserUseCase.update(request));

    StaticResourceBody resource = myPage.
        renderWithAlert(request.user(), alertMessage);

    return HttpResponseFactory.responseForStaticFile(
        HttpStatus.OK,
        resource.contentType(),
        resource.asBytes()
    );
  }


  @Override
  public String basePath() {
    return REQUEST_MAPPING;
  }

  private String extractAlertMessage(boolean isSuccess) {
    if (isSuccess) {
      return "alert('변경 완료되었습니다!');";
    }
    return "alert('변경 실패: 비밀번호가 올바르지 않습니다.');";
  }
}
