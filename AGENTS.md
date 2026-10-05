# Repository Guidelines

## 프로젝트 구조와 모듈 구성

V3.0은 Kotlin/Spring 기반 백엔드이며, Gradle 복합 빌드로 구성됩니다.

- `monolith/`: 도메인 모듈(`module-user`, `module-product`, `module-order`, `module-auth`), 공개 계약을 정의하는 `*-contract` 모듈, 실행 진입점인 `module-app`으로 구성됩니다.
- `payment-service/`, `delivery-service/`, `api-gateway/`: 각각 독립적으로 빌드하는 애플리케이션입니다.
- `test-support/`, `monolith/module-test-support/`: 공통 테스트 도구를 제공합니다.
- 각 모듈의 코드·테스트·설정은 `src/main/kotlin`, `src/test/kotlin`, `src/main/resources`에 둡니다.
- `infra/demo/`에는 Docker 초기화와 스모크 테스트, `infra/src/main/resources/`에는 모니터링과 WireMock 설정이 있습니다. 부하 테스트는 `k6/scripts/`, 실험 자료는 `images/`, `diagrams/`, 버전별 README에서 관리합니다.

도메인·애플리케이션·어댑터의 경계를 유지하세요. 다른 도메인에는 구현 클래스가 아닌 계약 모듈을 통해 의존하세요.

## 빌드·테스트·로컬 실행

호스트에서 빌드할 때는 JDK 21과 저장소의 Gradle Wrapper를 사용하세요. Windows에서는 `./gradlew` 대신 `gradlew.bat`을 사용합니다.

- `./gradlew buildAll`: 포함된 모든 프로젝트를 빌드하고 검증합니다.
- `./gradlew -p payment-service build`: 결제 서비스를 빌드하고 검증합니다.
- `./gradlew -p monolith :module-order:test`: 주문 모듈 테스트를 실행합니다.
- `./gradlew -p monolith jacocoTestReport`: 모놀리스 전체의 HTML/XML 커버리지 리포트를 생성합니다.
- `./gradlew -p payment-service ktlintCheck`: Kotlin 스타일을 검사합니다. 자동 정렬은 `ktlintFormat`을 사용합니다.
- `docker compose up --build -d --wait --wait-timeout 240`: 호스트 JDK 없이 로컬 데모를 빌드하고 실행합니다.
- `docker compose run --rm smoke-test`: Gateway의 `18080` 포트를 통해 로그인·주문·모의 결제·배송 흐름을 검증합니다.

## 코드 스타일과 명명 규칙

Kotlin은 기존 코드의 공백 4칸 들여쓰기와 ktlint 규칙을 따르세요. 타입은 PascalCase, 멤버는 camelCase로 작성하고, 역할에 따라 `Service`, `Port`, `Adapter`, `Command` 등의 접미사를 사용하세요. `.editorconfig`를 준수하고 셸 스크립트와 Gradle 실행 파일은 LF 줄바꿈을 유지하세요. KSP 생성 코드와 `build/` 산출물은 직접 수정하지 마세요.

## 테스트 작성 기준

JUnit 5, Kotest 검증 함수, Mockito Kotlin, Spring 슬라이스 테스트를 사용합니다. 클래스 이름은 `*Test`로 작성하며, 테스트 메서드는 한글을 포함한 설명형 백틱 이름을 사용할 수 있습니다. 기존 픽스처와 테스트 어노테이션을 재사용하세요. 동작 변경에는 회귀 테스트를 추가하고, 중복 이벤트·부분 실패·트랜잭션 경계를 검증하세요. JaCoCo 리포트에는 제외 항목이 있으며, 최소 커버리지 수치를 강제하지 않습니다. Codecov 업로드는 비활성화되어 있습니다.

## 커밋과 Pull Request

기존 이력처럼 `feat:`, `fix:`, `chore:`, `ci:` 접두사와 간결한 설명을 사용하세요. 한글 설명도 사용하며, 의존성 변경에는 `fix(deps):` 등의 범위를 지정합니다. 커밋은 하나의 목적에 집중하세요. PR에는 관련 이슈, 동작 변경, 설계 선택의 장단점, 검증 명령과 결과를 적으세요. 구조나 관측 방식이 바뀌면 이해에 도움이 되는 다이어그램 또는 화면을 첨부하세요.

## 보안과 설정 주의사항

Docker 인증 정보는 공개된 데모 전용 값이므로 운영 환경에 사용하지 마세요. 실제 비밀 정보는 Git에 포함하지 마세요. Docker 설정을 변경할 때 기존 IDE 실행 프로파일을 보존하세요. `docker compose down`은 데이터를 유지하지만, `down --volumes`는 데모 데이터를 삭제합니다. 성능 기록에서는 로컬 HTTP 응답 시간과 후속 처리가 완료되는 E2E 시간을 구분하세요.
