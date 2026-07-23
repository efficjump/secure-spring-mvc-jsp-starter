# 운영 가이드

## 프로파일

- 기본 설정은 안전한 공통값을 가집니다.
- `local`은 HTTP 개발 환경을 위해 Secure 쿠키를 끄고 SQL 문장 로그를 켭니다.
- `prod`는 DB 접속 정보를 필수로 요구하고 Secure 쿠키, 회원가입/부트스트랩/SQL 로그를 닫습니다.
- `test`는 H2의 MariaDB 호환 모드를 사용하며 Flyway 대신 테스트별 스키마를 생성합니다.

운영 실행은 최소한 다음 값을 비밀 관리 시스템에서 주입해야 합니다.

```sh
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mariadb://db.example.internal:3306/webapp?sslMode=verify-full
DB_USERNAME=<runtime-account>
DB_PASSWORD=<secret-manager-reference>
SESSION_COOKIE_SECURE=true
APP_I18N_COOKIE_SECURE=true
APP_BOOTSTRAP_ADMIN_ENABLED=false
APP_REGISTRATION_ENABLED=false
```

위 예시의 호스트와 비밀값은 실제 환경 값으로 바꿔야 합니다. DB TLS 인증서 신뢰 경로와 `sslMode` 지원 여부는 사용하는 Connector/J 버전 및 인증서 정책으로 배포 전에 검증하세요.

## 설정 카탈로그

### 데이터베이스와 풀

| 변수 | 기본값 | 판단 기준 |
|---|---:|---|
| `DB_URL` | 로컬 MariaDB URL | 운영에서는 TLS 검증과 내부 DNS 사용 |
| `DB_POOL_MAX_SIZE` | `10` | 인스턴스 수 × 풀 크기가 DB 한도를 넘지 않게 계산 |
| `DB_POOL_MIN_IDLE` | `2` | 저트래픽은 작게, 지연 민감 서비스는 부하 시험 후 조정 |
| `DB_CONNECTION_TIMEOUT_MS` | `10000` | 장애 시 요청 예산보다 짧게 유지 |
| `DB_MAX_LIFETIME_MS` | `1500000` | DB·프록시 연결 수명보다 짧게 유지 |
| `DB_LEAK_DETECTION_THRESHOLD` | `0` | 조사 기간에만 제한적으로 활성화 |

운영 권한은 두 계정으로 나누는 것이 좋습니다.

- 마이그레이션 계정: DDL과 Flyway 이력 테이블 변경 권한
- 런타임 계정: 애플리케이션 테이블의 필요한 DML 권한만 보유

현재 시작점은 간단한 로컬 실행을 위해 한 계정으로 Flyway와 런타임을 함께 수행합니다. 운영 파이프라인에서는 Flyway를 별도 단계로 실행하고 애플리케이션 시작 계정에서 DDL 권한을 제거하세요.

### 보안과 세션

| 변수 | 기본값 | 운영 메모 |
|---|---:|---|
| `APP_PASSWORD_ALGORITHM` | `argon2` | 변경 시 기존 해시는 계속 읽고 성공 로그인 시 갱신 |
| `APP_ARGON2_MEMORY_KIB` | `19456` | 배포 CPU·메모리에서 로그인 부하 시험 필요 |
| `APP_LOGIN_ATTEMPTS_PER_WINDOW` | `8` | 계정별 제한 |
| `APP_LOGIN_IP_ATTEMPTS_PER_WINDOW` | `40` | NAT 사용자가 많은 환경은 오탐 측정 필요 |
| `APP_ACCOUNT_LOCK_THRESHOLD` | `5` | DB에 유지되는 계정 잠금 기준 |
| `APP_MAX_CONCURRENT_SESSIONS` | `2` | 업무 성격과 공유 계정 금지 정책에 맞춤 |
| `APP_SESSION_METADATA_MAX_ENTRIES` | `10000` | 단일 노드 세션 부가정보 메모리 상한 |
| `SESSION_TIMEOUT` | `30m` | 민감 서비스는 절대/유휴 만료 정책을 별도 설계 |
| `SESSION_COOKIE_SAME_SITE` | `lax` | 외부 인증 POST 연동 시 흐름 검증 후 조정 |

보안 기간 값과 Argon2 비용을 올릴 때는 추측으로 정하지 말고 대표 하드웨어에서 p95/p99 로그인 지연과 동시 요청 메모리를 측정하세요.

### 다국어와 로케일 쿠키

