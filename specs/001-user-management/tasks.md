---

description: "Task list for user management implementation"
---

# Tasks: 사용자 관리

**Input**: Design documents from `/specs/001-user-management/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: 명세와 헌법 VIII은 주요 기능의 성공 경로와 거부 경로 테스트를 요구한다. 각 스토리의 테스트는 구현 전에 작성하고, 구현 전에는 실패해야 한다.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/main/java/com/poc/usermanagement/`, `src/test/java/com/poc/usermanagement/`, `src/main/resources/`
- Paths follow `specs/001-user-management/plan.md`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create the Maven source directories from `specs/001-user-management/plan.md`: `src/main/java/com/poc/usermanagement/user/`, `src/main/java/com/poc/usermanagement/security/`, `src/main/java/com/poc/usermanagement/web/`, `src/main/java/com/poc/usermanagement/bootstrap/`, `src/main/resources/templates/admin/`, `src/test/java/com/poc/usermanagement/signup/`, `src/test/java/com/poc/usermanagement/session/`, `src/test/java/com/poc/usermanagement/profile/`, `src/test/java/com/poc/usermanagement/withdrawal/`, and `src/test/java/com/poc/usermanagement/admin/`
- [X] T002 Create `pom.xml` with Java 21, Spring Boot 4.1.1 parent, and starters `web`, `security`, `thymeleaf`, `validation`, `data-jpa`, `h2`, plus `spring-boot-starter-test`
- [X] T003 [P] Create the application entry point in `src/main/java/com/poc/usermanagement/UserManagementApplication.java`
- [X] T004 [P] Create `src/main/resources/application.properties` with an H2 datasource, `ddl-auto=update`, and no password value
- [X] T005 [P] Ignore `target/` and local env files in `.gitignore` so `ADMIN_INITIAL_PASSWORD` is not committed

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T006 [P] Create `src/main/java/com/poc/usermanagement/user/Role.java` with only `USER` and `ADMIN`
- [X] T007 [P] Create `src/main/java/com/poc/usermanagement/user/AccountStatus.java` with only `ACTIVE`, `INACTIVE`, and `WITHDRAWN`
- [X] T008 Create `src/main/java/com/poc/usermanagement/user/UserAccount.java` with `loginId` stored lowercase, 4–20 characters, first character a lowercase letter, remaining characters lowercase letters, digits, or underscore, unique across all rows; `passwordHash` as BCrypt and never exposed; `name` trimmed to 1–50 characters; `email` stored lowercase, at most 254 characters, local part and domain separated by `@`, unique across all rows including `WITHDRAWN`; `role`; `status`; `createdAt`; `withdrawnAt` present only when status is `WITHDRAWN`
- [X] T009 Create `src/main/java/com/poc/usermanagement/user/UserAccountRepository.java` with lookup by `loginId` and by lowercase email, and no delete operation
- [X] T010 [P] Create `src/main/java/com/poc/usermanagement/web/FieldErrorCodes.java` with every code listed in `specs/001-user-management/contracts/web.md`
- [X] T011 [P] Create `src/main/java/com/poc/usermanagement/security/SessionInvalidator.java` to expire every active session for one `loginId`
- [X] T012 Create `src/main/java/com/poc/usermanagement/security/AccountReloadFilter.java` to reload status and role from storage on each protected request, invalidate the session when status is `INACTIVE` or `WITHDRAWN`, and apply a changed role without ending the session
- [X] T013 Create `src/main/java/com/poc/usermanagement/security/SecurityConfig.java` permitting `GET` and `POST` `/signup` and `/login`, requiring authentication elsewhere, enabling CSRF, and registering `AccountReloadFilter`
- [X] T014 Create `src/main/java/com/poc/usermanagement/bootstrap/AdminAccountSeeder.java` that inserts `loginId=admin`, `name=관리자`, `email=admin@example.com`, `role=ADMIN`, `status=ACTIVE` only when missing, taking the password only from `ADMIN_INITIAL_PASSWORD` and never writing that password to logs or `src/main/resources/application.properties`
- [X] T015 Exclude password parameters from logs in `src/main/resources/application.properties`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - 회원가입과 로그인 (Priority: P1) 🎯 MVP

