# Implementation Plan: 화면 배치와 표시

**Branch**: `002-screen-display-rules` | **Date**: 2026-09-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-screen-display-rules/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

로그인, 회원가입, 내 정보, 사용자 목록, 사용자 상세의 순서와 묶음, 보이는 조건만 맞춘다. 기존 Spring Boot 화면을 유지하고 공통 스타일시트 하나로 본문 열을 최대 640px, 가운데 정렬로 제한한다. 실패 코드는 `errorForm`이 가리키는 폼의 입력칸 바로 위에만 두고, 목록 아이디만 상세로 연결한다. 검증과 저장은 001을 바꾸지 않는다.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.1.1, Spring MVC, Thymeleaf. 배치용 추가 라이브러리 없음

**Storage**: 변경 없음. H2와 001 엔티티를 그대로 사용

**Testing**: JUnit 5, MockMvc. 002 테스트 이름은 `s002Fr00N_`. 001 테스트는 회귀로 유지

**Target Platform**: 로컬 JVM 웹 서버. 확인 창은 가로 1280px와 390px

**Project Type**: 서버 렌더링 웹 애플리케이션

**Performance Goals**: 추가 처리량 목표 없음. 001의 목록 응답 목표를 유지

**Constraints**: 서버 검증·권한·상태 전이·저장 결과 불변, 오류 코드 문자 불변, 비밀번호 원문 미표시, 본문 최대 폭 640px, 색·로고·글꼴 이름 없음

**Scale/Scope**: 화면 5개. 새 라우트 없음. 상세 진입은 목록 아이디 링크 하나

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Before Phase 0

| 원칙 | 판정 | 근거 |
| --- | --- | --- |
| I. Spec 우선 | 통과 | 구현은 002 명세의 순서와 표시만 다룬다. 색과 로고는 넣지 않는다. |
| II. 산출물 추적성 | 통과 | 화면 계약과 `s002Fr00N_` 테스트가 002 FR 번호를 가진다. |
| III. 입력값 검증 | 통과 | 검증 규칙과 실패 코드는 001 서버 검증을 유지한다. |
| IV. 비밀번호 비평문 저장 | 통과 | 저장 방식을 바꾸지 않는다. 입력 중 문자는 가리고 실패 응답에 원문을 남기지 않는다. |
| V. 역할 분리 | 통과 | 라우트 권한은 001과 같다. |
| VI. 개인정보 격리 | 통과 | USER의 관리자 화면 거부와 본인 경로를 바꾸지 않는다. |
| VII. Soft Delete | 통과 | 탈퇴는 상태와 시각 표시만 유지하고 삭제하지 않는다. |
| VIII. 주요 기능 테스트 | 통과 | 다섯 화면의 성공 배치와 실패 코드 위치를 테스트한다. |
| IX. 회귀 검증 | 통과 | 표시 변경 후 001 테스트를 포함한 `mvn test`를 다시 실행한다. |
| X. 산출물 구조 | 통과 | 기능 디렉터리 `specs/002-screen-display-rules`에 명세, 설계, 계약을 둔다. |

Phase 0으로 진행한다. 정당화되지 않은 위반은 없다.

### After Phase 1

위 10개 원칙을 화면 모델, 화면 계약, 실행 절차에 다시 대조했다. 저장 필드와 상태 전이는 001과 같고, 실패 코드의 위치만 `errorForm`으로 한정한다. 창 크기 1280px, 390px, 최대 폭 640px가 계약과 quickstart에 있다. 판정은 모두 통과이다.

## Project Structure

### Documentation (this feature)

```text
specs/002-screen-display-rules/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── screens.md
└── tasks.md             # /speckit-tasks 에서 작성
```

### Source Code (repository root)

```text
src/main/resources/static/layout.css
src/main/resources/templates/
├── login.html
├── signup.html
├── me.html
├── fragments.html
└── admin/
    ├── users.html
    └── user-detail.html
src/main/java/com/poc/usermanagement/web/
├── ProfileController.java
└── AdminUserController.java
src/test/java/com/poc/usermanagement/display/
```

**Structure Decision**: 001의 Maven 프로젝트와 패키지를 유지한다. 서비스, 보안, 저장소는 수정 대상이 아니다. 컨트롤러는 실패 폼 이름만 모델에 더하고, 순서와 폭은 템플릿과 스타일시트에서 맞춘다.

## Complexity Tracking

해당 없음. Constitution Check 위반이 없다.
