[English](README.md) | **한국어** | [简体中文](README.zh.md) | [日本語](README.ja.md)

# Secure Spring MVC JSP Starter

Spring MVC, JSP, MariaDB 조합을 새 프로젝트에서 반복해서 사용할 수 있도록 만든 보안 중심 시작점입니다. 단순한 로그인 예제를 넘어 계정 잠금, 세션 폐기, 관리자 안전장치, 보안 감사, SQL·슬로 쿼리 관찰, 운영 프로파일과 컨테이너 제한, DB 기반 다국어 관리, 다중 탭 업무 셸까지 하나의 기준으로 묶었습니다.

현재 기준 버전은 Java 21, Spring Boot 4.1.0, MariaDB 12.3.2 LTS이며 애플리케이션은 실행 가능한 WAR로 패키징합니다. Spring Boot 공식 문서상 JSP는 실행 가능한 JAR에서 지원되지 않기 때문에 WAR 구조를 유지해야 합니다.

![Spring MVC Starter 영문 메인 화면](docs/images/home.png)

새 사내 시스템, 백오피스, 운영 포털처럼 인증·권한·감사·관리 화면을 매번 다시 설계해야 하는 프로젝트에 적합합니다. 화면 예제만 제공하는 템플릿이 아니라 계정과 세션의 불변 조건, 운영 로그, DB 마이그레이션, 컨테이너 실행과 검증 기준까지 함께 가져가는 것을 목표로 합니다.

## 한눈에 보기

| 구분 | 기본 구성 |
|---|---|
| 서버 | Java 21, Spring Boot, Spring MVC, JSP/JSTL, 실행 가능한 WAR |
| 데이터 | MariaDB, Spring Data JPA, Flyway, HikariCP |
| 보안 | Spring Security, Argon2, CSRF, CSP, 세션 제한, 계정 잠금, 감사 로그 |
| 관리 | 사용자 계정, 로그인 이력, 활성 세션, 동적 메뉴, 다국어, 비밀번호 변경 |
| 업무 UI | 전체 화면 셸, 권한 기반 탐색, 본문 연결형 다중 탭, 반응형 레이아웃 |
| 다국어 | 영어 기본값, 한국어·영어·중국어·일본어 전체 번역, 언어·문구 런타임 관리 |
| 운영 | Docker Compose, health probe, 구조화된 로그 분리, SQL·슬로 쿼리 관찰 |
| 공급망 | 제3자 라이선스 보고서, CycloneDX SBOM, 의존성 자동 업데이트 |

## 포함된 기준

### 전체 화면 업무 셸

- 로그인 후 전체 화면을 사용하는 데스크톱형 업무 레이아웃
- DB 메뉴를 권한·그룹·순서대로 구성하는 왼쪽 탐색 영역
- 여러 JSP 업무 화면을 동시에 유지하는 동일 출처 iframe 탭
- 열린 탭과 활성 탭을 사용자별 `sessionStorage`에 보관하고 새로고침 시 복원
- 메뉴 검색, 사이드바 접기, 현재 탭 새로고침, 기본 탭 정리와 반응형 모바일 메뉴
- 카드 나열 대신 툴바, 지표 스트립, 데이터 그리드와 편집 패널 중심의 관리 UI
- 의미 기반 CSS 토큰과 별도 `theme-modern.css` 레이어로 색상·밀도·반경·고도 정책 분리
- 밝은 중립색 탐색 영역과 간결한 둥근 작업 탭
- 하단 강조선이나 색 띠 없이 배경·텍스트·굵기와 접근성 상태로 구분하는 활성 탭
- 사용자 계정, 로그인 이력, 활성 세션, 메뉴, 다국어, 비밀번호 변경 관리 화면

업무 화면은 URL을 직접 연결하는 메뉴 정의를 DB에서 읽어 오며, 서버가 허용한 내부 경로만 탭으로 엽니다. 탭별 상태는 브라우저 세션 범위에 남아 있어 목록과 편집 화면을 오가더라도 작업 맥락을 유지할 수 있습니다.

![하단 강조선 없이 정리한 다중 탭 업무 셸](docs/images/workspace.png)

### 다국어와 언어 전환

