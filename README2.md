# 나만의 WAS 만들기 — 설계 아키텍처 및 디자인 패턴 심화 분석

## 📋 목차
1. [프로젝트 개요](#프로젝트-개요)
2. [핵심 아키텍처](#핵심-아키텍처)
3. [주요 디자인 패턴과 의도](#주요-디자인-패턴과-의도)
4. [계층별 설계 의도](#계층별-설계-의도)
5. [요청 처리 흐름](#요청-처리-흐름)
6. [확장성과 유지보수성](#확장성과-유지보수성)

---

## 프로젝트 개요

이 프로젝트는 **학습용 경량 WAS(Web Application Server)**로서, 실제 웹 서버의 핵심 설계 원칙을 구현합니다.

### 주요 목표
- HTTP 연결 관리 및 Keep-Alive 지원
- 요청 파이프라인 구축 (필터 → 디스패처 → 서블릿)
- 동적 라우팅 및 메서드 핸들링
- 정적 자원 제공 분리
- 최소한의 DI(Dependency Injection) 컨테이너 구현

### 기술 스택
- **언어**: Java 17+
- **데이터베이스**: H2 (인메모리/파일 기반)
- **HTTP 파싱**: 직접 구현
- **라우팅**: 직접 구현
- **DI**: 반사(Reflection) 기반 간단한 컨테이너

---

## 핵심 아키텍처

### 전체 요청 흐름 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│ 1. 클라이언트 요청                                           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. ConnectionManager: 소켓 수명 및 연결 풀 관리             │
│    - Keep-Alive 관리                                        │
│    - 버퍼 닫기 시점 처리                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. RequestHandler: HTTP 요청 파싱                           │
│    - 요청 라인, 헤더, 본문 분석                             │
│    - 요청 메서드별 본문 처리 (Form, Multipart 등)          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. FilterChain: 교차 관심사 처리 (Chain of Responsibility)  │
│    - AuthFilter: 인증 확인                                  │
│    - LoginFilter: 로그인 처리                               │
│    - 기타 공통 필터들                                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. Dispatcher (Front Controller)                            │
│    - AppServlet 또는 StaticServlet으로 라우팅              │
└────────────────────┬────────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
         ▼                       ▼
┌──────────────────┐    ┌──────────────────┐
│ AppServlet       │    │ StaticServlet    │
│ (동적 요청)      │    │ (정적 자원)      │
└──────────────────┘    └──────────────────┘
         │                       │
         ▼                       │
┌─────────────────────────────────┐
│ AppRouteConfig: 라우트 매핑     │
│ RouteKey(Path, Method) →        │
│ Facade → MethodHandler          │
└─────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ MethodHandler:                  │
│ 1. ArgumentMapper로 요청 매핑   │
│ 2. AuthInjector로 인증 정보 주입│
│ 3. 핸들러 함수 실행             │
└─────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ Facade (UseCase 진입점)         │
│ - HomeFacade                    │
│ - ArticleFacade                 │
│ - UserFacade                    │
└─────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ UseCase (비즈니스 로직)         │
│ - RegisterUseCase               │
│ - PostingUseCase                │
│ - UpdateUserUseCase             │
└─────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ DAO / DataSource                │
│ - UserDao                       │
│ - ArticleDao                    │
└─────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│ H2 Database                     │
└─────────────────────────────────┘
```

---

## 주요 디자인 패턴과 의도

### 1. **DI 컨테이너 + Singleton 패턴**

**위치**: `container/DIContainer.java`, `annotation/Singleton.java`

**설계 의도**:
- 의존성 자동 주입으로 객체 결합도 낮추기
- 싱글톤 애노테이션으로 전역 상태 관리 및 메모리 효율화
- 리플렉션 기반으로 구성 파일 없이 자동 의존성 주입

**특징**:
```java
@Singleton  // 애노테이션으로 싱글톤 표시
public class DataSource { ... }

// 자동 의존성 주입
DIContainer.getInstance(Dispatcher.class);  // 생성자 매개변수도 자동 주입
```

**이점**:
- ✅ 테스트 용이: Mock 객체 교체 가능
- ✅ 순환 의존성 감지: ThreadLocal 사용
- ✅ 초기화 안전성: 생성 중 상태 추적

---

### 2. **Chain of Responsibility 패턴 (필터 체인)**

**위치**: `filter/Filter.java`, `filter/FilterChain.java`

**설계 의도**:
- 요청 처리 단계를 순차적 체인으로 구성
- 각 필터가 독립적으로 요청을 가로채기(intercept)
- 새로운 필터 추가 시 기존 코드 수정 불필요

**구현 흐름**:
```
AuthFilter → LoginFilter → ... → Dispatcher
     │                              │
     └──────────────────────────────┘
          (필터 체인 거쳐야 함)
```

**핵심 클래스**:
```java
// Filter 인터페이스: 단일 책임 - 한 가지 요청 처리만
public interface Filter {
    HttpResponse doFilter(HttpRequest request, FilterChain chain);
}

// FilterChain: 다음 필터로 전파
public class FilterChain {
    public HttpResponse doChain(HttpRequest request) {
        if (index < filters.size()) {
            return filters.get(index++).doFilter(request, this);
        }
        return dispatcher.dispatch(request);  // 마지막에 디스패처 호출
    }
}
```

**이점**:
- ✅ 관심사 분리: 인증, 로깅, 캐싱 등을 독립 필터로 구현
- ✅ 동적 필터 추가/제거 가능
- ✅ 필터 순서 조정 용이

---

### 3. **Front Controller + Dispatcher 패턴**

**위치**: `router/Dispatcher.java`, `router/servlet/AppServlet.java`

**설계 의도**:
- 모든 동적 요청이 단일 진입점(Dispatcher)을 지나감
- 동적/정적 요청을 분리해 처리
- 중앙 집중식 요청 분배로 공통 로직 처리 용이

**구현**:
```java
@Singleton
public class Dispatcher {
    public HttpResponse dispatch(HttpRequest request) {
        try {
            // 1차: 동적 요청 처리 시도
            Optional<HttpResponse> response = appServlet.service(request);
            if (response.isPresent()) return response.get();
            
            // 2차: 정적 자원 제공
            response = staticServlet.service(request);
            return response.orElseGet(HttpResponseFactory::notFound);
        } catch (Exception e) {
            return HttpResponseFactory.internalServerError(e.getMessage());
        }
    }
}
```

**이점**:
- ✅ 요청 흐름 추적 용이
- ✅ 예외 처리 중앙 관리
- ✅ 요청 전처리/후처리 일관성

---

### 4. **Strategy 패턴 (라우팅 및 메서드 핸들링)**

**위치**: `application/router/MethodHandler.java`, `router/AppRouteConfig.java`

**설계 의도**:
- 각 경로/메서드별로 다른 처리 전략(핸들러 함수) 설정
- 런타임에 경로에 따라 핸들러 동적 선택
- 새 엔드포인트 추가 시 전체 시스템 수정 불필요

**라우트 등록 방식**:
```java
// RouteKey = (Path, HttpMethod) 조합
Map<RouteKey, MethodHandler<?>> routeMap = new HashMap<>();

routeMap.put(
    new RouteKey(new Path("/register"), HttpMethod.POST),
    new MethodHandler<>(mapper, handler, authInjector)
);
```

**이점**:
- ✅ 엔드포인트 추가가 간단한 맵 수정으로 끝남
- ✅ 다양한 핸들러 전략 동시 운영 가능
- ✅ 테스트할 때 핸들러 Mock 주입 가능

---

### 5. **Facade 패턴 (비즈니스 로직 진입점)**

**위치**: `application/facade/Facade.java` (상위 클래스)
- `HomeFacade.java` - 홈, 회원가입 처리
- `ArticleFacade.java` - 게시글 처리
- `UserFacade.java` - 사용자 정보 처리

**설계 의도**:
- 여러 UseCase를 단일 Facade로 묶기
- 컨트롤러 레이어와 비즈니스 로직 간 결합도 낮추기
- 경로별 Facade를 조합하여 전체 라우팅 구성

**구현 방식**:
```java
@Singleton
public class HomeFacade extends Facade {
    private final HomePage homePage;
    private final RegisterUseCase registerUseCase;
    
    protected void createRouteMap() {
        // 경로별로 ArgumentMapper와 핸들러를 등록
        registerRoute(
            "/",
            HttpMethod.GET,
            new VoidMapper<>(GeneralRequest.class),
            req -> homePage.render(...)  // 홈 페이지 렌더링
        );
        
        registerRoute(
            "/register",
            HttpMethod.POST,
            new FormDataMapper<>(UserRequest.class),
            req -> registerUseCase.execute(...)  // 회원가입 실행
        );
    }
}
```

**이점**:
- ✅ 기능별 Facade로 코드 응집도 향상
- ✅ 경로 기반 라우팅이 직관적
- ✅ Facade 추가만으로 새 엔드포인트 집합 추가 가능

---

### 6. **ArgumentMapper 패턴 (요청 매핑)**

**위치**: `application/router/mapper/`
- `FormDataMapper` - form-urlencoded 요청
- `MultipartMapper` - multipart/form-data 요청
- `QueryParameterMapper` - URL 쿼리 매개변수
- `VoidMapper` - 매개변수 없음
- 기타 커스텀 매퍼들

**설계 의도**:
- 요청 형식에 따라 다른 매핑 전략 적용
- 리플렉션을 활용한 자동 매핑으로 보일러플레이트 코드 최소화
- 요청 타입별로 독립적인 매퍼로 확장성 확보

**사용 예**:
```java
// FormDataMapper: application/x-www-form-urlencoded
registerRoute(
    "/login",
    HttpMethod.POST,
    new FormDataMapper<>(LoginRequest.class),  // 자동 매핑
    loginHandler
);

// MultipartMapper: multipart/form-data (파일 업로드)
registerRoute(
    "/article",
    HttpMethod.POST,
    new MultipartMapper<>(ArticleRequest.class),  // 이미지 + 데이터
    postingHandler
);
```

**이점**:
- ✅ 요청 형식별 복잡한 파싱 로직 캡슐화
- ✅ 새 요청 타입 추가 시 새 Mapper 구현만 필요
- ✅ 핸들러는 매핑된 DTO로 깔끔하게 작업

---

### 7. **Template Method 패턴 (렌더링)**

**위치**: `application/usecase/rendering/`
- `HomePage` - 홈 페이지 렌더링
- `ArticlePage` - 게시글 상세 페이지 렌더링
- `WritingPage` - 글쓰기 페이지 렌더링

**설계 의도**:
- HTML 템플릿 기반 렌더링 프로세스 통일
- 데이터를 템플릿에 바인딩
- 페이지별 렌더링 로직 분리

**예시**:
```java
// ArticlePage: 글 상세 조회 후 템플릿에 바인딩
public class ArticlePage {
    public HttpResponse render(Article article, LoginUser user) {
        String html = TemplateEngine.render("article/template.html", {
            "writer": article.getWriterId(),
            "title": article.getTitle(),
            "image": article.getImageUrl(),
            "content": article.getContent(),
        });
        return HttpResponseFactory.html(html);
    }
}
```

**이점**:
- ✅ 렌더링 로직과 비즈니스 로직 분리
- ✅ 템플릿만 변경하면 렌더링 변경 가능
- ✅ 자동 HTML 이스케이핑 및 보안 강화 가능

---

### 8. **Data Access Object (DAO) 패턴**

**위치**: `application/db/`
- `UserDao` - 사용자 CRUD
- `ArticleDao` - 게시글 CRUD
- `DataSource` - H2 연결 관리

**설계 의도**:
- 데이터베이스 접근을 캡슐화
- SQL 쿼리를 DAO에 집중
- 향후 데이터베이스 변경 시 DAO만 수정

**예시**:
```java
@Singleton
public class UserDao {
    private final DataSource dataSource;
    
    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO USERS (USER_ID, PASSWORD, ...) VALUES (?, ?, ...)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUserId());
            // ... 매개변수 설정
            stmt.executeUpdate();
        }
    }
}
```

**이점**:
- ✅ 비즈니스 로직과 데이터 접근 분리
- ✅ 데이터베이스 마이그레이션 용이
- ✅ 테스트할 때 Mock DAO 주입 가능

---

## 계층별 설계 의도

### 계층 구조

```
┌────────────────────────────────────────┐
│ Presentation (HTTP 요청/응답)          │
│ - Filter, Dispatcher, Servlet          │
└────────────────────────────────────────┘
                  ▲
                  │ HttpRequest/Response
                  ▼
┌────────────────────────────────────────┐
│ Control/Routing (요청 라우팅)          │
│ - RouteKey, MethodHandler, Facade      │
│ - ArgumentMapper                       │
└────────────────────────────────────────┘
                  ▲
                  │ DTO (Data Transfer Object)
                  ▼
┌────────────────────────────────────────┐
│ Application/Business Logic (UseCase)   │
│ - RegisterUseCase, PostingUseCase      │
│ - AuthInjector, AuthSession            │
└────────────────────────────────────────┘
                  ▲
                  │ Domain Model
                  ▼
┌────────────────────────────────────────┐
│ Domain (비즈니스 엔티티)               │
│ - User, Article, Cookie, LoginUser     │
└────────────────────────────────────────┘
                  ▲
                  │ CRUD 메서드
                  ▼
┌────────────────────────────────────────┐
│ Data Access (DAO)                      │
│ - UserDao, ArticleDao, DataSource      │
└────────────────────────────────────────┘
                  ▲
                  │ SQL, Connection
                  ▼
┌────────────────────────────────────────┐
│ Persistence (H2 Database)              │
│ - USERS, ARTICLE 테이블               │
└────────────────────────────────────────┘
```

---

## 요청 처리 흐름

### 1. GET / (홈페이지)

```
1. 클라이언트 GET 요청
   ↓
2. ConnectionManager: 소켓 관리
   ↓
3. RequestHandler: HTTP 요청 파싱
   ↓
4. FilterChain 통과
   - AuthFilter: 인증 체크 (홈은 오픈 경로)
   ↓
5. Dispatcher: 동적/정적 분류
   → AppServlet으로 라우팅 (앱 요청이 아님)
   ↓
6. StaticServlet: index.html 제공
   ↓
7. HttpResponse 반환 (200 OK + HTML)
```

### 2. POST /register (회원가입)

```
1. 클라이언트 POST 요청 (form-urlencoded)
   ↓
2. FilterChain 통과
   - AuthFilter: 인증 불필요 (오픈 경로)
   ↓
3. Dispatcher → AppServlet
   ↓
4. AppRouteConfig: 라우트 맵에서 매칭
   RouteKey("/register", POST) 찾기
   ↓
5. MethodHandler 실행
   a) FormDataMapper.map(request)
      → UserRequest 객체로 매핑
   b) AuthInjector.injectIfNeed(dto, request)
      → 인증 정보 필요 없음 (null)
   c) registerHandler.apply(dto)
      → RegisterUseCase.execute(userRequest)
   ↓
