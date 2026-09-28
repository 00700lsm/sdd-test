# Web Contract: 사용자 관리

서버가 렌더링하는 HTML 폼 계약이다. 보호 요청은 세션이 있고, 처리할 때 저장소의 상태와 권한을 다시 읽는다. 상태 변경 폼에는 CSRF 토큰이 필요하다.

실패 응답은 변경을 저장하지 않고, 아래 코드를 해당 항목과 함께 모두 보여 준다. 로그인 실패만 예외이며 항상 `login.failed` 하나이다.

| 코드 | 의미 |
| --- | --- |
| `id.required` | 아이디 없음 |
| `id.length` | 아이디 길이 위반 |
| `id.pattern` | 아이디 문자 위반 |
| `id.duplicate` | 아이디 중복 |
| `password.required` | 비밀번호 없음 |
| `password.length` | 비밀번호 길이 위반 |
| `password.sameAsId` | 비밀번호가 아이디와 같음 |
| `password.currentMismatch` | 현재 비밀번호 불일치 |
| `password.unchanged` | 새 비밀번호가 현재와 같음 |
| `name.required` | 이름 없음 |
| `name.length` | 이름 길이 위반 |
| `email.required` | 이메일 없음 |
| `email.format` | 이메일 형식 위반 |
| `email.length` | 이메일 길이 위반 |
| `email.duplicate` | 이메일 중복 |
| `login.failed` | 로그인 실패. 원인을 구분하지 않음 |
| `auth.required` | 로그인 필요 |
| `auth.forbidden` | 권한 없음. 대상 개인정보를 포함하지 않음 |
| `session.kept` | 이미 로그인한 상태에서 가입 또는 로그인 거부. 현재 세션 유지 |
| `page.invalid` | 페이지 번호가 양의 정수가 아님 |
| `withdrawn.invalid` | 탈퇴 포함 값이 `true` 또는 `false`가 아님 |
| `status.invalid` | ACTIVE, INACTIVE가 아닌 상태 변경 |
| `role.invalid` | USER, ADMIN이 아닌 권한 변경 |
| `account.locked` | 탈퇴 계정의 상태 또는 권한 변경 |
| `admin.last` | 마지막 ACTIVE ADMIN 보호 |

## 라우트

| 경로 | 메서드 | 허용 | 명세 | 동작 |
| --- | --- | --- | --- | --- |
| `/signup` | GET | 비로그인 | FR-001 | 가입 폼 |
| `/signup` | POST | 비로그인 | FR-001, FR-002, FR-003, FR-004, FR-005, FR-006 | `loginId`, `password`, `name`, `email`. 성공 시 로그인 화면. 로그인 중이면 `session.kept` |
| `/login` | GET | 비로그인 | FR-007 | 로그인 폼 |
| `/login` | POST | 비로그인 | FR-007, FR-008, FR-014, FR-026 | `loginId`, `password`. 성공 시 세션 생성 후 본인 정보. 실패는 `login.failed`. 로그인 중이면 `session.kept` |
| `/logout` | POST | USER, ADMIN | FR-009 | 세션 종료 후 로그인 화면. 비로그인은 `auth.required` |
| `/me` | GET | USER, ADMIN | FR-010, FR-025 | 아이디, 이름, 이메일, 권한, 상태. 비밀번호 없음 |
| `/me` | POST | USER, ADMIN | FR-004, FR-011, FR-025 | `name`, `email`만 변경 |
| `/me/password` | POST | USER, ADMIN | FR-002, FR-006, FR-012, FR-025 | `currentPassword`, `newPassword`. 성공 후에도 현재 세션 유지 |
| `/me/withdrawal` | POST | USER, ADMIN | FR-013, FR-023, FR-024, FR-025 | `currentPassword`. 성공 시 WITHDRAWN, 탈퇴 시각, 모든 세션 종료 |
| `/admin/users` | GET | ADMIN | FR-016, FR-018, FR-019 | `q`, `page`, `includeWithdrawn`. 20건. 최근 가입 순. USER는 `auth.forbidden` |
| `/admin/users/{loginId}` | GET | ADMIN | FR-016, FR-020 | 상세. 탈퇴 계정이면 탈퇴 시각 포함. USER는 `auth.forbidden` |
| `/admin/users/{loginId}/status` | POST | ADMIN | FR-016, FR-021, FR-023 | `status`는 ACTIVE 또는 INACTIVE. INACTIVE면 대상 세션 전부 만료 |
| `/admin/users/{loginId}/role` | POST | ADMIN | FR-016, FR-022, FR-023 | `role`은 USER 또는 ADMIN. 세션은 유지 |

본인 기능의 경로에는 대상 아이디가 없다. 다른 사용자 식별자를 넣어 본인 경로를 호출할 수 없다. 관리자 경로는 저장소의 권한을 확인한 뒤에만 대상 정보를 반환한다.

목록의 `page` 기본값은 1이다. 마지막 페이지보다 큰 번호는 마지막 페이지를 보여 준다. `includeWithdrawn` 기본값은 `false`이다. `q`가 비어 있으면 같은 필터의 목록과 같다.
