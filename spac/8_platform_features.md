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
- **UI 및 데이터 다국어:** UI 메시지 및 `Domain.name`, `ClassificationNode.name`, `FieldDefinition.name` 등 메타데이터가 `JSONB` 다국어 Map(`ko`, `en` 등)으로 저장 및 렌더링된다.
- **GMT 타임존 보정:** 사용자의 개인화 타임존 설정 쿠키를 참조하여 백엔드의 `LocalDateTime` 직렬화 및 프론트엔드 일시 시각화 시 현지 시각 지차가 정밀 보정된다.
