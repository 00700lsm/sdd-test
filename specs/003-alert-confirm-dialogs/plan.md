# Implementation Plan: 알림 창과 확인 창

**Branch**: `003-alert-confirm-dialogs` | **Date**: 2026-09-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-alert-confirm-dialogs/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

회원탈퇴, 상태 변경, 권한 변경은 저장 요청 전에 페이지 안 확인 창으로 진행 여부를 묻는다. 입력이 비어 있거나 나중에 거부되어도 창이 먼저다. 취소하면 요청을 보내지 않는다. 가입, 회원정보 수정, 비밀번호 변경, 탈퇴, 상태 변경, 권한 변경이 성공하면 지정 문장의 알림 창을 한 번 연다. 확인 버튼은 창만 닫는다. 검증, 권한, 상태 전이, 저장 결과는 001을 유지한다.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.1.1, Spring MVC, Thymeleaf. 창은 HTML `dialog`와 정적 스크립트 하나. 추가 라이브러리 없음

**Storage**: 변경 없음. H2와 001 엔티티를 그대로 사용. 알림 문장은 저장하지 않는다

**Testing**: JUnit 5, MockMvc. 003 테스트 이름은 `s003Fr00N_`. 001과 002 테스트는 회귀로 유지. 취소와 빈 입력의 창 순서는 quickstart의 브라우저 확인

**Target Platform**: 로컬 JVM 웹 서버. 창 버튼은 가로 390px에서도 페이지 전체를 가로로 밀지 않고 도달

**Project Type**: 서버 렌더링 웹 애플리케이션

**Performance Goals**: 추가 처리량 목표 없음. 001의 목록 응답 목표를 유지

**Constraints**: 서버 검증·권한·상태 전이·저장 결과 불변, 오류 코드 문자 불변, 창 문장에 비밀번호 원문 없음, 버튼 문자는 확인과 취소로 고정, 색·로고·글꼴 이름 없음

**Scale/Scope**: 확인 창 3종, 성공 알림 6종. 새 라우트 없음. 로그인, 로그아웃, 검색, 화면 이동에는 창을 열지 않음

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Before Phase 0

| 원칙 | 판정 | 근거 |
| --- | --- | --- |
| I. Spec 우선 | 통과 | 구현은 003 명세의 확인 대상, 알림 문장, 취소 시 미저장만 다룬다. |
| II. 산출물 추적성 | 통과 | 창 계약과 `s003Fr00N_` 테스트가 003 FR 번호를 가진다. |
| III. 입력값 검증 | 통과 | 확인 창은 요청 전 화면 게이트이다. 수락과 거부는 001 서버 검증을 유지한다. |
| IV. 비밀번호 비평문 저장 | 통과 | 저장 방식을 바꾸지 않는다. 창 문장은 고정 문구이고 원문을 넣지 않는다. |
| V. 역할 분리 | 통과 | 라우트 권한은 001과 같다. 상태·권한 알림은 그 요청을 실행한 관리자 화면에만 둔다. |
| VI. 개인정보 격리 | 통과 | USER의 관리자 화면 거부와 본인 경로를 바꾸지 않는다. |
| VII. Soft Delete | 통과 | 탈퇴 확인 후에만 기존 논리 삭제를 적용한다. 취소는 기록을 바꾸지 않는다. |
| VIII. 주요 기능 테스트 | 통과 | 확인이 필요한 폼, 성공 알림, 실패 시 알림 없음을 테스트한다. |
| IX. 회귀 검증 | 통과 | 창을 넣은 뒤 001·002 테스트를 포함한 `mvn test`를 다시 실행한다. |
| X. 산출물 구조 | 통과 | 기능 디렉터리 `specs/003-alert-confirm-dialogs`에 명세, 설계, 계약을 둔다. |

Phase 0으로 진행한다. 정당화되지 않은 위반은 없다.

### After Phase 1

위 10개 원칙을 창 모델, 창 계약, 실행 절차에 다시 대조했다. 저장 필드와 상태 전이는 001과 같다. 확인은 요청 전 스크립트가 맡고, 성공 문장은 리다이렉트의 일회 값 또는 상세 응답의 `notice`로만 전달한다. `/dialogs.js`만 익명으로 읽을 수 있는 정적 경로에 추가한다. 인증과 인가 판단은 바꾸지 않는다. 판정은 모두 통과이다.

## Project Structure

### Documentation (this feature)

```text
specs/003-alert-confirm-dialogs/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── dialogs.md
└── tasks.md             # /speckit-tasks 에서 작성
```

### Source Code (repository root)

```text
src/main/resources/static/dialogs.js
src/main/resources/templates/
├── fragments.html
├── login.html
├── signup.html
├── me.html
└── admin/
    └── user-detail.html
src/main/java/com/poc/usermanagement/web/
├── SignupController.java
├── ProfileController.java
└── AdminUserController.java
src/main/java/com/poc/usermanagement/security/SecurityConfig.java
src/test/java/com/poc/usermanagement/dialog/
```

**Structure Decision**: 001의 Maven 프로젝트와 패키지를 유지한다. 서비스와 저장소는 수정 대상이 아니다. 확인 창은 템플릿의 `data-confirm`과 `dialogs.js`가 요청 전에 처리한다. 성공 알림은 컨트롤러가 고정 문장을 `notice`로 넘기고, 조각 템플릿이 알림 창으로 연다. 보안 설정의 공개 경로는 `/dialogs.js`만 추가한다. 로그인 화면의 가입·탈퇴 성공 알림이 익명 상태에서도 스크립트를 읽어야 하기 때문이다. 그 밖의 인증·인가 규칙은 001을 유지한다.

## Complexity Tracking

해당 없음. Constitution Check 위반이 없다.
