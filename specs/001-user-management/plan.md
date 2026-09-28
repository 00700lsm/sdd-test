# Implementation Plan: 사용자 관리

**Branch**: `001-user-management` | **Date**: 2026-09-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-user-management/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

일반 사용자의 가입부터 탈퇴까지와 관리자의 조회, 검색, 상태 변경, 권한 변경을 한 웹 애플리케이션으로 제공한다. Java 21과 Spring Boot 4.1.1에서 서버가 HTML 폼을 처리하고, 세션 권한은 요청마다 저장소에서 다시 읽는다. 비밀번호는 BCrypt로 저장하고, 탈퇴는 행을 지우지 않는다. 요구사항 번호는 [contracts/web.md](contracts/web.md)의 라우트와 테스트 이름에 남긴다.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.1.1, Spring Security, Spring MVC, Thymeleaf, Bean Validation, Spring Data JPA

**Storage**: H2. 아이디와 이메일 유일 제약. 행 삭제 연산 없음

**Testing**: JUnit 5, Spring Boot Test, MockMvc. 테스트 이름에 FR 번호

**Target Platform**: 로컬 JVM 웹 서버

**Project Type**: 서버 렌더링 웹 애플리케이션

**Performance Goals**: 기본 목록 100명에서 첫 페이지 20건과 이름 검색 첫 페이지를 각각 3초 안에 표시 (SC-002)

**Constraints**: 서버 측 입력 검증, 비밀번호 원문 미저장, 요청마다 권한 확인, 탈퇴 행 보존, 페이지 크기 20, 로그인 자동 만료 없음

**Scale/Scope**: 사용자 1종, 권한 2개, 상태 3개, 화면 경로 11개. 시각 디자인 완성도는 범위 밖

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Before Phase 0

| 원칙 | 판정 | 근거 |
| --- | --- | --- |
| I. Spec 우선 | 통과 | 구현은 이 설계 다음이고, 명세에 없는 비밀번호 찾기와 물리 삭제는 넣지 않는다. |
| II. 산출물 추적성 | 통과 | 라우트와 테스트가 FR 번호를 가진다. |
| III. 입력값 검증 | 통과 | 형식, 길이, 중복 검사는 서버에서 하고 실패 항목을 모두 반환한다. |
| IV. 비밀번호 비평문 저장 | 통과 | BCrypt만 저장한다. 초기 비밀번호는 환경값으로만 받는다. |
| V. 역할 분리 | 통과 | USER와 ADMIN을 요청마다 저장소 기준으로 구분한다. |
| VI. 개인정보 격리 | 통과 | 본인 경로는 대상 아이디가 없고, USER의 관리자 경로는 대상 정보 없이 거부한다. |
| VII. Soft Delete | 통과 | WITHDRAWN과 탈퇴 시각만 기록하고 삭제 연산은 없다. |
| VIII. 주요 기능 테스트 | 통과 | 사용자 스토리의 성공 경로와 거부 경로를 테스트한다. |
| IX. 회귀 검증 | 통과 | `mvn test`로 기존 FR 테스트를 다시 실행한다. |
| X. 산출물 구조 | 통과 | 기능 디렉터리 `specs/001-user-management`에 명세, 설계, 계약을 둔다. |

Phase 0으로 진행한다. 정당화되지 않은 위반은 없다.

### After Phase 1

위 10개 원칙을 데이터 모델, 웹 계약, 실행 절차에 다시 대조했다. 세션 재조회, 유일 제약, 페이지 크기, 오류 코드가 명세의 명확화 5건과 일치한다. 판정은 모두 통과이다.

## Project Structure

### Documentation (this feature)

```text
specs/001-user-management/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── web.md
└── tasks.md             # /speckit-tasks 에서 작성
```

### Source Code (repository root)

```text
pom.xml
src/main/java/com/poc/usermanagement/
├── UserManagementApplication.java
├── user/
│   ├── UserAccount.java
│   ├── Role.java
│   ├── AccountStatus.java
│   └── UserAccountRepository.java
├── security/
│   ├── SecurityConfig.java
│   ├── AccountReloadFilter.java
│   └── SessionInvalidator.java
├── web/
│   ├── SignupController.java
│   ├── SessionController.java
│   ├── ProfileController.java
│   └── AdminUserController.java
└── bootstrap/
    └── AdminAccountSeeder.java
src/main/resources/
├── application.properties
└── templates/
src/test/java/com/poc/usermanagement/
├── signup/
├── session/
├── profile/
├── withdrawal/
└── admin/
```

**Structure Decision**: 화면과 검증을 한 Maven 프로젝트에 둔다. 프론트엔드 저장소와 모바일 클라이언트는 두지 않는다. 패키지 경계는 계정, 보안, 화면, 시작 데이터이다.

## Complexity Tracking

해당 없음. Constitution Check 위반이 없다.
