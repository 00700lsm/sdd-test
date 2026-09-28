---

description: "Task list for screen display rules implementation"
---

# Tasks: 화면 배치와 표시

**Input**: Design documents from `/specs/002-screen-display-rules/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: 명세의 인수 시나리오와 헌법 VIII은 주요 화면의 성공 배치와 실패 코드 위치 테스트를 요구한다. 각 스토리의 테스트는 구현 전에 작성하고, 아직 맞지 않는 표시는 구현 전에 실패해야 한다. 테스트 메서드 이름은 `s002Fr00N_`으로 시작한다. 001 테스트 이름은 바꾸지 않는다.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/main/java/com/poc/usermanagement/`, `src/test/java/com/poc/usermanagement/`, `src/main/resources/`
- Paths follow `specs/002-screen-display-rules/plan.md`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create `src/test/java/com/poc/usermanagement/display/` and `src/main/resources/static/` from `specs/002-screen-display-rules/plan.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T002 Update `src/main/resources/templates/fragments.html` so the `errors` fragment prints each `error.code` as its own text only when the error list is non-empty, can be inserted inside one form, and does not render a second copy of the same codes

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - 모든 화면의 공통 배치 (Priority: P1) 🎯 MVP

**Goal**: 로그인, 회원가입, 내 정보, 사용자 목록, 사용자 상세가 같은 제목 규칙과 한 열 배치를 따른다.

**Independent Test**: 다섯 화면 중 아무거나 하나 열면, 제목으로 화면을 구분할 수 있고 입력칸과 그 이름이 짝으로 보인다.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T003 [P] [US1] Write failing MockMvc tests in `src/test/java/com/poc/usermanagement/display/CommonLayoutTest.java` extending `src/test/java/com/poc/usermanagement/support/AbstractWebTest.java` for FR-001 and FR-002: the first heading of `/login`, `/signup`, authenticated `/me`, admin `/admin/users`, and admin `/admin/users/admin` is exactly `로그인`, `회원가입`, `내 정보`, `사용자 목록`, `사용자 상세` respectively; each response links `layout.css`; `src/main/resources/static/layout.css` is absent or does not yet declare `max-width: 640px` and `margin-inline: auto`. Method names start with `s002Fr001_` and `s002Fr002_`
- [X] T004 [P] [US1] Write failing MockMvc tests in `src/test/java/com/poc/usermanagement/display/FieldAndErrorLayoutTest.java` extending `src/test/java/com/poc/usermanagement/support/AbstractWebTest.java` for FR-003 and FR-004: each control is paired with its own item name on one row and two different inputs are not in the same row; the form's primary button comes after that form's inputs; a signup request that fails two fields shows both codes above those inputs without one code replacing the other; password inputs use `type="password"`. Method names start with `s002Fr003_` and `s002Fr004_`

### Implementation for User Story 1

- [X] T005 [US1] Create `src/main/resources/static/layout.css` with one content column of `width: 100%`, `max-width: 640px`, and `margin-inline: auto`; remove the default body margin so a 390px-wide window fits without page-level horizontal scrolling; pair each item name and control on one row and do not place different inputs side by side; let controls shrink inside the column; wrap 50-character names and 254-character emails inside their cells with `overflow-wrap: anywhere`; scroll only the table region with `overflow-x: auto` and `max-width: 100%` when columns exceed the width. Do not set a color palette, logo, or font name
- [X] T006 [P] [US1] Apply the shared column to `src/main/resources/templates/login.html`: viewport `width=device-width`, link `/layout.css`, first content heading `로그인`, content inside `main.column`, each input paired with its own name, and the login button after those inputs
- [X] T007 [P] [US1] Apply the shared column to `src/main/resources/templates/signup.html`: viewport `width=device-width`, link `/layout.css`, first content heading `회원가입`, content inside `main.column`, each input paired with its own name, and the signup button after those inputs
- [X] T008 [P] [US1] Apply the shared column to `src/main/resources/templates/me.html`: viewport `width=device-width`, link `/layout.css`, first content heading `내 정보`, content inside `main.column`, and each input paired with its own name
- [X] T009 [P] [US1] Apply the shared column to `src/main/resources/templates/admin/users.html`: viewport `width=device-width`, link `/layout.css`, first content heading `사용자 목록`, content inside `main.column`, search inputs paired with their own names, and the table inside the scroll region from `layout.css`
- [X] T010 [P] [US1] Apply the shared column to `src/main/resources/templates/admin/user-detail.html`: viewport `width=device-width`, link `/layout.css`, first content heading `사용자 상세`, content inside `main.column`, and each control paired with its own name

**Checkpoint**: User Story 1 is testable on any one of the five screens without later stories

---

## Phase 4: User Story 2 - 로그인과 회원가입 화면 (Priority: P2)

**Goal**: 방문자가 로그인과 회원가입에서 필요한 칸만 위에서 아래로 보고 두 화면을 오간다.

**Independent Test**: 로그인 화면에서 회원가입으로 이동했다가 다시 로그인으로 돌아오고, 각 화면의 칸 순서가 명세 시나리오와 같다.

### Tests for User Story 2 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T011 [US2] Write failing MockMvc tests in `src/test/java/com/poc/usermanagement/display/LoginSignupLayoutTest.java` extending `src/test/java/com/poc/usermanagement/support/AbstractWebTest.java` for FR-005, FR-006, and FR-007: login visible order is heading `로그인`, failure codes, 아이디, 비밀번호, 로그인 button, link to `/signup`, with no other account fields; signup visible order is heading `회원가입`, failure codes, 아이디, 비밀번호, 이름, 이메일, 가입 button, link to `/login`; after signup failure the id, name, and email values remain, the password field is empty, and the password plaintext is absent. Method names start with `s002Fr005_`, `s002Fr006_`, and `s002Fr007_`

### Implementation for User Story 2

- [X] T012 [P] [US2] Set `src/main/resources/templates/login.html` to the order heading `로그인`, failure codes, 아이디, 비밀번호, 로그인 button, `/signup` link, and no other account fields. Keep a failed login's id and leave the password field empty
- [X] T013 [P] [US2] Set `src/main/resources/templates/signup.html` to the order heading `회원가입`, failure codes, 아이디, 비밀번호, 이름, 이메일, 가입 button, `/login` link. After failure keep id, name, and email, and do not put a value on the password input

**Checkpoint**: User Stories 1 and 2 both work. Login and signup are independently reachable from each other

---

## Phase 5: User Story 3 - 내 정보 화면 (Priority: P2)

**Goal**: 로그인한 사용자가 요약, 수정, 비밀번호 변경, 탈퇴, 로그아웃을 서로 다른 묶음으로 본다.

**Independent Test**: 내 정보 화면에서 요약, 수정, 비밀번호 변경, 탈퇴, 로그아웃이 서로 다른 묶음으로 위에서 아래로 보인다.

### Tests for User Story 3 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T014 [US3] Write failing MockMvc tests in `src/test/java/com/poc/usermanagement/display/MeLayoutTest.java` extending `src/test/java/com/poc/usermanagement/support/AbstractWebTest.java` for FR-008, FR-009, and FR-010: visible order is heading `내 정보`, read-only summary 아이디, 이름, 이메일, 권한, 상태, name/email form, password form, withdrawal form, logout; the name/email form contains neither the withdrawal button nor logout; summary values are not editable inputs; a password-change failure shows its codes only immediately above the password inputs and not above the name/email form, the withdrawal form, or the page heading; `ADMIN` sees `사용자 목록` linking to `/admin/users` below logout, and `USER` does not see that link or an empty heading in its place. Method names start with `s002Fr008_`, `s002Fr009_`, and `s002Fr010_`

### Implementation for User Story 3

- [X] T015 [US3] Pass `errorForm` as `profile`, `password`, or `withdrawal` from the failed action only in `src/main/java/com/poc/usermanagement/web/ProfileController.java`. Do not change validation, role checks, or stored results in `src/main/java/com/poc/usermanagement/user/ProfileService.java`, `PasswordService.java`, or `WithdrawalService.java`
- [X] T016 [US3] Reorder `src/main/resources/templates/me.html` to heading `내 정보`, read-only summary 아이디, 이름, 이메일, 권한, 상태, name/email form, password form, withdrawal form, logout. Render failure codes only inside the form named by `errorForm`, immediately above that form's inputs. Keep the `사용자 목록` link to `/admin/users` below logout only when the role is `ADMIN`

**Checkpoint**: User Stories 1 through 3 work. A password failure is visible only on the password form

---

## Phase 6: User Story 4 - 관리자 목록과 상세 (Priority: P3)

**Goal**: ADMIN이 목록에서 사용자를 비교하고, 아이디로 상세에 들어갔다가 목록으로 돌아온다.

**Independent Test**: ADMIN이 목록에서 한 아이디를 선택해 상세를 보고, 목록으로 돌아온 뒤 칸 순서와 빈 결과가 시나리오와 같다.

### Tests for User Story 4 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T017 [P] [US4] Write failing MockMvc tests in `src/test/java/com/poc/usermanagement/display/AdminListLayoutTest.java` extending `src/test/java/com/poc/usermanagement/support/AbstractWebTest.java` for FR-011, FR-012, and FR-013: visible order is heading `사용자 목록`, search bundle, current page number, table or empty result, previous/next, `내 정보` link; visible search order is 검색어, 탈퇴 포함, 검색 button; list failure codes `page.invalid` and `withdrawn.invalid` appear only immediately above the search inputs; column order is 아이디, 이름, 이메일, 권한, 상태, 탈퇴 시각; one user is one row; a row without a withdrawal time has an empty cell; a 50-character name and 254-character email are present in that row; the login id links only to `/admin/users/{loginId}`; no matching users shows `결과 없음` and no user row; missing previous or next page hides that link; the table header remains the first table row. Method names start with `s002Fr011_`, `s002Fr012_`, and `s002Fr013_`
- [X] T018 [P] [US4] Write failing MockMvc tests in `src/test/java/com/poc/usermanagement/display/AdminDetailLayoutTest.java` extending `src/test/java/com/poc/usermanagement/support/AbstractWebTest.java` for FR-014 and FR-015: visible order is heading `사용자 상세`, summary 아이디, 이름, 이메일, 권한, 상태, status form, role form, link to `/admin/users`; withdrawal time appears only when status is `WITHDRAWN` and then immediately after 상태; status and role are separate forms; status choices are only `ACTIVE` and `INACTIVE`; role choices are only `USER` and `ADMIN`; a status failure's codes appear only immediately above the status inputs; a role failure's codes appear only immediately above the role inputs; a withdrawn target still shows both forms. Method names start with `s002Fr014_` and `s002Fr015_`

### Implementation for User Story 4

- [X] T019 [US4] Pass `errorForm` as `list`, `status`, or `role` from the failed action only in `src/main/java/com/poc/usermanagement/web/AdminUserController.java`. Do not change search, status, or role rules in `src/main/java/com/poc/usermanagement/user/AdminUserQueryService.java` or `AdminUserCommandService.java`
- [X] T020 [P] [US4] Update `src/main/resources/templates/admin/users.html` to the order heading `사용자 목록`, search bundle, current page number, table or empty result, previous/next, `내 정보` link. Visible search order is 검색어, 탈퇴 포함, 검색 button. Render list failure codes only immediately above the search inputs when `errorForm` is `list`. Column order is 아이디, 이름, 이메일, 권한, 상태, 탈퇴 시각. Leave the withdrawal-time cell empty when there is no time. Link only the login id to `/admin/users/{loginId}`. Show `결과 없음` and no user row when the list is empty. Hide 이전 when there is no previous page and 다음 when there is no next page. Keep the column names in the table's first row and previous/next below the table. Put the table in the region that scrolls horizontally inside itself
- [X] T021 [P] [US4] Update `src/main/resources/templates/admin/user-detail.html` to the order heading `사용자 상세`, summary 아이디, 이름, 이메일, 권한, 상태, status form, role form, `/admin/users` link. Show 탈퇴 시각 only when status is `WITHDRAWN`, immediately after 상태. Keep status and role as separate forms. Status choices are only `ACTIVE` and `INACTIVE`. Role choices are only `USER` and `ADMIN`. Render status failure codes only immediately above the status inputs when `errorForm` is `status`, and role failure codes only immediately above the role inputs when `errorForm` is `role`

**Checkpoint**: All four user stories work. Choosing a list id opens that user's detail, and detail returns to the list

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T022 Run `mvn test` from the repository root using `pom.xml` and keep every existing test in `src/test/java/com/poc/usermanagement/` passing for FR-016. Do not change error-code characters in `specs/001-user-management/contracts/web.md` or validation results in `src/main/java/com/poc/usermanagement/user/`. If an existing test fails only because markup moved, adjust that locator without changing the expected code or stored outcome
- [X] T023 Check the 1280px and 390px scenarios in `specs/002-screen-display-rules/quickstart.md` on the running server: at 1280px the login column is centered and no wider than 640px; at 390px the login button is reached without scrolling the page horizontally; at 390px the user-list heading and search button need no page-level horizontal scroll while only the table region may scroll

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User Story 1 finishes the shared column on every template before later stories edit those files
  - After User Story 1, User Stories 2, 3, and 4 can proceed in parallel because they touch different files
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after User Story 1 - edits `login.html` and `signup.html` only, and stays testable without the admin screens
- **User Story 3 (P2)**: Can start after User Story 1 - edits `me.html` and `ProfileController.java` only
- **User Story 4 (P3)**: Can start after User Story 1 - edits the admin templates and `AdminUserController.java` only. The list-to-detail link is new in this story

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Shared stylesheet before the five templates in User Story 1
- `errorForm` on the controller before the template reads it in User Stories 3 and 4
- Story complete before moving to the next priority when one person is implementing

### Parallel Opportunities

- T003 and T004 can run in parallel
- T006, T007, T008, T009, and T010 can run in parallel after T005
- T012 and T013 can run in parallel after T011
- After User Story 1, User Stories 2, 3, and 4 can run in parallel
- T017 and T018 can run in parallel
- T020 and T021 can run in parallel after T019

---

## Parallel Example: User Story 1

```bash
# Tests together:
Task: "Write CommonLayoutTest in src/test/java/com/poc/usermanagement/display/CommonLayoutTest.java"
Task: "Write FieldAndErrorLayoutTest in src/test/java/com/poc/usermanagement/display/FieldAndErrorLayoutTest.java"

# After layout.css, templates together:
Task: "Apply the shared column to src/main/resources/templates/login.html"
Task: "Apply the shared column to src/main/resources/templates/signup.html"
Task: "Apply the shared column to src/main/resources/templates/me.html"
Task: "Apply the shared column to src/main/resources/templates/admin/users.html"
Task: "Apply the shared column to src/main/resources/templates/admin/user-detail.html"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Open any one screen and confirm its title, one column, and paired inputs
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test login and signup order independently → Deploy/Demo
4. Add User Story 3 → Test that a password failure stays on the password form → Deploy/Demo
5. Add User Story 4 → Test list id to detail and back → Deploy/Demo
6. Run the full `mvn test` regression and the 1280px / 390px checks

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. One developer completes User Story 1 because every template receives the shared column
3. Once User Story 1 is done:
   - Developer A: User Story 2
   - Developer B: User Story 3
   - Developer C: User Story 4
4. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
- Do not change 001 validation, roles, status transitions, or stored results. FR-016 allows only order, grouping, and visibility of the same information
