# 🌐 MDM Enterprise Web Console (Frontend)

엔터프라이즈 마스터 데이터 관리(MDM) 플랫폼의 공식 웹 프론트엔드 콘솔(`classification-front`)입니다.
Nuxt 3 및 Vue 3 기반으로 구축되었으며, 대용량 엔터프라이즈 마스터 데이터의 실시간 시각화, 다축 분류, 데이터 품질(DQ) 자율 정제, 결재 워크플로우, 데이터 계보(Lineage) 추적 및 B2B 온보딩 퍼널을 제공합니다.

---

## 🛠 기술 스택 (Tech Stack)

| 영역 | 기술 / 라이브러리 | 버전 / 세부 사양 |
|---|---|---|
| **Framework** | **Nuxt 3 + Vue 3** | Nuxt `^3.21.11`, Vue `3.5.x`, SSR & SPA 하이브리드 모드 |
| **Language** | **TypeScript** | TypeScript `^5.9.3`, 엄격 타입 검사 |
| **UI Library** | **Vuestic UI** | 반응형 모던 컴포넌트, 맞춤 CSS 토큰 시스템 |
| **Grid Engine** | **AG-Grid Vue3** | AG-Grid Enterprise `^34.3.1` (가상 스크롤, 서버사이드 페이징/정렬/필터) |
| **Data Viz** | **Apache ECharts** | DQ 시계열 트렌드, 리스크 레이더, 데이터 계보 파이프라인 그래프 |
| **State Management**| **Pinia** | 6개 전역 스토어 (`useAuthUser`, `useMenuStore`, `useChatStore` 등) |
| **Internationalization**| **@nuxtjs/i18n** | Zero-Fallback 다국어 지원, 런타임 언어 실시간 전환 |
| **Rich Text Editor**| **Tiptap Editor** | 결재 본문 서식 및 사내 공지 에디터 |
| **Real-time Comms**| **STOMP / SockJS** | 웹소켓 기반 실시간 인앱 메신저, 시스템 라디오, 결재 알림 |
| **Testing** | **Vitest + Vue Test Utils**| **243개** 단위/컴포넌트 테스트 스펙 완비 |

---

## 📊 규모 및 구성 현황 (Baseline Metrics)

- **버전**: `1.5.76` (`package.json`)
- **라우트 페이지**: 28개 (`pages/`)
- **재사용 UI 컴포넌트**: 170개 (`components/`)
- **비즈니스 로직 훅**: 28개 (`composables/`)
- **전역 미들웨어 & 플러그인**: 8개 (`middleware/`, `plugins/`)
- **단위/컴포넌트 테스트**: 243개 (`*.spec.ts`)
- **프론트엔드 코드 라인 수**: 약 87,100줄

---

## 🔑 주요 화면 및 기능 (Core Features)

1. **소개 랜딩 페이지 (`pages/index.vue`)**:
   - 비로그인 방문자 대상 인터랙티브 소개 페이지 (Why MDM, 핵심 아키텍처, 고객 사례, FAQ).
   - 로그인 상태 감지 시 업무 대시보드(`/dashboard`)로 자동 라우팅.
2. **B2B 셀프 온보딩 퍼널 (`pages/register.vue`)**:
   - 회원가입 및 업종별 맞춤 도메인 템플릿(부동산 임대차, 고객, 상품 등) 선택 및 자동 프로비저닝 연동.
3. **MDM ROI 계산기 (`pages/roi-calculator.vue`)**:
   - 마스터 데이터 오류 비용 및 인건비 절감액을 산출하는 인터랙티브 리드마그넷 위젯.
4. **2FA / OTP 다중인증 모달 (`components/TwoFactorVerifyModal.vue`)**:
   - TOTP 구글 OTP, 이메일 OTP, 일회용 긴급 백업코드 입력 지원.
5. **대용량 마스터 레코드 탐색기 (`pages/records/index.vue`)**:
   - AG-Grid Enterprise 기반 수만 건 데이터의 무중단 가상 스크롤 및 서버사이드 정렬/필터링.
   - 단일 클릭 상세 서랍(Drawer) 및 더블 클릭 모달 뷰.
6. **부동산 임대차 (`LEASE_CONTRACT`) 리스크 대시보드 (`components/dashboard/LeaseRiskWidget.vue`)**:
   - 계약 만기 임박(D-30, D-7) 알림 및 월세 연체 상태 실시간 관제.
7. **데이터 계보 (Data Lineage) 5단계 파이프라인 (`components/records/PipelineLineageModal.vue`)**:
   - Ingestion → DQ 검증 → 정제 → Golden Record → Target 전 구간 시각화 및 노드 상세 Diff.
8. **RBAC 세분화 & 컬럼 수준 마스킹 관리 (`pages/admin/permissions.vue`)**:
   - 도메인/노드별 데이터 접근 스코프 제한 및 역할별 컬럼 동적 마스킹(`***`) 설정.
9. **스키마 변경 하위호환성 사전 시뮬레이션 (`pages/domains/schema-history.vue`)**:
   - Breaking Change 사전 감지 및 기존 레코드 대상 영향도 시뮬레이션 리포트.
10. **8방향 리사이즈 실시간 협업 메신저 (`components/chat/FloatingChat.vue`)**:
    - 웹소켓 STOMP 실시간 채팅, 원클릭 다국어 번역, 엑셀 뷰어, 시스템 라디오.

---

## 🧪 테스트 및 품질 검증 (TDD Pipeline)

본 프로젝트는 사이드 이펙트 방지를 위해 프론트엔드 TDD 규약을 엄격히 준수합니다.

```bash
# 1. 의존성 설치
npm install

# 2. 로컬 개발 서버 구동 (포트: 3000)
npm run dev

# 3. Vitest 단위 및 컴포넌트 테스트 전체 실행
npm run test:unit

# 4. 종합 품질 검증 (Vitest 243개 테스트 + Nuxt 정적 컴파일 AST 빌드 검증)
npm test
```

---

## 🚀 배포 절차 (Fast-Track Deployment)

`.agents/AGENTS.md` 지침에 따라 배포 시점에만 `package.json`과 `k8s/31-frontend.yaml` 버전을 상향한 후 전용 스크립트를 사용합니다.

```bash
# 호스트 사전 빌드 산출물 복사 기반 초고속 (~20초) 배포 파이프라인
../deploy-frontend.sh
```
스크립트는 다음 단계를 자동으로 수행합니다:
1. 호스트 머신에서 `npm run build` 실행
2. Docker 이미지 빌드 (`profavor2/mplatform-frontend:<TAG>`)
3. Minikube 로컬 레지스트리 로드
4. K8s 매니페스트 적용 및 롤아웃 재시작 (`kubectl rollout restart deployment frontend -n mdm-system`)
5. 롤아웃 성공 여부 자동 확인

