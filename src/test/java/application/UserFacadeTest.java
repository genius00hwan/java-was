//package application;
//
//import application.db.Database;
//import application.facade.UserFacade;
//import application.model.User;
//import application.request.UserRequest;
//import application.usecase.RegisterUseCase;
//import http.response.HttpResponse;
//import http.response.HttpStatus;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.*;
//
//
//class UserFacadeTest {
//
//    private UserFacade userEntry;
//    private Database fakeDatabase;
//    private RegisterUseCase registerUseCase;
//
//    @BeforeEach
//    void setUp() {
//        fakeDatabase = new Database();
//        registerUseCase = new RegisterUseCase(fakeDatabase);
//        userEntry = new UserFacade(registerUseCase);
//    }
//
//    @Test
//    @DisplayName("회원가입 성공")
//    void register_success() {
//        // given
//        UserRequest.RegisterRequest request = new UserRequest.RegisterRequest(
//                "testuser",
//                "pass123",
//                "test",
//                "test@test.com"
//        );
//
//        // when
//        HttpResponse response = userEntry.register(request);
//
//        // then
//        assertThat(response.status()).isEqualTo(HttpStatus.FOUND); // redirect → 302 FOUND
//        User savedUser = fakeDatabase.findUserById("testuser");
//        assertThat(savedUser).isNotNull();
//        assertThat(savedUser.getUserId()).isEqualTo("testuser");
//        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
//    }
//
//    @Test
//    @DisplayName("파라미터가 하나라도 누락되면 BadRequest 반환")
//    void register_missing_params_returns_bad_request() {
//        // given
//        UserRequest.RegisterRequest invalidRequest = new UserRequest.RegisterRequest(
//                "",         // userId 누락
//                "pass123",
//                "test",
//                "test@test.com"
//        );
//
//        // when
//        HttpResponse response = userEntry.register(invalidRequest);
//
//        // then
//        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//}