6. UseCase 비즈니스 로직
   a) 중복 아이디 검사 (UserDao.find)
   b) 비밀번호 암호화 (EncryptUtil)
   c) 사용자 저장 (UserDao.insert)
   ↓
7. HttpResponse (201 Created or 400 Bad Request)
```

### 3. POST /login (로그인)

```
1. 클라이언트 POST 요청
   ↓
2. FilterChain
   - AuthFilter: 오픈 경로
   - LoginFilter: 로그인 시도 감시
   ↓
3. AppServlet → /login 라우트
   ↓
4. LoginUseCase.execute(loginRequest)
   a) UserDao.find(userId) - DB 조회
   b) 비밀번호 검증 (EncryptUtil.verify)
   ↓
5. 로그인 성공 시
   - AuthSession.addSession(sid) - 세션 저장
   - Cookie("sid", sid) 응답
   - 302 Redirect → /
   ↓
6. 로그인 실패 시
   - 302 Redirect → /login?failure=true
```

### 4. POST /article (게시글 작성, 인증 필요)

```
1. 클라이언트 POST 요청 (multipart/form-data, 쿠키 포함)
   ↓
2. FilterChain
   - AuthFilter: 인증 체크
     a) request.cookieValue("sid") 추출
     b) AuthSession.isValid(sid) 확인
     c) 미인증 → 302 /login?forbidden=true
   ↓