| 변수 | 기본값 | 운영 메모 |
|---|---:|---|
| `APP_I18N_DEFAULT_LOCALE` | `ko` | 허용 목록에 반드시 포함되는 BCP 47 언어 태그 |
| `APP_I18N_SUPPORTED_LOCALES` | `ko,en` | 번들이 준비된 언어만 쉼표로 구분해 등록 |
| `APP_I18N_RESPECT_ACCEPT_LANGUAGE` | `true` | 선택 쿠키가 없는 첫 요청에만 브라우저 선호 언어 반영 |
| `APP_I18N_COOKIE_NAME` | `APP_LOCALE` | 같은 호스트의 다른 앱 쿠키와 충돌하지 않는 이름 사용 |
| `APP_I18N_COOKIE_PATH` | `/` | 별도 컨텍스트 경로 배포 시 해당 경로로 범위 축소 |
| `APP_I18N_COOKIE_MAX_AGE` | `365d` | 개인정보·사용자 설정 보존 정책에 맞춰 조정 |
| `APP_I18N_COOKIE_SAME_SITE` | `Lax` | `Lax` 또는 더 엄격한 `Strict`만 허용 |
| `APP_I18N_COOKIE_SECURE` | `true` | 운영 HTTPS에서는 반드시 `true`, 로컬 HTTP에서만 `false` |

언어 쿠키는 자바스크립트에서 읽을 필요가 없으므로 `HttpOnly`이며, 교차 사이트 요청에 불필요하게 실리지 않도록 `SameSite=Lax`를 사용합니다. 언어 변경 엔드포인트는 설정된 허용 목록에 있는 정확한 언어 태그만 저장하고, 직접 변조된 쿠키 값도 해석 단계에서 허용 목록으로 다시 제한합니다. 화면 복귀 주소도 같은 애플리케이션의 정규화된 절대 경로만 허용하므로, 프록시나 애플리케이션이 이 검증을 우회해 외부 URL을 주입하지 않게 유지하세요.

새 언어를 운영에 추가할 때는 번들 파일, 기본 메뉴 이름·그룹, 오류·검증 메시지, 이메일이나 내보내기처럼 별도 출력 채널을 함께 검토합니다. 현재 자동 테스트는 한국어와 영어 번들의 키 집합을 비교합니다. 세 번째 언어를 추가하면 해당 파일도 같은 일관성 검사 대상에 포함하세요.

### 업무 셸과 동일 출처 프레임

| 변수 | 기본값 | 운영 메모 |
|---|---:|---|
| `APP_FRAME_OPTIONS` | `SAMEORIGIN` | 외부 프레이밍은 차단하고 내부 업무 탭만 허용 |
| `APP_WORKSPACE_MAX_TABS` | `12` | 브라우저·업무 화면 메모리 사용량을 측정해 조정 |
| `APP_WORKSPACE_DEFAULT_MENU_KEY` | `dashboard` | 활성화되고 사용자 권한에 포함된 메뉴 키 사용 |
| `APP_WORKSPACE_STORAGE_KEY` | `secure-mvc-workspace` | 같은 출처의 다른 앱과 충돌하지 않는 키 사용 |
| `APP_WORKSPACE_RESERVED_PATHS` | 인증·셸·내부 경로 | 메뉴로 열 수 없는 접두사 목록 |

업무 셸은 `frame-ancestors 'self'`와 `SAMEORIGIN`을 함께 사용합니다. `APP_FRAME_OPTIONS=DENY`로 바꾸거나 CSP의 `frame-ancestors`를 `none`으로 바꾸면 보안은 더 엄격해지지만 내부 탭도 렌더링되지 않습니다. 반대로 다른 출처를 허용하지 마세요. 외부 시스템 연계는 iframe 예외보다 OIDC와 서버 간 API 등 명시적인 통합 경계를 우선합니다.

메뉴 관리자는 로컬 절대 경로만 등록할 수 있습니다. 예약 목록을 줄일 때는 `/workspace` 재귀 프레임, 로그인·로그아웃·언어 변경 경로, Actuator와 정적 자산이 메뉴로 열리지 않는지 먼저 확인해야 합니다.

### 프록시와 TLS

기본 `SERVER_FORWARD_HEADERS_STRATEGY=NONE`은 클라이언트가 위조한 `Forwarded`/`X-Forwarded-*`를 신뢰하지 않습니다. 신뢰할 수 있는 프록시 뒤에서만 프레임워크 또는 컨테이너 전략으로 바꾸고, 프록시가 외부 전달 헤더를 제거한 뒤 자체 값으로 다시 쓰도록 설정하세요. 그렇지 않으면 HTTPS 판정, 리다이렉트, 감사 IP, 속도 제한 키가 조작될 수 있습니다.

HSTS는 HTTPS로 인식된 요청에만 전송됩니다. TLS 종료 프록시를 사용할 때 전달 헤더 구성이 잘못되면 HSTS가 빠질 수 있으므로 실제 공개 URL에서 확인해야 합니다. `preload`는 하위 도메인 전체에 장기간 영향을 주므로 조직의 모든 서브도메인이 HTTPS일 때만 켭니다.

