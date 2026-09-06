# 🤝 Contributing to Enterprise MDM Platform

엔터프라이즈 MDM 플랫폼(`mplatform`) 프로젝트에 기여해 주셔서 감사합니다.
본 프로젝트는 데이터 거버넌스 및 금융/엔터프라이즈 마스터 데이터의 무결성을 최우선으로 하며, 사이드 이펙트 방지를 위해 엄격한 개발 및 배포 표준 지침을 준수해야 합니다.

---

## 📋 핵심 개발 원칙 (Core Engineering Rules)

모든 기여자 및 개발자는 아래 9대 원칙을 예외 없이 반드시 준수해야 합니다.

### 1. 🇰🇷 한국어 계획서 작성 (Korean Implementation Plan)
- 기능 구현 또는 대규모 리팩토링 착수 전, 반드시 계획서(`implementation_plan.md`) 및 태스크를 한국어로 작성하여 승인을 받습니다.

### 2. 🚫 DB 작업 시 `TRUNCATE TABLE` 절대 금지
- 데이터베이스 작업 시 `TRUNCATE TABLE` 명령어를 절대로 사용하지 않습니다.
- 기존 데이터를 일괄 삭제하지 말고, 문제가 있는 레코드/도메인을 명시적으로 조회하여 개별 삭제(`DELETE FROM ... WHERE ...`)합니다.

### 3. 🧪 프론트엔드 & 백엔드 TDD (Test-Driven Development) 필수
- 사이드 이펙트 방지를 위해 반드시 **프론트엔드와 백엔드 모두 TDD 기반으로 개발**합니다.
  - **백엔드**: JUnit 5 단위 테스트(`*Test.java`) 작성 및 전체 통과 검증 (`mvn test`)
  - **프론트엔드**: Vitest 컴포넌트/단위 테스트(`*.spec.ts`) 및 정적 빌드 검증 (`npm test`)

### 4. 🌐 하드코딩 절대 금지 & Zero-Fallback 다국어 원칙
- **소스 코드에 임의의 문자열, 필드명, 라벨 하드코딩을 엄격히 금지**합니다.
- 사용자가 예시나 힌트(특정 라벨, 용어, 필드명)를 제시하더라도 문자열 fallback으로 넣지 않습니다.
- 모든 메타데이터, 필드명, 연동 채널, DQ 검칙은 100% DB 스키마(`FieldDefinition` 등) 및 `vue-i18n`(`$t(...)` 또는 `useI18n()`) 정의를 통해서만 동적으로 조회/조립하여 반영합니다.

### 5. 🕒 개인화 타임존(Timezone) 및 날짜 오프셋 방어
- 날짜/시간 관련 기능 개발 시, 반드시 사용자가 정의한 개인화 타임존 설정 쿠키(`user_timezone`)를 조회하여 다국어 및 현지 시각(GMT 시차)이 정상 적용되도록 개발합니다.
- Spring Boot의 `LocalDateTime` 직렬화 시 오프셋 누락 건에 대비하여 프론트엔드의 `parseDate` 등 방어 헬퍼 함수를 필수로 사용합니다.

### 6. 🔒 화면단 무의미한 Raw UUID 노출 금지
- 프론트엔드 및 사용자 화면단에는 절대로 무의미한 raw UUID(예: `340a0917-af0b-4d13-a1ce-479d4b2e2ca7`)를 그대로 표기하지 않습니다.
- 화면 노출 시 반드시 식별 코드(예: `REC-340a0917`)나 사용자/시스템 명칭으로 치환하여 표출합니다.

### 7. 📊 AG-Grid 대용량 서버사이드 페이징
- AG-Grid로 화면 개발 시, 대량의 데이터 처리를 위해 반드시 가상 스크롤(Virtual Scrolling) 및 서버 사이드 페이징(Server-Side Pagination/Sorting/Filtering)이 가능하도록 개발합니다.

### 8. 🔍 신중한 코드 Diff 검증
- 파일 내용 변경(Multi-replace 등) 후에는 괄호 `}`나 태그가 잘못 남겨지지 않았는지 반드시 Diff를 재차 확인하고, 빌드/린트 스크립트를 실행하여 문법 오류가 없는지 검증합니다.

### 9. 🚀 독립적 버전 관리 및 배포 시점 상향 원칙
- **패키지 버전 상향(Bump Version)은 오직 배포(Deploy) 시점에만 수행**합니다. 기능 개발 커밋 단계에서는 버전을 올리지 않습니다.
- 프론트엔드(`frontend/package.json`)와 백엔드(`backend/pom.xml`)는 독립적인 서비스이므로 변경사항이 발생한 대상 모듈의 버전만 개별적으로 올립니다.

---

## 🚀 배포 표준 지침 (Deployment Guidelines)

### 1. 프론트엔드 단독 배포 (초고속 ~20초)
프론트엔드 변경 시 도커 내부에서 다시 빌드하지 않고 호스트 머신에서 사전 빌드한 산출물을 복사하여 초고속으로 배포합니다.
```bash
# 1. 버전 상향 (frontend/package.json, k8s/31-frontend.yaml)
# 2. 전용 스크립트 실행 (빌드 + 이미지 생성 + minikube 로드 + k8s 롤아웃)
./deploy-frontend.sh
```

### 2. 백엔드 단독 배포 (초고속 ~15초)
백엔드 변경 시 호스트 머신에서 사전 빌드한 JAR(`target/*.jar`)를 복사하여 초고속으로 배포합니다.
```bash
# 1. 버전 상향 (backend/pom.xml, k8s/30-backend.yaml)
# 2. 전용 스크립트 실행 (빌드 + 이미지 생성 + minikube 로드 + k8s 롤아웃)
./deploy-backend.sh
```

### 3. 전체 시스템 통합 배포
전체 서비스(인프라, 백엔드, 프론트엔드, 모바일, 인그레스 등)를 일괄 배포할 때만 `./deploy.sh`를 사용합니다.
```bash
./deploy.sh
```

---

## 🌿 Git Flow & Pull Request 절차

1. 기본 개발 브랜치인 `dev`에서 최신 커밋을 확인합니다.
2. 기능/버그 단위로 브랜치를 생성합니다 (`feat/...`, `fix/...`).
3. TDD 원칙에 따라 단위 테스트를 작성하고 기능을 구현합니다.
4. 로컬에서 테스트 전체 통과 확인:
   - Backend: `cd backend && mvn test`
   - Frontend: `cd frontend && npm test`
5. 커밋 메시지는 Conventional Commits 규약을 따릅니다 (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `build:`).
6. PR 생성 후 CI 파이프라인 검증 및 코드 리뷰를 거쳐 `dev`에 머지합니다.
7. 배포 완료 후 반드시 `kubectl get pods -n mdm-system`으로 파드의 Running 상태를 확인합니다.

---

## 📄 라이선스 (License)
본 프로젝트에 기여된 모든 코드는 [MIT License](./LICENSE) 라이선스에 동의한 것으로 간주됩니다.