3. AppServlet → /article 라우트
   ↓
4. MethodHandler 실행
   a) MultipartMapper.map(request)
      → ArticleRequest (title, content, image 포함)
   b) AuthInjector.injectIfNeed(dto, request)
      → LoginUser 객체 주입 (세션에서 추출)
      → dto의 optional<LoginUser> 필드 채우기
   c) postingHandler.apply(dto)
      → PostingUseCase.execute(articleRequest, loginUser)
   ↓
5. UseCase
   a) 제목/내용 검증
   b) 이미지 저장 (multipart body에서 추출)
   c) Article 객체 생성
   d) ArticleDao.insert(article) - DB 저장
   ↓
6. HttpResponse (201 Created or 400 Bad Request)
```

---

## 확장성과 유지보수성

### 새 엔드포인트 추가 방법

#### 시나리오: `/api/comment` (댓글 작성) 추가

**1단계**: Facade 생성 또는 기존 Facade 수정

```java
@Singleton
public class CommentFacade extends Facade {
    private final CommentUseCase commentUseCase;
    
    public CommentFacade(AuthInjector authInjector, CommentUseCase usecase) {
        super(authInjector);
        this.commentUseCase = usecase;
    }
    
    @Override
    public String basePath() {
        return "/comment";
    }
    
    @Override
    protected void createRouteMap() {
        registerRoute(
            "/create",  // 최종 경로: /comment/create
            HttpMethod.POST,
            new FormDataMapper<>(CommentRequest.class),
            request -> {
                HttpResponse response = commentUseCase.execute(request);
                return response;
            }
        );
    }
}
```

**2단계**: ApiModule 목록에 Facade 추가

```java
// router/AppRouteConfig에 CommentFacade 등록
List<ApiModule> apiModules = Arrays.asList(
    homeFacade,
    articleFacade,
    userFacade,
    new CommentFacade(authInjector, commentUseCase)  // 추가
);
```

**3단계**: UseCase 구현

```java
public class CommentUseCase {
    private final CommentDao commentDao;
    
