# Rules

- Always write implementation plans (implementation_plan.md) and tasks in Korean ("계획서는 한글로 표시해줘. 앞으로 쭈욱.").
- DB 작업 시 `TRUNCATE TABLE` 명령어를 절대로 사용하지 말 것. 기존 데이터를 날리지 말고 문제가 있는 레코드/도메인을 명시적으로 조회하여 개별 삭제(Delete)할 것.
- 파일 내용 변경(Multi-replace 등) 후에는 괄호 `}`나 태그가 잘못 남겨지지 않았는지 반드시 Diff를 재차 확인하고, 필요 시 빌드/린트 스크립트를 돌려 Syntax 에러가 없는지 검증할 것.
- 추후 AG-Grid로 화면 개발 시, 대량의 데이터 처리를 위해 반드시 서버 사이드 페이징(Server-Side Pagination)이 가능하도록 개발할 것.
- 기능 개발 및 수정 시 사이드 이펙트 방지를 위해 반드시 **프론트엔드와 백엔드 모두 TDD(Test-Driven Development) 기반으로 개발**할 것. 백엔드는 JUnit 단위 테스트(Unit Test), 프론트엔드는 단위/컴포넌트 테스트 스펙을 먼저 작성하여 검증을 통과한 후에만 기능을 반영할 것.
- 날짜/시간 관련 기능 개발 및 수정 시, 반드시 사용자가 정의한 개인화 타임존(Timezone) 설정 쿠키를 조회하여 다국어 및 현지 시각(GMT 시차)이 정상 적용되도록 개발할 것. (Spring Boot의 LocalDateTime 직렬화 시 오프셋 누락 건에 대비하여 parseDate 등의 방어 헬퍼 함수를 필수 사용)
- **하드코딩은 절대로 하지 말 것. 예외적으로 꼭 필요한 경우에는 반드시 사용자에게 사전에 컨펌(Confirm)을 받은 후에만 진행할 것.**
- 프론트엔드 다국어(Multilingual) 처리는 반드시 **vue-i18n (`$t(...)` 또는 `useI18n()`)을 통해서만 개발 및 구현**할 것. 화면 레이블 및 텍스트 하드코딩을 금지하고 i18n 로케일 정의를 통해 적용할 것.