**Goal**: 방문자가 계정을 만들고 그 자격으로 로그인한다. 새 계정은 `USER`이자 `ACTIVE`이다.

**Independent Test**: 새 아이디와 이메일로 가입한 뒤 그 자격으로 로그인하면, 자신의 이름과 이메일을 가진 `USER`/`ACTIVE` 계정으로 들어간다.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T016 [P] [US1] Write failing MockMvc tests for valid signup, combined duplicate id and email, combined field-rule failures, and `session.kept` when already logged in in `src/test/java/com/poc/usermanagement/signup/SignupControllerTest.java` for FR-001, FR-002, FR-003, FR-004, FR-005, and FR-006
- [X] T017 [P] [US1] Write failing MockMvc tests for `ACTIVE` login, one `login.failed` result for unknown id, wrong password, `INACTIVE`, and `WITHDRAWN`, and `session.kept` when already logged in in `src/test/java/com/poc/usermanagement/session/LoginControllerTest.java` for FR-007, FR-008, and FR-014

### Implementation for User Story 1

- [X] T018 [US1] Implement registration in `src/main/java/com/poc/usermanagement/user/RegistrationService.java`: trim id, email, and name; store id and email lowercase; id is 4–20 characters, starts with a lowercase letter, then only lowercase letters, digits, or underscore; password is 8–64 characters and not equal to the id; name is 1–50 characters after trim; email is at most 254 characters with a local part and domain separated by `@`; return every failing field code together; enforce uniqueness across all statuses including `WITHDRAWN`; create `role=USER` and `status=ACTIVE`; never return the password
- [X] T019 [US1] Implement `GET` and `POST` `/signup` in `src/main/java/com/poc/usermanagement/web/SignupController.java` and `src/main/resources/templates/signup.html`, returning `session.kept` and preserving the current session when the caller is already authenticated
- [X] T020 [US1] Implement `GET` and `POST` `/login` in `src/main/java/com/poc/usermanagement/web/SessionController.java` and `src/main/resources/templates/login.html`, returning only `login.failed` for every authentication failure and `session.kept` when the caller is already authenticated

**Checkpoint**: User Story 1 is functional and testable without later stories

---

## Phase 4: User Story 2 - 로그아웃 (Priority: P2)

**Goal**: 로그인한 사용자가 로그인 상태를 끝낸다.

**Independent Test**: 시드된 `admin`으로 로그인한 뒤 로그아웃하면 `/me`는 다시 로그인을 요구한다.

### Tests for User Story 2 ⚠️

- [X] T021 [US2] Write a failing MockMvc test that logout ends the session and anonymous logout returns `auth.required` in `src/test/java/com/poc/usermanagement/session/LogoutControllerTest.java` for FR-009 and FR-017

### Implementation for User Story 2

- [X] T022 [US2] Add `POST` `/logout` to `src/main/java/com/poc/usermanagement/web/SessionController.java` so it invalidates the current session and sends the caller to `/login`

**Checkpoint**: User Stories 1 and 2 both work. Logout is verified with the seeded admin account.

---

## Phase 5: User Story 3 - 내 회원정보 조회와 수정 (Priority: P2)

**Goal**: 로그인한 사용자가 자신의 정보를 보고 이름과 이메일만 수정한다.

**Independent Test**: `USER`로 로그인해 이름과 이메일을 수정한 뒤 다시 조회하면 변경 값이 보이고, 다른 사용자 경로로 조회하거나 수정하면 대상 정보가 반환되지 않는다.

### Tests for User Story 3 ⚠️

- [X] T023 [P] [US3] Write failing tests for viewing own `loginId`, name, email, role, and status without `passwordHash`, and for updating name and email, in `src/test/java/com/poc/usermanagement/profile/ProfileViewUpdateTest.java` for FR-010, FR-011, and FR-025
- [X] T024 [P] [US3] Write failing tests that a `USER` calling `/admin/users` or `/admin/users/{loginId}` receives `auth.forbidden` and no other user's personal data in `src/test/java/com/poc/usermanagement/profile/ProfileIsolationTest.java` for FR-015 and FR-016