- 영어를 기본값으로 사용하고 홈·로그인·가입·오류 화면, 업무 셸과 모든 관리 화면을 한국어·영어·중국어·일본어로 제공
- 관리 화면에서 BCP 47 언어를 등록·수정·정렬·활성화하고 기본 언어를 재배포 없이 변경
- 번들 메시지 키를 검색해 MariaDB 번역으로 덮어쓰며 저장 다음 요청부터 즉시 적용
- 첫 방문에는 DB 활성 언어 안에서 브라우저 `Accept-Language`를 적용하고 일치 항목이 없으면 DB 기본 언어 사용
- 헤더, 로그인 패널과 업무 셸 상단의 언어 선택기로 현재 화면에서 즉시 전환
- 선택 언어를 `HttpOnly`, `SameSite=Lax` 쿠키에 보관하며 운영 HTTPS에서는 `Secure` 적용
- 언어 변경 후에도 로그인 이력 검색 조건과 페이지 같은 안전한 내부 쿼리 문자열 유지
- 외부 URL, 경로 이동, 프로토콜 상대 URL을 반환 경로로 사용할 수 없도록 리다이렉트 검증
- 일반 텍스트·`MessageFormat` 검증, 제한된 캐시와 관리 트랜잭션 커밋 후 즉시 캐시 무효화
- 기본 DB 메뉴는 `navigation.menu.<menu-key>.label/group` 규칙으로 번역하고 사용자 정의 메뉴는 DB 원문으로 안전하게 대체
- 환경변수는 빈 카탈로그 대체값과 쿠키 보안·브라우저 언어 반영·캐시 상한을 설정

새 언어는 **다국어 관리** 화면에서 BCP 47 태그와 번역 문구를 등록하면 바로 사용할 수 있습니다. 완전히 검토된 기본 번들을 함께 배포하려면 `messages_<언어태그>.properties`를 추가합니다. `MessageBundleConsistencyTest`는 기본 네 언어의 모든 키와 동적 열거형 메시지가 빠지지 않았는지 확인합니다.

| English | 한국어 | 简体中文 | 日本語 |
|---|---|---|---|
| ![영문 로그인](docs/images/login-en.png) | ![한국어 로그인](docs/images/login.png) | ![중국어 로그인](docs/images/login-zh.png) | ![일본어 로그인](docs/images/login-ja.png) |

| 언어 카탈로그 | 번역 편집 |
|---|---|
| ![동적 언어 카탈로그](docs/images/localization.png) | ![DB 번역 편집 화면](docs/images/translations.png) |

### 기본 관리 화면

| 화면 | 주요 기능 | 접근 권한 |
|---|---|---|
| 업무 현황 | 계정·로그인·세션 지표와 최근 보안 이벤트 확인 | 사용자 |
| 사용자 계정 | 계정 조회, 역할·상태 변경, 잠금 해제 | 관리자 |
| 로그인 이력 | 성공·실패 결과, 요청 ID, 시각과 원격 주소 조회 | 관리자 |
| 세션 관리 | 활성 세션 조회와 선택 세션 강제 종료 | 관리자 |
| 메뉴 관리 | 메뉴 그룹, 순서, 내부 경로, 역할과 표시 상태 편집 | 관리자 |
| 다국어 관리 | 언어 등록, 기본 언어 선택, DB 번역 문구 편집 | 관리자 |
| 비밀번호 변경 | 현재 비밀번호 확인, 정책 검증, 변경 후 전체 세션 만료 | 사용자 |

### 인증과 계정

- 사용자명 정규화와 중복 방지, 선택적으로 열 수 있는 회원가입
- Argon2id 계열을 기본값으로 사용하는 `DelegatingPasswordEncoder`
- bcrypt, PBKDF2, scrypt로 저장된 기존 해시 판별과 로그인 시 자동 재해싱
- 길이·제어문자·사용자명/이메일 포함 여부를 검사하는 비밀번호 정책
- 현재 비밀번호 확인, 같은 비밀번호 재사용 방지, 변경 후 모든 세션 만료
- 사용자명과 IP 기준 로그인 속도 제한, DB에 유지되는 계정 잠금
- 오류 원인을 외부에 구분해 주지 않는 로그인 실패 메시지
- 설정 길이를 넘는 자격 증명을 해시 연산 전에 거부하고 HTTP 폼 전체 크기 제한
- 최초 관리자 1회 생성, 자기 계정 비활성화/자기 관리자 권한 제거 방지
- 마지막 관리자 및 마지막 활성 관리자 보호, 권한·상태 변경 시 기존 세션 폐기

### 웹 보안