    public HttpResponse execute(CommentRequest request) {
        // 비즈니스 로직
        Comment comment = new Comment(...);
        commentDao.insert(comment);
        return HttpResponseFactory.created();
    }
}
```

**4단계**: DAO 및 데이터베이스 테이블 추가

```java
@Singleton
public class CommentDao {
    private final DataSource dataSource;
    
    public void insert(Comment comment) throws SQLException {
        // SQL 쿼리 실행
    }
}

// DataSource.initSchema()에 테이블 생성 쿼리 추가
CREATE TABLE COMMENTS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    ARTICLE_ID BIGINT NOT NULL,
    WRITER_ID VARCHAR(50) NOT NULL,
    CONTENT TEXT,
    CREATED_AT TIMESTAMP,
    FOREIGN KEY (ARTICLE_ID) REFERENCES ARTICLE(ID),
    FOREIGN KEY (WRITER_ID) REFERENCES USERS(USER_ID)
);
```

**5단계**: 요청 DTO 추가

```java
public record CommentRequest(
    Long articleId,
    Optional<LoginUser> loginUser,  // AuthInjector가 자동 주입
    String content
) { }
```

### 기존 코드 수정 없음! ✅

---

### 예외 처리 일관성

**위치**: `application/exception/`
- `client/` - 클라이언트 오류 (4xx)
- `server/` - 서버 오류 (5xx)

**설계**:
```java
// 커스텀 예외
public class IllegalContentTypeException extends RuntimeException { }
public class WhoRUException extends RuntimeException { }  // 인증 오류

