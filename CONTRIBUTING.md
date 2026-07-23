# 기여 가이드

이 저장소는 반복해서 사용할 수 있는 보안 중심 업무 시스템 기준을 지향합니다. 변경은 특정 조직이나 실행 환경에 묶이지 않아야 하며, 안전한 기본값을 약화시키는 경우에는 위협 모델과 대안을 함께 설명해야 합니다.

## 개발 환경

- Java 21 또는 Docker와 Docker Compose v2
- Maven Wrapper는 저장소에 포함되어 있습니다.

```sh
git config core.hooksPath .githooks
make public-check
make test
```

로컬 비밀값은 `.env.example`을 참고해 `.env`에만 보관합니다. `.env`는 커밋 대상이 아닙니다.

## 변경 기준

- 입력 검증, 인가, 감사 이벤트와 트랜잭션 경계를 함께 검토합니다.
- 스키마 변경은 기존 Flyway 파일을 수정하지 않고 새 버전 파일로 추가합니다.
- 운영 설정은 환경변수로 덮어쓸 수 있게 유지합니다.
- 새 의존성은 유지보수 상태와 라이선스를 확인하고 `THIRD-PARTY-NOTICES.md`를 갱신합니다.
- 관리 화면은 키보드 탐색, 명확한 포커스, 충분한 대비와 전체 화면 업무 흐름을 유지합니다.
- 화면 문구를 추가하면 지원하는 모든 메시지 번들을 함께 갱신하고 번들 일관성 테스트를 실행합니다.
- 공개 안전성 세부 기준은 [AGENTS.md](AGENTS.md)를 따릅니다.

## 제출 전 확인

```sh
./scripts/public-release-check.sh staged
./mvnw --batch-mode --no-transfer-progress clean package
```

테스트를 실행할 수 없었다면 이유와 미검증 범위를 변경 설명에 남겨 주세요. 기여한 코드는 별도 합의가 없는 한 저장소의 Apache License 2.0 조건으로 제공됩니다.