- Spring Security 기본 CSRF 보호와 모든 변경 요청의 POST 처리
- 로그인 시 세션 ID 교체, 동시 세션 수 제한, 로그아웃 시 세션·쿠키 제거
- `HttpOnly`, `Secure`, `SameSite` 세션 쿠키 설정
- CSP, Permissions-Policy, Referrer-Policy, HSTS, 동일 출처만 허용하는 프레임·MIME 스니핑 방지 헤더
- 인증 페이지의 캐시 금지와 안전한 오류 화면
- JSP 출력값의 기본 이스케이프, 직접 접근할 수 없는 `/WEB-INF/views`
- 요청 ID 발급, 로그 위조를 막는 개행 제거, 상세 예외를 숨기는 오류 응답

### 데이터와 운영

- Flyway 스키마 이력, Hibernate `validate`, Open Session in View 비활성화
- HikariCP 제한 시간·수명·풀 크기 환경변수화
- 애플리케이션·보안 감사·SQL 로그 분리와 크기/기간 기반 순환
- SQL 바인드 값 로깅 기본 비활성화, MariaDB 슬로 쿼리 테이블 활성화
- 보안 이벤트를 DB와 별도 감사 로그에 함께 기록
- Flyway V2 기반 동적 메뉴와 관리자 메뉴 편집 감사 이벤트
- Flyway V3 기반 런타임 언어·메시지 카탈로그, 변경 감사와 제한된 캐시
- Actuator health/info 공개, 나머지 관리 엔드포인트 관리자 제한
- 정상 종료, liveness/readiness probe, 비루트·읽기 전용 컨테이너
- 브라우저 언어 감지, 보호된 선택 쿠키, 네 언어 전체 번들과 DB 문구 오버라이드

## 빠른 시작

Docker만 있으면 로컬 Java나 Maven 설치 없이 실행할 수 있습니다.

### 준비 사항

- Docker Engine 또는 Docker Desktop과 Compose v2
- `make` 명령. 없는 환경에서는 Makefile에 적힌 Docker Compose 명령을 직접 실행할 수 있습니다.
- 기본 포트 `8080`, `3306`을 사용할 수 있는 로컬 환경

복제한 저장소의 루트에서 환경 파일을 먼저 생성합니다.

```sh
make init
```

생성된 `.env`에서 포트와 초기 관리자 설정을 검토한 뒤 서비스를 시작합니다. 이 파일에는 비밀값이 있으므로 저장소에 추가하거나 공유하지 마세요.

```sh
make up
```

컨테이너가 준비되면 다음 순서로 확인합니다.

1. `.env`의 `APP_BOOTSTRAP_ADMIN_USERNAME`과 `APP_BOOTSTRAP_ADMIN_PASSWORD`를 사용해 `http://127.0.0.1:8080/login`에 로그인합니다.
2. 로그인 패널 오른쪽 위에서 한국어·영어·중국어·일본어가 전환되고, 새로고침 뒤에도 선택이 유지되는지 확인합니다.
3. 임시 비밀번호를 즉시 변경합니다. 변경하면 기존 로그인 세션이 모두 만료됩니다.
4. 다음 기동 전 `.env`의 `APP_BOOTSTRAP_ADMIN_ENABLED=false`로 바꿔 초기 계정 생성을 닫습니다.
5. 자동 점검을 실행합니다.

```sh
make smoke
```

`make init`은 `.env`가 없을 때만 안전한 임의 DB 비밀번호와 초기 관리자 비밀번호를 만들고 파일 권한을 `600`으로 설정합니다. 로그인에 성공하면 `/workspace` 업무 셸로 이동합니다.

포트가 사용 중이면 `.env`의 `APP_PORT`, `DB_PORT`만 바꾸면 됩니다. 바인드 주소 기본값은 외부에 노출되지 않는 `127.0.0.1`입니다.

```sh
make logs       # 앱과 DB 로그 추적
make test       # 전체 자동화 테스트
make package    # 테스트 후 실행 가능한 WAR 생성
make public-check # 공개 저장소 안전성 검사
make down       # 컨테이너 중지, DB 볼륨 보존
```

로컬에 Java 21이 있다면 `./mvnw test`, `./mvnw clean package`도 사용할 수 있습니다. 산출물은 `target/secure-mvc-starter.war`입니다.

### 정상 동작 확인

| 주소 | 기대 결과 |
|---|---|
| `http://127.0.0.1:8080/login` | 로그인 화면 |
| `http://127.0.0.1:8080/internal/actuator/health` | 애플리케이션 상태 |
| `http://127.0.0.1:8080/internal/actuator/health/liveness` | 프로세스 생존 상태 |
| `http://127.0.0.1:8080/internal/actuator/health/readiness` | 요청 수신 준비 상태 |