// FilterChain 또는 AppServlet에서 통일 예외 처리
try {
    // ... 요청 처리
} catch (IllegalContentTypeException e) {
    return HttpResponseFactory.badRequest(e.getMessage());
} catch (Exception e) {
    return HttpResponseFactory.internalServerError(e.getMessage());
}
```

---

### 인증 및 권한 관리

**위치**: `application/auth/`

**설계**:
- `AuthSession`: 세션 저장소 (ConcurrentHashMap)
- `AuthFilter`: 경로별 인증 필요 여부 판단
- `AuthInjector`: 핸들러에 인증 정보 주입
- `Cookie`: HTTP 쿠키 표현

**흐름**:
```
1. 로그인 성공
   → AuthSession에 (sid) 저장
   → 쿠키에 sid 담아 반환

2. 다음 요청
   → 쿠키에서 sid 추출
   → AuthFilter: AuthSession.isValid(sid) 확인
   → 유효 → 요청 계속
   → 무효 → /login으로 리다이렉트

3. 핸들러 실행
   → AuthInjector가 LoginUser 정보 추출
   → DTO의 Optional<LoginUser> 필드 채우기
   → 핸들러에서 현재 사용자 정보 사용 가능
```

---

### 정적/동적 분리의 이점

**StaticServlet vs AppServlet**

```java
public class Dispatcher {
    public HttpResponse dispatch(HttpRequest request) {
        // 1차: 동적 요청 (앱 로직)
        Optional<HttpResponse> response = appServlet.service(request);
        if (response.isPresent()) return response.get();
        
        // 2차: 정적 자원
        response = staticServlet.service(request);
        return response.orElseGet(HttpResponseFactory::notFound);
    }
}
```

**이점**:
- ✅ 정적 자원 캐싱 가능 (향후 개선)
- ✅ 정적/동적 파일 처리 로직 분리
- ✅ 성능 최적화 선택적 적용
- ✅ 마이크로서비스 분리 용이 (CDN 등으로 정적 전담)

---

## 핵심 설계 원칙 요약

### SOLID 원칙 적용

| 원칙 | 적용 부분 | 효과 |
|------|---------|------|
| **S** (단일 책임) | Filter, Facade, UseCase | 각 컴포넌트가 한 가지만 담당 |
| **O** (열림-닫힘) | Mapper, Filter, Facade | 확장에는 열려있고 수정에는 닫혀있음 |
| **L** (리스코프 치환) | Filter 인터페이스 | 모든 Filter가 동일하게 대체 가능 |
| **I** (인터페이스 분리) | ArgumentMapper, ApiModule | 필요한 메서드만 정의 |
| **D** (의존성 역전) | DIContainer | 고수준 모듈이 저수준에 의존하지 않음 |

### DRY (Don't Repeat Yourself)

- `ArgumentMapper`: 요청 매핑 로직 중앙화
- `HttpResponseFactory`: 응답 생성 일관성
- `FilterChain`: 공통 처리 일관화
- `Template**: HTML 렌더링 템플릿 재사용

