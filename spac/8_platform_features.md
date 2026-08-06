# 8. 플랫폼 성능 & 캐싱 명세

## 8.1 EffectiveFields 캐싱 및 무효화 전략
- **캐싱 대상:** `FieldDefinitionService.getEffectiveFields()` (노드 상속 트리 수집 계산)
- **적용 메커니즘:** Spring Cache (`@Cacheable("effectiveFields")`)
- **이중 캐시 스토리지 (Hybrid Cache Infrastructure):**
  - **Primary:** Redis (`RedisCacheConfig`)
  - **Fallback:** In-Memory ConcurrentMap (`LocalCacheConfig`) — Redis 서버 미설치 또는 연결 불가 시 자동으로 애플리케이션 내 로컬 메모리 캐시로 전환된다.
- **캐시 무효화 (Cache Eviction):**
  - 스키마 필드/노드의 변경, 스키마 승인 완료 시 `@CacheEvict(value = "effectiveFields", allEntries = true)`를 통해 캐시를 실시간으로 초기화하여 데이터 일관성을 유지한다.

---

## 8.2 다국어 및 개인화 타임존 지원 (Zero-Fallback & Timezone Safe)
- **Zero-Fallback UI 및 메타데이터 다국어:** UI 메시지 및 `Domain.name`, `ClassificationNode.name`, `FieldDefinition.name` 등 메타데이터가 `JSONB` 다국어 Map(`ko`, `en` 등)으로 저장 및 렌더링된다. 프론트엔드 컴포넌트는 소스 내 하드코딩된 폴백 텍스트 사용이 엄격히 차단되며 100% 로케일 사전에 기반해 동적 매칭된다.
- **GMT 타임존 보정 및 방어 직렬화:** 사용자의 개인화 타임존 설정 쿠키를 참조하며, Spring Boot의 `LocalDateTime` JSON 직렬화 시 타임존 오프셋(`+00:00`)이 생략되는 Edge-case를 완치하기 위해 프론트엔드 단에서 `parseDate` 헬퍼 함수 및 `useTimezoneDate()` 컴포저블을 필수 거치도록 설계되었다.

---

## 8.3 보안 컴플라이언스 및 개인정보 마스킹 (Data Masking)
- **정규식 동적 마스킹:** 화면에 노출되는 개인정보/민감정보는 설정된 `maskingPattern`에 따라 클라이언트/서버에서 안전하게 마스킹 처리된다.
- **민감 데이터 감사(Audit):** 마스킹을 해제하고 원본 데이터를 확인하는 행위는 반드시 '열람 사유'를 포함하여 `SensitiveDataAccessLog`에 추적 가능하도록 강제 기록된다.

---

## 8.4 TDD 및 정적 빌드 검증 파이프라인 (Test & Static Verify)
- **결함 제로지향 파이프라인:** 단 하나의 기능 변경이나 리팩터링도 화면 및 데이터 로직의 붕괴를 초래하지 않도록, 프론트엔드와 백엔드 모두 TDD 기반 개발 원칙을 준수한다.
- **Nuxt AST 정적 컴파일 결합 검증:**
  - 프론트엔드 유닛/컴포넌트 테스트 스위트 91개(`Vitest`)와 함께, `npm test` 구동 시 **`npm run build`(Nuxt Node 템플릿 컴파일 및 번들 정적 분석)를 동시 진행**한다.
  - 이로써 Vue AST 템플릿의 문법 에러, 잘못된 프로퍼티 조작, 브라우저 API(`indexedDB` 등)의 SSR 런타임 누출을 사전에 완벽히 솎아낸다.
- **대용량 AG-Grid 서버 사이드 페이징 Ready:**
  - 그리드 구성 시 v32+ 최신 구문(문자열 기반 속성 철폐 및 객체 기반 전면 도입)을 적용하며, 대용량 데이터를 원활히 소화하도록 서버 사이드 페이징 처리를 고려한 표준 컴포저블(`useAgGridTheme` 등)을 연동한다.

---

## 8.5 대화형 메신저 및 확장 협업 도구 (In-App Collaboration Workspace)
- **8방향 자유 드래그/리사이징 메신저:**
  - 실시간 채팅 및 알림을 담당하는 인앱 메신저(`InAppMessenger.vue`)는 화면 제약 없는 자유 배치와 8방향 리사이즈 핸들러, 참여자 프로필, 파일 첨부 기능을 제공한다.
- **실시간 메시지 번역:**
  - 다국어 글로벌 환경의 스튜어드 간 소통을 위해 메시지 송수신 시 원클릭 번역 및 번역 토글(`translateMessage`, `hideTranslation`)을 기본 탑재한다.
- **스프레드시트 및 테이블 프리뷰어:**
  - Excel 대량 파일이나 복잡한 데이터셋을 업로드하기 전 즉시 가시화해볼 수 있는 대화형 데이터 뷰어 모달(`ExcelPreviewModal`, `TableDataViewerModal`) 및 유튜브 계정 연동 DJ 방송 제어판 기능까지 지원한다.
