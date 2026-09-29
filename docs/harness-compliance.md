# State-Graph Harness와 현재 저장소 비교

비교 대상은 `State-Graph Agent Harness Engineering Pipeline (PoC v3.0)`이다.
현재 저장소는 Spec Kit(`specify → clarify → plan → tasks → implement → converge`)으로 사용자 관리 PoC를 진행한 상태다.

두 문서의 목적이 다르다. 하네스 문서는 LLM 한 번의 작업을 상태 그래프, 도구 권한, 비용 한도로 묶는다. 이 저장소의 헌법은 요구·Spec·설계·구현·테스트의 추적성을 묶는다. 아래 판정은 하네스 문서의 통제가 이 저장소에 얼마나 들어와 있는지를 본다.

판정은 세 단계다.

- **준수**: 같은 통제가 규칙 또는 실제 실행에 있다.
- **부분**: 취지는 같고, 전이 조건이나 강제 수단은 없다.
- **미적용**: 해당 통제가 저장소에 없다.

## 총평

산출물을 단계마다 남기고, 구현 전에 사람이 스펙과 계획을 승인하는 흐름은 들어와 있다. 테스트도 구현 태스크보다 앞에 둔다.

상태 그래프의 엣지 조건, 읽기 전용 도구 잠금, 신규/기존 파일 라우팅, 토큰·비용 예산, Docker 샌드박스, Git Worktree 롤백은 적용되어 있지 않다. 에이전트는 Cursor 세션 안에서 읽기·쓰기·셸을 단계와 무관하게 사용할 수 있다.

## 항목별 대조

| 하네스 통제 | 판정 | 이 저장소의 대응 |
| --- | --- | --- |
| Phase 0. 런타임(자동 파이프라인 / IDE)과 모델 정책을 질문하고 `.agent/config.yaml`에 고정 | 미적용 | `.specify/init-options.json`에 `integration: cursor-agent`만 있다. `.agent/config.yaml`은 없다. 모델 티어, 태스크당 토큰, 비용, 재시도 상한이 없다. |
| Reasoning Before Action. 수정 전에 제약과 영향 분석 | 부분 | 헌법 I은 Spec 작성 전 구현을 금지한다. `plan`이 `research.md`에 기술 결정을 남긴다. 분석 페이즈의 도구가 읽기 전용으로 잠기지는 않는다. |
| Phase 1 산출물만 다음 단계로 전달 (Context Compaction) | 부분 | 명령마다 `spec.md`, `plan.md`, `tasks.md`, `contracts/`를 읽는다. 같은 대화의 이전 이력도 함께 남는다. 검증된 산출물만 주입하는 노드는 없다. |
| Phase 2 계획 후 HITL 승인 전 구현 금지 | 준수 | 구현은 사용자가 `/speckit-implement`를 실행할 때만 시작한다. `workflow.yml`에도 spec 검토, plan 검토 게이트가 있다. 이번 작업은 그 엔진 대신 명령을 하나씩 실행했다. |
| Red. 실패하는 테스트를 먼저 쓰고, 통과하는 테스트는 반려 | 부분 | `speckit-implement`는 테스트 태스크를 구현보다 먼저 실행하라고 한다. 기능 001–003 태스크도 그 순서다. 테스트가 구현 전에 실제로 실패했는지 확인하는 게이트는 없다. |
| Green. 린트 통과와 테스트 통과만으로 구현 종료 | 부분 | 구현 끝에 `mvn test`로 스위트를 통과시켰다. 린터·타입체커 0 error를 전이 조건으로 두는 단계는 없다. Checkstyle, Spotless 설정도 없다. |
| 신규 파일은 `create`, 기존 파일은 unified diff만 | 미적용 | 도구 화이트리스트가 없다. 새 파일은 전체 작성, 기존 파일은 부분 수정으로 작업했지만, 기존 파일 전체 덮어쓰기를 막는 규칙은 없다. |
| Light-tier 린트 수정 루프, 최대 3회, 예산 초과 시 중단 | 미적용 | 실패 시 같은 세션에서 고친다. 재시도 횟수와 비용 한도가 없다. |
| 실패 또는 예산 초과 시 Worktree 폐기와 롤백 | 미적용 | 작업은 `main` 작업 트리에서 했다. `git worktree` 스냅샷이 없다. |
| Docker에서만 린트·테스트 실행 | 미적용 | 테스트는 호스트의 Maven과 H2 인메모리 DB로 돈다. Dockerfile이 없다. |
| Phase 4. diff로 PR 본문 작성 후 머지 준비 | 부분 | 구현 후 변경을 커밋했다. PR 본문 작성과 머지는 하지 않았다. `gh`도 없다. |
| 계층형 거버넌스 | 부분 | `.specify/memory/constitution.md`가 도메인 원칙(Spec 우선, 추적성, 검증, 비밀번호, 권한, Soft Delete)을 고정한다. 모델·비용·런타임 정책은 그 범위 밖이다. |

## 이미 겹치는 동작

다음은 하네스와 이름이 달라도 같은 효과를 낸다.

