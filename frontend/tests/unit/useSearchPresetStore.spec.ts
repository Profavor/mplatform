import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useSearchPresetStore } from '~/stores/useSearchPresetStore'

describe('useSearchPresetStore (TDD)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    if (typeof localStorage !== 'undefined') {
      localStorage.clear()
    }
  })

  it('새로운 검색 필터 프리셋을 추가하고 도메인별로 조회할 수 있다', () => {
    const store = useSearchPresetStore()
    const domainId = 'domain-123'

    store.savePreset(domainId, {
      name: 'VIP 고객 검색',
      filters: [{ field: 'GRADE', operator: 'EQUALS', value: 'VIP' }]
    })

    const presets = store.getPresetsByDomain(domainId)
    expect(presets).toHaveLength(1)
    expect(presets[0].name).toBe('VIP 고객 검색')
    expect(presets[0].filters[0].value).toBe('VIP')
  })

  it('프리셋 ID로 특정 프리셋을 삭제할 수 있다', () => {
    const store = useSearchPresetStore()
    const domainId = 'domain-123'

    const saved = store.savePreset(domainId, {
      name: '임시 프리셋',
      filters: []
    })

    expect(store.getPresetsByDomain(domainId)).toHaveLength(1)

    store.deletePreset(saved.id)
    expect(store.getPresetsByDomain(domainId)).toHaveLength(0)
  })
})