문제가 생기면 `make logs`로 애플리케이션과 DB 로그를 함께 확인합니다. 처음부터 다시 만들 필요가 있을 때도 `make down`은 DB 볼륨을 보존하므로, 볼륨 삭제가 필요한지 먼저 판단하세요.

## 프로젝트를 복제한 뒤 바꿀 곳

1. `pom.xml`의 `groupId`, `artifactId`, `name`, `finalName`을 서비스 명에 맞춥니다.
2. `com.example.webstarter` 패키지를 조직의 역도메인 패키지로 일괄 변경합니다.
3. `messages*.properties`의 제품 문구와 기본 메뉴 번역을 바꾸고 `theme-modern.css`의 의미 기반 디자인 토큰을 서비스 브랜드에 맞춥니다. 구조와 동작 기준은 `app.css`에 유지합니다.
4. 기존 `V1`~`V3` Flyway 마이그레이션은 수정하지 않고 업무 테이블은 새 버전으로 추가합니다.
5. 업무 권한이 단순 `USER`/`ADMIN`을 넘는다면 역할을 기능 권한으로 세분화하고 서비스 메서드의 `@PreAuthorize`를 함께 수정합니다.
6. 배포 전에 [보안 체크리스트](docs/SECURITY-CHECKLIST.md)와 [운영 가이드](docs/OPERATIONS.md)를 완료합니다.

이미 적용된 Flyway 파일을 수정하면 체크섬이 달라집니다. 운영 DB에서는 기존 마이그레이션을 고치지 말고 다음 버전의 변경 파일을 추가해야 합니다.

## 구조

```text
src/main/java/com/example/webstarter
├── config       환경 설정과 Spring Security 체인
├── security     인증, 비밀번호, 로그인 제한, 세션 폐기
├── user         사용자 도메인과 계정 서비스
├── admin        관리자 기능과 불변 조건
├── audit        감사 이벤트 DB·파일 기록
├── bootstrap    최초 관리자 생성
├── navigation   DB 메뉴, 내부 경로 검증, 권한별 메뉴와 번역 구성
├── localization 런타임 언어 카탈로그, 번역 오버라이드, 검증과 캐시
└── web          MVC·언어 컨트롤러, 요청 ID·헤더·접근 로그
```

상세한 요청 흐름과 확장 경계는 [아키텍처 문서](docs/ARCHITECTURE.md)에 정리했습니다.

## 설정 원칙

코드에 환경별 값을 넣지 않고 `application.yml`의 모든 운용 값을 환경변수로 덮어쓸 수 있게 했습니다. 주요 묶음은 다음과 같습니다.

