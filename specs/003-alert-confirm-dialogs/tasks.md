---

description: "Task list for alert and confirm dialog implementation"
---

# Tasks: 알림 창과 확인 창

**Input**: Design documents from `/specs/003-alert-confirm-dialogs/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: 명세의 인수 시나리오와 헌법 VIII은 확인 창, 성공 알림, 창을 띄우지 않는 상황의 테스트를 요구한다. 각 스토리의 테스트는 구현 전에 작성하고, 아직 맞지 않는 창은 구현 전에 실패해야 한다. 테스트 메서드 이름은 `s003Fr00N_`으로 시작한다. 001과 002 테스트 이름은 바꾸지 않는다.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/main/java/com/poc/usermanagement/`, `src/test/java/com/poc/usermanagement/`, `src/main/resources/`
- Paths follow `specs/003-alert-confirm-dialogs/plan.md`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create `src/test/java/com/poc/usermanagement/dialog/` from `specs/003-alert-confirm-dialogs/plan.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T002 Add closed dialog shells to `src/main/resources/templates/fragments.html`: a notice dialog with one message element and a single `type="button"` labeled `확인`, and a confirm dialog with one question element plus `type="button"` controls labeled `확인` and `취소`. Do not open either dialog by default and do not add any other button
- [X] T003 Permit anonymous `GET /dialogs.js` in `src/main/java/com/poc/usermanagement/security/SecurityConfig.java` by adding `/dialogs.js` beside `/signup`, `/login`, and `/layout.css`. Do not change any other authorization rule
- [X] T004 In `src/main/resources/static/layout.css`, limit an open dialog to the content column width so its `확인` and `취소` controls are reachable at 390px without scrolling the page horizontally. Do not set a color palette, logo, or font name

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - 되돌리기 어려운 동작 전의 확인 (Priority: P1) 🎯 MVP

**Goal**: 회원탈퇴, 상태 변경, 권한 변경은 저장 요청 전에 확인 창을 연다. 취소하면 검증과 저장을 하지 않는다.

**Independent Test**: 각 동작에서 취소를 누르면 탈퇴 상태, 탈퇴 시점, 상태, 권한이 누르기 전과 같다. 확인을 누르면 기존 사용자 관리 규칙대로 진행된다.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T005 [US1] Write failing MockMvc tests in `src/test/java/com/poc/usermanagement/dialog/ConfirmDialogTest.java` extending `src/test/java/com/poc/usermanagement/support/AbstractWebTest.java` for FR-001, FR-002, FR-003, FR-004, FR-008, and FR-009. The authenticated `/me` withdrawal form posts to `/me/withdrawal`, has `novalidate`, and `data-confirm` exactly `탈퇴하면 이 계정으로 다시 로그인할 수 없습니다. 탈퇴할까요?`. The admin detail status form and role form have `novalidate` and `data-confirm` exactly `이 사용자의 상태를 변경할까요?` and `이 사용자의 권한을 변경할까요?`. `GET /dialogs.js` returns 200 and the script prevents the first submit of a `data-confirm` form, closes without submitting on `취소`, and submits that form once on `확인`. An empty or wrong withdrawal password still renders the confirm question before any failure code. Method names start with `s003Fr001_`, `s003Fr002_`, `s003Fr003_`, `s003Fr004_`, `s003Fr008_`, and `s003Fr009_`

### Implementation for User Story 1

- [X] T006 [P] [US1] Create `src/main/resources/static/dialogs.js` so a form with `data-confirm` opens the confirm dialog with `showModal()` before the request, even when a required field is empty. `취소` only closes the dialog and leaves the inputs unchanged. `확인` submits that same form once. While the dialog is open, the rest of the page cannot proceed, and the script does not place the password value in the question
- [X] T007 [P] [US1] Mark the `/me/withdrawal` form in `src/main/resources/templates/me.html` with `novalidate` and `data-confirm="탈퇴하면 이 계정으로 다시 로그인할 수 없습니다. 탈퇴할까요?"`, include the confirm dialog fragment, and link `/dialogs.js`. Do not add that attribute to the profile, password, or logout forms
- [X] T008 [P] [US1] Mark the status form and role form in `src/main/resources/templates/admin/user-detail.html` with `novalidate` and `data-confirm` exactly `이 사용자의 상태를 변경할까요?` and `이 사용자의 권한을 변경할까요?`, include the confirm dialog fragment, and link `/dialogs.js`

**Checkpoint**: User Story 1 is testable by cancelling withdrawal, status change, or role change without later stories

---

## Phase 4: User Story 2 - 저장 성공 후의 알림 (Priority: P2)

**Goal**: 가입, 회원정보 수정, 비밀번호 변경, 탈퇴, 상태 변경, 권한 변경이 실제로 성공했을 때만 지정 문장의 알림 창을 한 번 연다.

**Independent Test**: 각 성공 한 건마다 지정한 문장의 알림이 한 번 보이고, 그 문장에 비밀번호가 없다. 알림의 확인을 눌러도 같은 저장이 반복되지 않는다.

### Tests for User Story 2 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T009 [US2] Write failing MockMvc tests in `src/test/java/com/poc/usermanagement/dialog/NoticeDialogTest.java` extending `src/test/java/com/poc/usermanagement/support/AbstractWebTest.java` for FR-005, FR-006, and FR-009. Successful signup shows `가입되었습니다.` once on the login screen and does not log the visitor in. Successful name and email update shows `회원정보를 수정했습니다.` once on `/me`. Successful password change shows `비밀번호를 변경했습니다.` once and keeps the session. Successful withdrawal shows `탈퇴했습니다.` once and then the login screen with the session ended. Successful status change shows `상태를 변경했습니다.` once on that admin detail only. Successful role change shows `권한을 변경했습니다.` once and does not end the target session by the role change alone. Each notice dialog has only a `type="button"` labeled `확인`. None of the six sentences contains the submitted password. Method names start with `s003Fr005_`, `s003Fr006_`, and `s003Fr009_`

### Implementation for User Story 2

- [X] T010 [P] [US2] On signup success in `src/main/java/com/poc/usermanagement/web/SignupController.java`, flash `notice` exactly `가입되었습니다.` before `redirect:/login`. Do not set `notice` when signup fails, and do not put the password in the message
- [X] T011 [P] [US2] On success in `src/main/java/com/poc/usermanagement/web/ProfileController.java`, flash `notice` exactly `회원정보를 수정했습니다.` before `redirect:/me`, `비밀번호를 변경했습니다.` before `redirect:/me`, and `탈퇴했습니다.` before `redirect:/login`. Do not set `notice` on a failed update, password change, or withdrawal
- [X] T012 [P] [US2] On an empty error list in `src/main/java/com/poc/usermanagement/web/AdminUserController.java`, set model `notice` to exactly `상태를 변경했습니다.` or `권한을 변경했습니다.` for that command only. Do not set `notice` when the command is rejected, and do not add the notice to any other user's screen
- [X] T013 [P] [US2] Include the notice dialog fragment and `/dialogs.js` in `src/main/resources/templates/login.html` so a present `notice` can open there
- [X] T014 [P] [US2] Include the notice dialog fragment in `src/main/resources/templates/me.html` so a present `notice` can open there
- [X] T015 [P] [US2] Include the notice dialog fragment in `src/main/resources/templates/admin/user-detail.html` so a present `notice` can open there
- [X] T016 [US2] Extend `src/main/resources/static/dialogs.js` so a rendered notice dialog opens with `showModal()` once, and its `확인` button only closes the dialog and does not submit any form

**Checkpoint**: User Stories 1 and 2 both work: confirm before the risky actions, then one success notice

---

## Phase 5: User Story 3 - 창을 띄우지 않는 상황 (Priority: P3)

**Goal**: 로그인, 로그아웃, 검색, 화면 이동과 가입·수정·비밀번호 변경의 입력 실패는 창을 열지 않는다. 탈퇴·상태·권한의 거부는 확인 뒤에 실패 코드만 보인다.

**Independent Test**: 로그인 실패, 가입·수정·비밀번호 변경의 형식 오류, 로그아웃, 검색, 링크 이동에서 알림 창과 확인 창이 없다. 실패 코드는 해당 묶음 위에 남는다.

### Tests for User Story 3 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T017 [US3] Write failing MockMvc tests in `src/test/java/com/poc/usermanagement/dialog/NoDialogTest.java` extending `src/test/java/com/poc/usermanagement/support/AbstractWebTest.java` for FR-007, FR-008, FR-010, and FR-011. Login failure, signup validation failure, profile validation failure, and password validation failure render no open notice dialog and no `data-confirm`. The logout form and the `/admin/users` search form have no `data-confirm`. After confirming would be bypassed by a direct post, a rejected withdrawal, status change, or role change — including the last ACTIVE admin — returns the existing failure code only above that form, with no `notice`, and leaves the stored account unchanged. Method names start with `s003Fr007_`, `s003Fr008_`, `s003Fr010_`, and `s003Fr011_`

### Implementation for User Story 3

- [X] T018 [US3] Keep `data-confirm` off the login form in `src/main/resources/templates/login.html`, the signup form in `src/main/resources/templates/signup.html`, the profile, password, and logout forms in `src/main/resources/templates/me.html`, and the search form in `src/main/resources/templates/admin/users.html`. In `SignupController.java`, `ProfileController.java`, `AdminUserController.java`, and `SessionController.java`, leave failure responses without `notice` so the 001 failure codes stay in the 002 `errorForm` position only

**Checkpoint**: All three stories are independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T019 Run `mvn test` from the repository root using `pom.xml` and keep every existing test in `src/test/java/com/poc/usermanagement/` passing for FR-010. Do not change error-code characters in `specs/001-user-management/contracts/web.md` or validation results in `src/main/java/com/poc/usermanagement/user/`
- [X] T020 Check the browser scenarios in `specs/003-alert-confirm-dialogs/quickstart.md` on the running server: an empty withdrawal password opens the confirm dialog before any failure code, cancel leaves the account unchanged, each success sentence appears once, and at 390px the open dialog's `확인` and `취소` need no page-level horizontal scroll

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User Story 1 adds the confirm gate before later stories edit the same templates and `dialogs.js`
  - User Story 2 starts after User Story 1
  - User Story 3 starts after User Story 2 because it checks that the other forms stayed without a confirm gate
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after User Story 1 - adds `notice` on the success redirects and the detail response, and opens the notice dialog from the same script
- **User Story 3 (P3)**: Can start after User Story 2 - proves login, logout, search, and ordinary validation failures still have no dialog

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- The shared dialog fragment exists before a template includes it
- `dialogs.js` confirm behavior is in place before success-notice opening is added to that same file
- Story complete before moving to the next priority

### Parallel Opportunities

- T002, T003, and T004 touch different files and can run in parallel after T001
- T006, T007, and T008 can run in parallel after T005
- T010, T011, T012, T013, T014, and T015 can run in parallel after T009
- T016 follows those template and controller tasks because it opens the notice they render
- T019 and T020 run after T018

---

## Parallel Example: User Story 1

```bash
# After the failing confirm test:
Task: "Create src/main/resources/static/dialogs.js"
Task: "Mark the withdrawal form in src/main/resources/templates/me.html"
Task: "Mark the status and role forms in src/main/resources/templates/admin/user-detail.html"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Cancel withdrawal, status change, and role change and confirm nothing is stored
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test the three confirm gates independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test the six success sentences independently → Deploy/Demo
4. Add User Story 3 → Test that login, logout, search, and validation failures stay quiet → Deploy/Demo
5. Run the full `mvn test` regression and the 390px dialog check

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. One developer completes User Story 1 because `dialogs.js`, `me.html`, and `user-detail.html` receive the confirm gate
3. Once User Story 1 is done, one developer completes User Story 2 because those same files receive the notice dialog
4. User Story 3 is the absence check after both dialogs exist

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
- Do not change 001 validation, roles, status transitions, or stored results. FR-010 allows only the confirm gate before the request and the success notice after it