### 관심사 분리 (Separation of Concerns)

```
HTTP 처리        ← RequestHandler, ConnectionManager
필터링          ← FilterChain, Filter들
라우팅          ← Dispatcher, AppRouteConfig
요청 변환       ← ArgumentMapper
비즈니스 로직   ← UseCase, Facade
데이터 접근     ← DAO
```

---

## 문제 해결 사례

### 1. 순환 의존성 문제

**문제**: DIContainer가 A 생성 중 B 필요 → B 생성 중 A 필요

**해결**:
```java
ThreadLocal<Map<Class<?>, Boolean>> underConstruction = ...;
// 생성 중인 클래스 추적 → 순환 의존 감지
```

### 2. Keep-Alive 연결 유지

**문제**: 하나의 소켓으로 여러 HTTP 요청 처리

**해결**:
```java
// ConnectionManager에서 소켓 재사용 관리
// BufferedReader/Writer 닫기 시점 명확히 처리
```

### 3. Multipart 파일 업로드 처리

**문제**: Form 데이터와 파일을 함께 처리

**해결**:
```java
// MultipartMapper에서 경계(boundary) 파싱
// 파일 바이너리와 폼 필드 분리
```

---

## 마무리

이 프로젝트는 **경량이지만 견고한 웹 서버의 설계 원칙**을 보여줍니다.

**핵심 교훈**:
1. **패턴이 코드를 단순하게 만든다**
2. **관심사 분리로 유지보수성 극대화**
3. **확장성 있는 설계는 비용 절감**
4. **DI와 팩토리로 결합도 최소화**

이러한 설계 원칙은 **Spring, Django, Express.js 등 실무 프레임워크**에서도 동일하게 적용되고 있습니다.