### Implementation for User Story 3

- [X] T025 [US3] Implement own-profile read and name/email update in `src/main/java/com/poc/usermanagement/user/ProfileService.java`, leaving `loginId`, `role`, and `status` unchanged, accepting the caller's current email as not a duplicate, and returning `email.duplicate` together with `name.length` when both fail
- [X] T026 [US3] Implement `GET` and `POST` `/me` in `src/main/java/com/poc/usermanagement/web/ProfileController.java` and `src/main/resources/templates/me.html`, showing `loginId`, name, email, role, and status, and never `passwordHash`

**Checkpoint**: Own profile works for the seeded admin and for a `USER`. Other users' data stays hidden.

---

## Phase 6: User Story 4 - 비밀번호 변경 (Priority: P2)

**Goal**: 로그인한 사용자가 현재 비밀번호를 확인한 뒤 새 비밀번호로 바꾼다.

**Independent Test**: 현재 비밀번호로 새 비밀번호를 설정한 뒤 로그아웃하고, 이전 비밀번호 로그인은 실패하고 새 비밀번호 로그인은 성공한다.

### Tests for User Story 4 ⚠️

- [X] T027 [US4] Write failing tests for a successful change, combined `password.currentMismatch` and new-password rule failure, and rejection of a `USER` changing another account in `src/test/java/com/poc/usermanagement/profile/PasswordChangeTest.java` for FR-006, FR-012, FR-015, and FR-025

### Implementation for User Story 4

- [X] T028 [US4] Implement password change in `src/main/java/com/poc/usermanagement/user/PasswordService.java` so the current password must match, the new password is 8–64 characters, not equal to `loginId`, and not equal to the current password, both failure codes are returned together, and the current session stays open
- [X] T029 [US4] Add `POST` `/me/password` to `src/main/java/com/poc/usermanagement/web/ProfileController.java` and the password form in `src/main/resources/templates/me.html`

**Checkpoint**: Password change works without logging the caller out.

---

## Phase 7: User Story 5 - 회원탈퇴 (Priority: P2)

**Goal**: 본인 확인 후 상태를 `WITHDRAWN`으로 남기고 로그인을 끝낸다.

**Independent Test**: 비밀번호를 확인하고 탈퇴한 뒤 같은 자격으로 로그인하면 실패하고, 행과 탈퇴 시각은 남는다.

### Tests for User Story 5 ⚠️

- [X] T030 [US5] Write failing tests for soft delete, `login.failed` after withdrawal, wrong current password, and rejection when the caller is the only `ACTIVE` `ADMIN` in `src/test/java/com/poc/usermanagement/withdrawal/WithdrawalTest.java` for FR-013, FR-014, FR-023, FR-024, and FR-025

### Implementation for User Story 5

- [X] T031 [US5] Implement withdrawal in `src/main/java/com/poc/usermanagement/user/WithdrawalService.java` by setting `status=WITHDRAWN` and `withdrawnAt` without deleting the row, ending all sessions for that `loginId`, leaving status and `withdrawnAt` unchanged when the current password is wrong, and returning `admin.last` when the change would leave zero `ACTIVE` `ADMIN` accounts
- [X] T032 [US5] Add `POST` `/me/withdrawal` to `src/main/java/com/poc/usermanagement/web/ProfileController.java` and the withdrawal form in `src/main/resources/templates/me.html`

**Checkpoint**: Withdrawal keeps the row and blocks the next login.

---

## Phase 8: User Story 6 - 관리자의 사용자 조회와 검색 (Priority: P3)

**Goal**: 관리자가 목록, 검색, 상세를 본다. 기본 결과에는 탈퇴 사용자가 없다.