### HTTP 입력 제한

`SERVER_MAX_FORM_POST_SIZE`는 일반 폼 본문의 상한이며 기본값은 64KB입니다. `SERVER_MAX_PARAMETER_COUNT`, `SERVER_MAX_PART_COUNT`, `SERVER_MAX_HEADER_SIZE`도 환경변수로 조정할 수 있습니다. 파일 업로드를 추가할 때 이 값을 무작정 키우지 말고 업로드 전용 경로, 파일 크기·개수, 실제 MIME 검사, 저장소 정책을 별도로 설계하세요. 로그인 자격 증명은 폼 상한과 별개로 사용자명 및 `APP_PASSWORD_MAX_LENGTH`를 인증 필터에서 먼저 검사해 과도한 해시 연산을 막습니다.

## 로그 운영

파일 기본 순환은 파일당 20MB, 30일, 로그 종류별 총 2GB입니다. `APP_LOG_MAX_FILE_SIZE`, `APP_LOG_MAX_HISTORY`, `APP_LOG_TOTAL_SIZE_CAP`으로 조정할 수 있습니다. 컨테이너에서는 `/app/logs` 영구 볼륨을 사용합니다.

민감정보 원칙은 다음과 같습니다.

- 비밀번호, CSRF 토큰, 세션 쿠키, Authorization 헤더, 요청 본문은 기록하지 않습니다.
- SQL 바인드 로깅은 개발에서도 필요한 짧은 조사 시간에만 켜고 결과 파일을 민감정보로 취급합니다.
- 로그 수집기에서 `security-audit.log` 접근 권한과 보존 기간을 별도로 관리합니다.
- IP, 사용자명, 이메일은 개인정보가 될 수 있으므로 감사 목적과 파기 주기를 정책으로 정합니다.

MariaDB 슬로 로그를 `TABLE`로 오래 유지하면 `mysql.slow_log`가 커질 수 있습니다. 주기적으로 보관·삭제하고 대규모 운영에서는 FILE 출력과 에이전트 수집을 검토하세요.

## 상태 확인과 관찰

```text
GET /internal/actuator/health
GET /internal/actuator/health/liveness
GET /internal/actuator/health/readiness
GET /internal/actuator/info
```

health와 info만 익명 접근을 허용합니다. details는 항상 숨깁니다. metrics 등 추가 노출 엔드포인트는 관리자 인증이 필요하지만, 인터넷에 직접 노출하지 말고 관리 네트워크나 별도 포트로 격리하는 편이 안전합니다.

요청 ID는 응답 `X-Request-ID`, 접근 로그, 감사 로그에 함께 남습니다. 장애 조사 시 사용자에게 받은 요청 ID로 세 로그를 연계할 수 있습니다.

## 배포 순서

1. 전체 테스트와 WAR 패키징을 수행합니다.
2. 의존성·컨테이너 이미지 취약점 스캔과 SBOM 생성을 CI에 연결합니다.
3. DB 백업을 확인하고 Flyway 변경을 스테이징 복제본에서 실행합니다.
4. 별도 마이그레이션 계정으로 변경을 적용합니다.
5. 최소 권한 런타임 계정과 `prod` 프로파일로 새 버전을 배포합니다.
6. readiness 성공 후 트래픽을 전환합니다.
7. 로그인·권한·CSRF·보안 헤더·SQL 오류율·슬로 쿼리를 확인합니다.
8. 문제 발생 시 애플리케이션은 이전 이미지로 되돌리되 DB는 역방향 SQL을 즉흥 실행하지 않습니다. 앞으로 호환되는 후속 마이그레이션으로 복구합니다.

## 백업과 복구

DB 백업은 생성 성공이 아니라 복구 성공으로 검증해야 합니다. 정기적으로 격리 환경에 복원한 뒤 Flyway 이력, 사용자·역할 수, 감사 이벤트 기간, 애플리케이션 기동을 확인하세요. 목표 복구 시점(RPO)과 복구 시간(RTO), 암호화 키·인증서·비밀 관리 시스템 복구 절차도 함께 시험해야 합니다.

## 로컬 스택 초기화 주의

`make down`은 데이터를 보존합니다. 정말로 생성한 로컬 데이터까지 지울 때만 다음 명령을 실행하세요.

```sh
docker compose --env-file .env down --volumes
```

이 명령은 프로젝트의 MariaDB 데이터와 컨테이너 로그 볼륨을 복구하기 어렵게 삭제합니다. 운영 환경에서는 사용하지 않습니다.