| 영역 | 대표 환경변수 | 기본 방향 |
|---|---|---|
| DB | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_POOL_MAX_SIZE` | 비밀값 외부 주입, 제한된 풀 |
| 세션 | `SESSION_COOKIE_SECURE`, `SESSION_COOKIE_SAME_SITE`, `SESSION_TIMEOUT`, `APP_SESSION_METADATA_MAX_ENTRIES` | 운영 HTTPS, 30분, Lax, 제한된 메타데이터 |
| 다국어 | `APP_I18N_DEFAULT_LOCALE`, `APP_I18N_SUPPORTED_LOCALES`, `APP_I18N_COOKIE_*`, `APP_I18N_CACHE_*` | DB 카탈로그, 영어 대체값, 운영 Secure 쿠키와 제한된 캐시 |
| 암호 | `APP_PASSWORD_ALGORITHM`, `APP_ARGON2_*` | Argon2, 설정 검증 |
| 로그인 보호 | `APP_LOGIN_*`, `APP_ACCOUNT_LOCK_*` | IP·식별자 제한과 계정 잠금 |
| 헤더 | `APP_CONTENT_SECURITY_POLICY`, `APP_FRAME_OPTIONS`, `APP_PERMISSIONS_POLICY`, `APP_HSTS_*` | 외부 프레임 차단, 동일 출처 업무 탭 허용 |
| 로그 | `APP_SQL_LOG_LEVEL`, `APP_SQL_BIND_LOG_LEVEL`, `APP_LOG_*` | 운영 SQL off, 바인드 항상 off 권장 |
| 관리 | `MANAGEMENT_ENDPOINTS`, `APP_ADMIN_PAGE_SIZE` | 최소 엔드포인트와 제한된 페이지 |
| HTTP 입력 | `SERVER_MAX_FORM_POST_SIZE`, `SERVER_MAX_PARAMETER_COUNT` | 과도한 폼과 파라미터 조기 거부 |
| 업무 셸 | `APP_WORKSPACE_MAX_TABS`, `APP_WORKSPACE_DEFAULT_MENU_KEY`, `APP_WORKSPACE_RESERVED_PATHS` | 탭 상한, 시작 메뉴, 재귀·민감 경로 차단 |

전체 목록과 운영 예시는 [운영 가이드](docs/OPERATIONS.md)를 참고하세요.

## 쿼리 로그 확인

로컬 프로파일은 Hibernate SQL을 `/app/logs/sql.log`에 기록합니다. SQL 파라미터는 비밀번호·토큰·개인정보가 섞일 수 있으므로 `APP_SQL_BIND_LOG_LEVEL=OFF`가 기본이며 운영에서도 그대로 유지하는 편이 안전합니다.

```sh
docker compose --env-file .env exec app sh -c 'tail -f /app/logs/sql.log'
docker compose --env-file .env exec db mariadb -uroot -p
```

DB 콘솔에서는 다음처럼 임계 시간을 넘긴 쿼리를 봅니다.

```sql
SELECT start_time, query_time, rows_examined, sql_text
FROM mysql.slow_log
ORDER BY start_time DESC
LIMIT 50;
```

운영에서는 슬로 로그의 보관 기간과 테이블 크기를 반드시 관리하고, 트래픽이 큰 환경은 파일 출력이나 중앙 관찰 시스템으로 넘기세요.

## 의도적으로 포함하지 않은 것

이 저장소는 안전한 출발점이지 모든 서비스의 인증 제품을 대신하지 않습니다. 이메일 소유 확인, 비밀번호 재설정 메일, MFA/패스키, SSO/OIDC, CAPTCHA, 개인정보 동의·파기 정책은 사업 요구와 외부 시스템이 필요하므로 샘플 동작으로 가장하지 않았습니다. 인터넷 공개 서비스라면 회원가입을 열기 전에 최소한 이메일 확인과 계정 복구 흐름을 구현해야 합니다.

다중 인스턴스에서는 메모리 기반 로그인 제한과 `SessionRegistry`가 노드마다 나뉩니다. Redis 같은 공유 저장소 기반 제한기와 Spring Session으로 교체한 뒤 배포해야 합니다. DB 마이그레이션 권한과 런타임 권한도 운영에서는 별도 계정으로 분리하는 것이 원칙입니다.

## 라이선스와 공급망 확인

프로젝트 자체 소스는 [Apache License 2.0](LICENSE)으로 공개합니다. 제3자 라이브러리는 각 라이선스를 유지하며, 주요 범주와 재배포 시 주의사항은 [제3자 고지](THIRD-PARTY-NOTICES.md)에 정리했습니다.

`clean package`를 실행하면 빌드에 사용하는 직접·전이 의존성 라이선스를 `target/generated-sources/license/THIRD-PARTY.txt`에 생성합니다. 라이선스 메타데이터가 없는 새 의존성은 빌드를 실패시키며, 배포 구성 요소를 담은 CycloneDX SBOM은 패키지의 `META-INF/sbom`에 포함됩니다.

```sh
make license-report
make package
```

## 공개 저장소 안전장치

[저장소 작업 지침](AGENTS.md)은 공급자 중립 표현, 개인 워크스테이션 정보와 비밀값 배제, 이미지 점검과 의존성 라이선스 확인을 모든 변경의 기본 규칙으로 둡니다. 커밋과 푸시 훅을 사용하려면 복제 후 한 번 설정하세요.

```sh
git config core.hooksPath .githooks
./scripts/public-release-check.sh repository
```

검사는 `.env`, 키·인증서, 로그, 빌드 산출물, 큰 파일, 절대 사용자 경로와 고신뢰 비밀값 패턴을 차단합니다. 개인 차단 문자열은 공개 파일에 적지 않고 `.git/info/public-release-deny-patterns`에 한 줄씩 보관할 수 있습니다. 기여 절차는 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고하세요.

## 참고 기준

- [Spring Boot Servlet/JSP 문서](https://docs.spring.io/spring-boot/4.0/reference/web/servlet.html)
- [Spring Boot traditional WAR 배포](https://docs.spring.io/spring-boot/how-to/deployment/traditional-deployment.html)
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/7.0/servlet/exploits/csrf.html)
- [Spring Security 보안 헤더](https://docs.spring.io/spring-security/reference/features/exploits/headers.html)
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [MariaDB Connector/J](https://mariadb.com/docs/connectors/mariadb-connector-j/about-mariadb-connector-j)