**Independent Test**: `ACTIVE`, `INACTIVE`, `WITHDRAWN` 계정을 저장소에 넣은 뒤 기본 목록에는 탈퇴 사용자가 없고, 탈퇴 포함 조회와 아이디 지정 상세에는 탈퇴 시각이 보인다. `USER`의 같은 요청은 거부된다.

### Tests for User Story 6 ⚠️

- [X] T033 [P] [US6] Write failing tests, using `UserAccountRepository` rather than signup, for the default list, one-box search, empty query, 20-row pages, and the seeded admin login in `src/test/java/com/poc/usermanagement/admin/AdminUserQueryTest.java` for FR-018, FR-019, and FR-026
- [X] T034 [P] [US6] Write failing tests for withdrawn detail including `withdrawnAt` and for `USER` receiving `auth.forbidden` without personal data in `src/test/java/com/poc/usermanagement/admin/AdminUserDetailTest.java` for FR-016 and FR-020

### Implementation for User Story 6

- [X] T035 [US6] Implement paged search in `src/main/java/com/poc/usermanagement/user/AdminUserQueryService.java`: exclude `WITHDRAWN` unless `includeWithdrawn=true`, page size 20, page numbers starting at 1, order by `createdAt` descending then `loginId` ascending, empty `q` equal to the list, a match when `q` is contained in `loginId` or `name` or `email` without case sensitivity, treat `%` and `_` as literal characters, and keep the last page when the requested page is past the end
- [X] T036 [US6] Implement `GET` `/admin/users` and `GET` `/admin/users/{loginId}` in `src/main/java/com/poc/usermanagement/web/AdminUserController.java`, `src/main/resources/templates/admin/users.html`, and `src/main/resources/templates/admin/user-detail.html`, omitting `passwordHash` and showing `withdrawnAt` only when status is `WITHDRAWN`

**Checkpoint**: Admin query works against repository fixtures and does not require later status-change behavior.

---

## Phase 9: User Story 7 - 관리자의 상태와 권한 변경 (Priority: P3)

**Goal**: 관리자가 `ACTIVE`와 `INACTIVE`, `USER`와 `ADMIN` 사이에서만 계정을 바꾼다.

**Independent Test**: `ACTIVE` `USER`를 `INACTIVE`로 바꾸면 로그인할 수 없고, 다시 `ACTIVE`로 바꾸면 로그인할 수 있다. 권한을 `ADMIN`으로 바꾸면 그 세션의 다음 요청부터 관리자 목록이 가능하다.

### Tests for User Story 7 ⚠️

- [X] T037 [US7] Write failing tests for immediate session end on `INACTIVE`, role change without logout, rejection of `WITHDRAWN` changes, `USER` `auth.forbidden`, and protection of the last `ACTIVE` `ADMIN` in `src/test/java/com/poc/usermanagement/admin/AdminUserCommandTest.java` for FR-008, FR-016, FR-017, FR-021, FR-022, and FR-023

### Implementation for User Story 7

- [X] T038 [US7] Implement changes in `src/main/java/com/poc/usermanagement/user/AdminUserCommandService.java` accepting only `ACTIVE` or `INACTIVE` and only `USER` or `ADMIN`, returning `status.invalid`, `role.invalid`, or `account.locked` for `WITHDRAWN`, expiring all sessions when status becomes `INACTIVE`, keeping sessions when only the role changes, and returning `admin.last` when the change would leave zero `ACTIVE` `ADMIN` accounts
- [X] T039 [US7] Add `POST` `/admin/users/{loginId}/status` and `POST` `/admin/users/{loginId}/role` to `src/main/java/com/poc/usermanagement/web/AdminUserController.java` and the forms in `src/main/resources/templates/admin/user-detail.html`

