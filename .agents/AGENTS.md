# Rules

- Always write implementation plans (implementation_plan.md) and tasks in Korean ("계획서는 한글로 표시해줘. 앞으로 쭈욱.").
- DB 작업 시 `TRUNCATE TABLE` 명령어를 절대로 사용하지 말 것. 기존 데이터를 날리지 말고 문제가 있는 레코드/도메인을 명시적으로 조회하여 개별 삭제(Delete)할 것.
- 파일 내용 변경(Multi-replace 등) 후에는 괄호 `}`나 태그가 잘못 남겨지지 않았는지 반드시 Diff를 재차 확인하고, 필요 시 빌드/린트 스크립트를 돌려 Syntax 에러가 없는지 검증할 것.
- 추후 AG-Grid로 화면 개발 시, 대량의 데이터 처리를 위해 반드시 서버 사이드 페이징(Server-Side Pagination)이 가능하도록 개발할 것.
- 기능 개발 및 수정 시 사이드 이펙트 방지를 위해 반드시 **프론트엔드와 백엔드 모두 TDD(Test-Driven Development) 기반으로 개발**할 것. 백엔드는 JUnit 단위 테스트(Unit Test), 프론트엔드는 단위/컴포넌트 테스트 스펙을 먼저 작성하여 검증을 통과한 후에만 기능을 반영할 것.
- 날짜/시간 관련 기능 개발 및 수정 시, 반드시 사용자가 정의한 개인화 타임존(Timezone) 설정 쿠키를 조회하여 다국어 및 현지 시각(GMT 시차)이 정상 적용되도록 개발할 것. (Spring Boot의 LocalDateTime 직렬화 시 오프셋 누락 건에 대비하여 parseDate 등의 방어 헬퍼 함수를 필수 사용)
- **하드코딩은 절대로 하지 말 것. 예외적으로 꼭 필요한 경우에는 반드시 사용자에게 사전에 컨펌(Confirm)을 받은 후에만 진행할 것.**
- **사용자가 예시나 힌트(예: 특정 라벨, 용어, 필드명)를 제시하더라도 소스 코드(Java, TS, Vue 등)에 임의로 하드코딩하거나 문자열 fallback으로 넣는 행위를 절대로 금지할 것.** 모든 메타데이터, 필드명, 연동 채널, DQ 검칙, 메시지/헤더는 100% DB 스키마(`FieldDefinition` 등) 및 `vue-i18n` 정의를 통해서만 동적으로 조회/조립하여 반영할 것.
- 프론트엔드 다국어(Multilingual) 처리는 반드시 **vue-i18n (`$t(...)` 또는 `useI18n()`)을 통해서만 개발 및 구현**할 것. 화면 레이블 및 텍스트 하드코딩을 금지하고 i18n 로케일 정의를 통해 적용할 것.
- **프론트엔드 및 사용자 화면단에는 절대로 무의미한 raw UUID(예: 340a0917-af0b-4d13-a1ce-479d4b2e2ca7)를 그대로 표기하지 말 것.** 사용자에게 노출 시 반드시 식별 코드(예: REC-340a0917)나 사용자/시스템 명칭으로 치환하여 표출할 것.
- **배포(Kubernetes/도커 빌드 등) 시에는 반드시 변경사항이 있는 대상 모듈(프론트엔드 `frontend/package.json` 또는 백엔드 `backend/pom.xml`)의 패키지 버전만 개별적으로 올린 후(Bump Version), 해당 모듈만 빌드 및 배포를 진행할 것. (프론트엔드와 백엔드 버전은 각각 독립적으로 관리)**

---

# 🚀 배포 표준 지침 (Deployment Guidelines)

### 1. 버전 관리 원칙
- **독립적 버전 관리**: 프론트엔드(`frontend/package.json`)와 백엔드(`backend/pom.xml`)는 독립적인 서비스이므로 변경사항이 발생한 대상 모듈의 버전만 개별적으로 올린다(Bump Version).
- **사전 버전 상향 필수**: 배포 시에는 반드시 소스 코드의 패키지 버전 및 대상 `k8s/*.yaml` 매니페스트의 이미지 태그를 먼저 올린 후 빌드/배포를 실행한다.

### 2. 모듈별 배포 방법

#### ① 프론트엔드 단독 배포 (초고속 ~20초 파이프라인)
프론트엔드 변경 시 도커 내부에서 `npm ci`나 `npm run build`를 다시 돌리지 않고 호스트 머신에서 사전 빌드한 산출물을 복사하여 초고속으로 배포한다.
- **실행 방법**:
  ```bash
  # 1. 버전 상향 (frontend/package.json, k8s/31-frontend.yaml)
  # 2. 전용 스크립트 실행 (빌드 + 이미지 생성 + minikube 로드 + k8s 롤아웃)
  ./deploy-frontend.sh
  ```
- **수동 실행 절차**:
  ```bash
  cd frontend && npm run build
  docker build -t profavor2/mplatform-frontend:<TAG> .
  minikube image load profavor2/mplatform-frontend:<TAG>
  kubectl apply -f k8s/31-frontend.yaml
  kubectl rollout restart deployment frontend -n mdm-system
  kubectl rollout status deployment frontend -n mdm-system --timeout=60s
  ```

#### ② 백엔드 단독 배포 (초고속 ~15초 파이프라인)
백엔드 변경 시 도커 내부에서 `mvn package`를 다시 돌리지 않고 호스트 머신에서 사전 빌드한 JAR(`target/*.jar`)를 복사하여 초고속으로 배포한다.
- **실행 방법**:
  ```bash
  # 1. 버전 상향 (backend/pom.xml, k8s/30-backend.yaml)
  # 2. 전용 스크립트 실행 (빌드 + 이미지 생성 + minikube 로드 + k8s 롤아웃)
  ./deploy-backend.sh
  ```
- **수동 실행 절차**:
  ```bash
  cd backend && mvn clean package -DskipTests
  docker build -t profavor2/mplatform-backend:<TAG> .
  minikube image load profavor2/mplatform-backend:<TAG>
  kubectl apply -f k8s/30-backend.yaml
  kubectl rollout restart deployment backend -n mdm-system
  kubectl rollout status deployment backend -n mdm-system --timeout=60s
  ```

#### ③ 전체 시스템 통합 배포
전체 서비스(인프라, 백엔드, 프론트엔드, 모바일, 인그레스 등)를 일괄 배포할 때만 `./deploy.sh`를 사용한다.
```bash
./deploy.sh
```

### 3. 작업 및 배포 후 보고 원칙
- 배포가 완료되면 반드시 `kubectl get pods -n mdm-system`으로 파드의 Running 상태 및 롤아웃 완료 여부를 확인한 후 사용자에게 최종 보고한다.

