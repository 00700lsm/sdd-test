# Data Model: 사용자 관리

저장 엔티티는 사용자 하나이다. 권한과 상태는 그 사용자의 값이다.

## UserAccount

| 필드 | 필수 | 규칙 |
| --- | --- | --- |
| id | 예 | 내부 식별자. 외부 입력으로 받지 않는다. |
| loginId | 예 | 저장 시 소문자. 4–20자. 첫 글자는 영문 소문자. 이후 영문 소문자, 숫자, 밑줄. 전체 행에서 유일. |
| passwordHash | 예 | BCrypt 해시. 응답과 화면에 포함하지 않는다. |
| name | 예 | 앞뒤 공백 제거 후 1–50자. |
| email | 예 | 저장 시 소문자. 앞뒤 공백 제거 후 254자 이하. `@`로 로컬부와 도메인이 나뉜 형식. 전체 행에서 유일. |
| role | 예 | `USER`, `ADMIN`. |
| status | 예 | `ACTIVE`, `INACTIVE`, `WITHDRAWN`. |
| createdAt | 예 | 가입 시각. 목록과 검색의 정렬 기준. 내림차순. |
| withdrawnAt | 조건부 | `WITHDRAWN`일 때만 시각이 있다. 그 외에는 비어 있다. |

공개 가입으로 만드는 행은 `role=USER`, `status=ACTIVE`, `withdrawnAt` 없음이다.

시작 시 없으면 한 행을 만든다. `loginId=admin`, `name=관리자`, `email=admin@example.com`, `role=ADMIN`, `status=ACTIVE`. 비밀번호 해시는 환경값으로만 만든다.

유일 제약은 탈퇴 행을 포함한 모든 상태에 적용한다. 행을 삭제하는 연산은 없다.

## 상태 전이

```text
ACTIVE → INACTIVE     관리자 상태 변경. 대상의 모든 세션 만료.
INACTIVE → ACTIVE     관리자 상태 변경. 이후 로그인 가능.
ACTIVE → WITHDRAWN    본인 탈퇴와 현재 비밀번호 확인. withdrawnAt 기록. 대상의 모든 세션 만료.
INACTIVE → WITHDRAWN  본인 탈퇴와 현재 비밀번호 확인. 같은 기록과 세션 만료.
WITHDRAWN → *         없음.
* → WITHDRAWN         관리자 상태 변경으로는 불가.
```

마지막 `ACTIVE`이면서 `ADMIN`인 계정은 `USER`로 바꾸거나, `INACTIVE`로 바꾸거나, 탈퇴할 수 없다.

권한 변경은 `USER`와 `ADMIN` 사이만 가능하다. `WITHDRAWN` 계정은 권한과 상태를 바꾸지 않는다. 권한 변경은 세션을 지우지 않는다. 다음 보호 요청이 저장소의 권한을 따른다.

## 조회

기본 목록과 기본 검색은 `WITHDRAWN`을 제외한다. 탈퇴 포함을 선택한 뒤에만 포함한다. 상세는 관리자가 아이디를 지정하면 탈퇴 계정도 보여 주며, 그때 `withdrawnAt`을 포함한다.

정렬은 `createdAt` 내림차순, 같은 시각이면 `loginId` 오름차순으로 고정한다. 페이지 크기는 20이고 번호는 1부터이다. 요청 페이지가 마지막을 넘으면 마지막 페이지를 유지한다.

검색어가 있으면 소문자로 맞춘 뒤 `loginId`, `name`, `email` 중 하나라도 그 문자열을 연속으로 포함하면 일치한다. 검색어의 `%`와 `_`는 와일드카드로 해석하지 않는다.
