# GMC Backend — Claude 작업 가이드

## 프로젝트 개요

- **Spring Boot 4.0.6 / Java 17**
- 인증: Google OAuth2 + JWT (JJWT 0.12.6)
- DB: H2 In-Memory (개발), JPA/Hibernate
- 파일 업로드: AWS S3 (AWS SDK 2.25.60)
- API 문서: SpringDoc OpenAPI (Swagger UI)
- 환경 변수: `.env` 파일 → `DotenvEnvironmentPostProcessor`로 주입

---

## 패키지 구조

```
com.gmc.backend
├── domain/                  # 엔티티 + Repository (순수 도메인)
│   ├── coupon/
│   ├── member/
│   └── membercoupon/
├── {feature}/               # 기능 단위 모듈 (예: coupon, membercoupon, member)
│   ├── application/         # Service + dto/
│   └── presentation/        # Controller
├── auth/
│   ├── jwt/                 # JwtTokenProvider, JwtAuthenticationFilter
│   └── oauth2/              # CustomOAuth2UserService, OAuth2SuccessHandler
├── config/                  # SecurityConfig, SwaggerConfig, DotenvEnvironmentPostProcessor
├── infra/s3/                # S3Config, S3Uploader
└── common/
    ├── response/            # ApiResponse
    └── exception/           # ErrorCode, CustomException, GlobalExceptionHandler
```

새 기능 추가 순서: `domain/` 에 엔티티+Repository → `{feature}/application/` 에 Service+DTO → `{feature}/presentation/` 에 Controller

---

## 응답 형식

모든 API 응답은 `ApiResponse<T>`로 감싼다.

```java
// 성공
ResponseEntity.ok(ApiResponse.success(data));
ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));

// 실패는 GlobalExceptionHandler가 자동 처리
```

```json
// 성공
{ "status": "success", "data": { ... } }

// 실패 (null 필드는 JSON에서 제외됨)
{ "status": "40401", "message": "존재하지 않는 회원입니다." }
```

---

## 예외 처리

`ErrorCode` enum에 에러를 정의하고, `CustomException`으로 던진다.

```java
// ErrorCode 추가 예시
MEMBER_NOT_FOUND("40401", "존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),

// 사용
throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
```

`GlobalExceptionHandler`가 `CustomException`, 파라미터 누락, 기타 예외를 일괄 처리한다. 컨트롤러에서 try-catch 하지 않는다.

---

## 엔티티 컨벤션

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // Builder 강제
@Builder
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계는 FetchType.LAZY 기본
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
}
```

- 객체 생성은 항상 `Builder` 패턴
- 엔티티 직접 `new` 금지

---

## DTO 컨벤션

Java `record` 사용, 엔티티→DTO 변환은 `from()` static 메서드로 제공.

```java
public record CouponResponse(Long id, String name, LocalDate expiresAt) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(coupon.getId(), coupon.getName(), coupon.getExpiresAt());
    }
}
```

---

## Service 컨벤션

```java
@Service
@RequiredArgsConstructor
public class CouponService {

    @Transactional(readOnly = true)   // 조회는 readOnly
    public List<CouponResponse> getCoupons() { ... }

    @Transactional                    // 쓰기는 기본 트랜잭션
    public CouponResponse createCoupon(...) { ... }
}
```

---

## Controller 컨벤션

- URL: `/api/{resource}` (복수형, 소문자 하이픈)
- 인증이 필요한 엔드포인트는 `@AuthenticationPrincipal String email` 파라미터 추가
- multipart 요청은 `consumes = MediaType.MULTIPART_FORM_DATA_VALUE` 명시

```java
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getCoupons(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCoupons()));
    }
}
```

---

## 인증 구조

- JWT subject에 **이메일** 저장
- 요청 헤더: `Authorization: Bearer {token}`
- 필터에서 파싱 후 `SecurityContext`에 이메일을 principal로 저장
- 컨트롤러에서 `@AuthenticationPrincipal String email`로 주입받음

---

## Security 허용 경로

새 공개 엔드포인트 추가 시 `SecurityConfig`의 `permitAll()`과 `JwtAuthenticationFilter.shouldNotFilter()`를 **둘 다** 수정해야 한다.

현재 허용 경로: `/h2-console/**`, `/api/oauth/**`, `/login/oauth2/**`, `/swagger-ui/**`, `/v3/api-docs/**`

---

## S3 파일 업로드

`S3Uploader.upload(MultipartFile)`을 호출하면 `coupons/{UUID}-{원본파일명}` 형태의 key를 반환한다. DB에는 full URL이 아닌 **key(경로)** 를 저장한다.

테스트에서는 `@MockitoBean S3Uploader s3Uploader`로 모킹한다.

---

## 테스트 컨벤션

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional           // 테스트 후 롤백으로 데이터 격리
class CouponControllerTest {

    @MockitoBean S3Uploader s3Uploader;   // 외부 의존성은 항상 Mock

    @BeforeEach
    void setUp() {
        // 테스트용 Member 저장 + JWT 토큰 발급
        given(s3Uploader.upload(any())).willReturn("coupons/test.png");
    }

    @Test
    @DisplayName("쿠폰 목록 조회 - 빈 목록 반환")   // 한글 DisplayName
    void getCoupons_emptyList() throws Exception {
        mockMvc.perform(get("/api/coupons")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
}
```

- 인증 필요 엔드포인트: `@BeforeEach`에서 Member 저장 + `jwtTokenProvider.generateToken(email)`로 토큰 준비
- 인증 없는 요청 테스트도 반드시 포함 (401 검증)

---

## 환경 변수 (.env)

```
JWT_SECRET=...
JWT_EXPIRATION=3600000
CLIENT_ID=...
CLIENT_PW=...
OAUTH2_REDIRECT_URI=...
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=...
AWS_ACCESS_KEY=...
AWS_SECRET_KEY=...
```

`.env`는 `.gitignore`에 포함되어야 하며, `.env.example`을 함께 관리한다.