1. **구현 착수 게이트.** Spec, clarify 답변, plan, tasks가 끝난 뒤에만 코드를 고친다. 체크리스트에 미체크가 있으면 implement가 사용자 확인을 기다린다.
2. **정형 산출물.** 기능마다 `spec.md`, `plan.md`, `research.md`, `data-model.md`, `contracts/`, `tasks.md`, `quickstart.md`가 남는다. 테스트 메서드 이름은 `fr00N`, `s002Fr00N`, `s003Fr00N`으로 요구 항목을 가리킨다.
3. **테스트 선행 선언.** 태스크 순서가 Red에 해당한다. 완료 확인은 전체 `mvn test` 통과다.
4. **사람 개입.** clarify는 한 번에 질문을 끝내고 답을 Spec에 기록한다. 명령 사이에 사용자가 다음 단계를 직접 호출한다.
5. **부작용이 좁은 검증.** H2는 테스트 프로파일에서 인메모리라 호스트 DB를 바꾸지 않는다. 이건 Docker 격리와 같은 수준은 아니다.

## 적용할 수 있는 부분

Spec Kit 흐름을 교체하지 않고 넣을 수 있는 것만 골랐다. 우선순위는 이 PoC의 목적(SI/SM에서 Spec 추적)에 맞춘 것이다.

### 1. Red 확인을 한 번만 실행

새 테스트 파일을 넣은 직후, 구현 전에 그 테스트만 실행한다. 통과하면 테스트를 고치고 다시 실패를 확인한 다음 구현으로 넘어간다. 하네스의 `Red_Verify`와 같다. 그래프 엔진 없이 `mvn test -Dtest=...` 한 번으로 된다.

### 2. 단계 입력을 산출물 경로로 한정

다음 Spec Kit 명령을 시작할 때 이전 대화 요약 대신 해당 기능 디렉터리의 파일만 읽는다. 이미 명령이 그렇게 설계돼 있고, 긴 세션에서는 대화가 함께 남는다. 기능 하나를 명령마다 새 세션으로 두면 하네스의 Context Compaction에 가까워진다.

### 3. 재시도 상한을 작업 규칙으로 고정

같은 테스트 실패를 세 번 고친 뒤에도 실패하면 구현을 멈추고 사람에게 넘긴다. 예산 필드는 현재 Cursor 세션에서 읽을 수 없으므로 토큰·달러 한도는 넣지 않는다. 횟수 한도만 헌법 또는 구현 스킬 메모에 적어도 하네스 Escalation의 핵심은 재현된다.

### 4. 기존 파일은 부분 수정만

새 파일은 전체 작성, 이미 있는 파일은 구간 치환만 한다. 구현 중에 대체로 그렇게 했다. 규칙으로 남기면 하네스의 `create_file` / `apply_patch` 분기와 같다. 별도 도구를 만들 필요는 없다.

### 5. 런타임 선택을 한 줄로 기록

이 저장소의 런타임은 IDE 연동이다. 모델은 세션 모델을 전 단계에 쓴다. `.agent/config.yaml`의 티어 표 전체를 만들기보다, `init-options.json` 옆에 그 두 문장만 적어 두면 Phase 0의 결정이 남는다. 자동 모델 전환은 이 세션 구조에서 실행 수단이 없다.

### 6. 실험만 Worktree로 분리

기능 구현 자체는 지금처럼 하나의 작업 트리로 충분하다. 하네스나 빌드 방식을 시험할 때만 `git worktree`로 분리하면, 실패 시 작업 트리를 지우면 된다. 매 테스트마다 Docker를 띄우는 구성은 이 PoC의 검증 대상이 아니다. 테스트 격리는 H2 인메모리로 이미 맞다.

## 지금 넣지 않을 부분

- **페이즈별 모델 자동 전환.** 분석·코딩·린트를 다른 모델에 배정하는 라우터는 현재 통합(`cursor-agent`)에 없다.
- **태스크당 토큰·달러 예산.** 세션이 그 수치를 게이트로 노출하지 않는다.
- **Docker 일회용 테스트 런너.** 빌드와 테스트는 로컬 JDK 21과 Maven이다. 격리 수준을 올리려면 별도 인프라 스펙이 먼저다.
- **`workflow.yml` 전 구간 자동 실행.** 정의는 specify, plan, tasks, implement와 두 개의 승인 게이트다. 실제 진행은 clarify, checklist, analyze, converge를 사이에 두고 사람이 단계를 끊었다. 자동 체인은 그 HITL보다 짧다.

## 대응표

하네스 페이즈를 이 저장소 명령에 포개면 아래와 같다.

| 하네스 | 이 저장소 |
| --- | --- |
| Phase 0 Setup | 초기화 시 `init-options.json`. 이후 재질문 없음 |
| Phase 1 Analysis | `/speckit-specify`, `/speckit-clarify`, plan의 `research.md` |
| Phase 2 Planning + HITL | `/speckit-plan`, `/speckit-tasks`, 사용자의 다음 명령 |
| Phase 3A Red | tasks의 테스트 항목, implement의 테스트 선행 |
| Phase 3B Green | 서비스·컨트롤러·화면 구현, `mvn test` |
| Phase 3C Lint | 없음 |
| Phase 3D Refactor | 태스크의 polish. 회귀는 전체 테스트 |
| Phase 4 Summary | 수동 커밋. PR 본문 자동화 없음 |
| 거버넌스 | `constitution.md` (도메인 원칙) |
