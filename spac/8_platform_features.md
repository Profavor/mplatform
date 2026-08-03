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

## 8.2 다국어 및 개인화 타임존 지원
- **UI 및 데이터 다국어:** UI 메시지 및 `Domain.name`, `ClassificationNode.name`, `FieldDefinition.name`, `FieldDefinition.hint` 등 메타데이터가 `JSONB` 다국어 Map(`ko`, `en` 등)으로 저장 및 렌더링된다. 프론트엔드 및 서버 요청 시 `Accept-Language` 헤더가 쿠키/스토어 기반으로 자동 주입되어 다국어를 지원한다.
- **GMT 타임존 보정:** 사용자의 개인화 타임존 설정 쿠키를 참조하여 백엔드의 `LocalDateTime` 직렬화 및 프론트엔드 일시 시각화 시 현지 시각 지차가 정밀 보정된다.

---

## 8.3 보안 컴플라이언스 및 개인정보 마스킹 (Data Masking)
- **정규식 동적 마스킹:** 화면에 노출되는 개인정보/민감정보는 설정된 `maskingPattern`에 따라 클라이언트/서버에서 안전하게 마스킹 처리된다.
- **민감 데이터 감사(Audit):** 마스킹을 해제하고 원본 데이터를 확인하는 행위는 반드시 '열람 사유'를 포함하여 `SensitiveDataAccessLog`에 추적 가능하도록 강제 기록된다.
