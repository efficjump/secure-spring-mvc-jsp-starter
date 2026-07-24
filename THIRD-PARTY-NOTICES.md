# Third-party notices

이 프로젝트의 자체 소스 코드는 [Apache License 2.0](LICENSE)으로 배포합니다. 의존 라이브러리는 각 저작권자와 개별 라이선스의 적용을 받으며, 프로젝트 라이선스가 그 조건을 변경하지 않습니다.

## 확인 방법

2026-07-24 기준으로 빌드에 사용하는 직접·전이 의존성 154개를 Maven 메타데이터로 검사했습니다. 라이선스가 식별되지 않은 항목은 없었습니다. 빌드할 때 같은 검사가 다시 실행되며, 라이선스 정보가 누락된 새 의존성이 있으면 빌드가 실패합니다.

```sh
make license-report
```

전체 좌표와 선언 라이선스는 빌드 후 `target/generated-sources/license/THIRD-PARTY.txt`에서 확인할 수 있습니다. 패키징 결과에는 이 보고서와 CycloneDX SBOM이 함께 들어갑니다.

## 주요 라이선스 범주

| 구성 요소 범주 | 확인된 라이선스 |
|---|---|
| Spring, Flyway, Hibernate ORM, Caffeine, Tomcat, Micrometer 등 | Apache License 2.0 |
| MariaDB Connector/J | LGPL-2.1-or-later |
| Logback | EPL-2.0 또는 LGPL-2.1-only |
| Jakarta API 및 구현 일부 | EPL-2.0, EDL-1.0, GPL-2.0-with-Classpath-Exception 중 해당 프로젝트가 선언한 조건 |
| 로깅·유틸리티 전이 의존성 일부 | MIT, BSD-2-Clause, BSD-3-Clause, CC0 |
| Bouncy Castle Provider | Bouncy Castle License |

## 별도로 내려받는 컨테이너 이미지

Compose 구성은 이미지를 저장소에 포함하지 않고 실행 시 각 레지스트리에서 내려받습니다.

| 이미지 역할 | 핵심 구성 요소의 라이선스 | 확인 사항 |
|---|---|---|
| MariaDB 데이터베이스 | MariaDB Server GPL-2.0 | 이미지에 포함된 OS 패키지의 개별 고지도 함께 유지합니다. |
| Eclipse Temurin 런타임 | GPL-2.0-with-Classpath-Exception | JRE와 베이스 이미지 패키지의 고지를 배포 이미지에서 확인합니다. |
| Maven 빌드 환경 | Apache Maven Apache-2.0 | 빌드 이미지의 JDK와 베이스 패키지는 각각의 라이선스를 따릅니다. |

바이너리를 재배포할 때는 패키지 안의 `META-INF` 라이선스와 고지 파일을 제거하지 말고, 선택한 배포 형태에 필요한 소스 제공·고지 의무를 별도로 확인하세요. 이 문서는 자동 수집 결과를 정리한 것이며 법률 자문을 대신하지 않습니다.