**Checkpoint**: All user stories are independently functional.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T040 [P] Check that no password plaintext is present in `src/main/resources/application.properties` or log settings
- [X] T041 Run `mvn test` and align failures with the scenarios in `specs/001-user-management/quickstart.md`
- [X] T042 [P] Add the matching FR id comments on request methods in `src/main/java/com/poc/usermanagement/web/SignupController.java`, `src/main/java/com/poc/usermanagement/web/SessionController.java`, `src/main/java/com/poc/usermanagement/web/ProfileController.java`, and `src/main/java/com/poc/usermanagement/web/AdminUserController.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel where their files do not overlap
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Starts after Foundational. No other story is required. Creates `SessionController` login.
- **User Story 2 (P2)**: Starts after Foundational. Adds logout to `src/main/java/com/poc/usermanagement/web/SessionController.java`, so implement after T020 if that file is shared. The test uses the seeded admin, not a story-1 registration.
- **User Story 3 (P2)**: Starts after Foundational. Creates `ProfileController`. Isolation tests do not need admin query implementation to expect `auth.forbidden`.
- **User Story 4 (P2)**: Adds password change to `ProfileController` and `me.html`, so implement after T026.
- **User Story 5 (P2)**: Adds withdrawal to `ProfileController` and `me.html`, so implement after T029. The last-admin case uses the seeded admin.
- **User Story 6 (P3)**: Starts after Foundational. Tests insert rows through `UserAccountRepository` and do not require signup.
- **User Story 7 (P3)**: Adds status and role forms to `AdminUserController` and `user-detail.html`, so implement after T036.

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Services before controllers
- Controllers before or with their templates
- Story complete before moving to the next shared file

### Parallel Opportunities

- T003, T004, and T005 can run together after T001 and T002
- T006 and T007 can run together; T010 and T011 can run together after T009
- T016 and T017 can run together
- T023 and T024 can run together
- T033 and T034 can run together
- After Foundational, US1 and US6 can proceed on different files
- T040 and T042 can run together before T041

---

## Parallel Example: User Story 1

```bash
# Tests for User Story 1 together:
Task: "Write failing signup tests in src/test/java/com/poc/usermanagement/signup/SignupControllerTest.java"
Task: "Write failing login tests in src/test/java/com/poc/usermanagement/session/LoginControllerTest.java"
```

## Parallel Example: User Story 6

```bash
# Tests for User Story 6 together:
Task: "Write failing list and search tests in src/test/java/com/poc/usermanagement/admin/AdminUserQueryTest.java"
Task: "Write failing detail tests in src/test/java/com/poc/usermanagement/admin/AdminUserDetailTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Run `SignupControllerTest` and `LoginControllerTest`
5. Demo signup and login if those tests pass

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → signup and login demo
3. Add User Story 2 → logout
4. Add User Story 3 → own profile and isolation
5. Add User Story 4 → password change
6. Add User Story 5 → soft delete
7. Add User Story 6 → admin query
8. Add User Story 7 → status and role changes
9. Run `mvn test` against `specs/001-user-management/quickstart.md`

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1, then User Story 2 in `SessionController`
   - Developer B: User Story 3, then User Story 4 and User Story 5 in `ProfileController`
   - Developer C: User Story 6, then User Story 7 in `AdminUserController`
3. Stories meet at `mvn test` in Phase 10

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence

---

## Phase 11: Convergence

- [X] T043 CRITICAL: Remove the committed plaintext initial admin password from `src/test/resources/application-test.properties` and supply it only at test runtime, without writing it into the spec, `src/main/resources`, logs, or another committed settings file per Constitution IV (contradicts)
- [X] T044 CRITICAL: Add the FR identifier to every test method name under `src/test/java/com/poc/usermanagement/` so each test identifies the spec item it verifies per Constitution II (partial)
- [X] T045 Add tests for specified paths that the code already implements but no test verifies: an ADMIN updating their own name and email leaves `loginId`, role, and status unchanged (US3/AC5, FR-025); id length boundaries and `password.sameAsId` (FR-002); signup as `admin` returns `id.duplicate` (FR-003, FR-026); a WITHDRAWN login id and email cannot be registered again (FR-003, FR-004); `password.unchanged` (FR-012); the default admin list includes INACTIVE and excludes WITHDRAWN (FR-018); requesting status `WITHDRAWN` returns `status.invalid` and leaves the account unchanged (FR-021) per Constitution VIII (partial)
