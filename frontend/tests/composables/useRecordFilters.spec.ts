import { describe, it, expect, beforeEach } from 'vitest'
import { useRecordFilters } from '~/composables/useRecordFilters'

describe('useRecordFilters', () => {
  let filters: ReturnType<typeof useRecordFilters>

  beforeEach(() => {
    filters = useRecordFilters()
  })

  it('초기 필터 상태가 정상적으로 설정되어야 한다', () => {
    expect(filters.searchQuery.value).toBe('')
    expect(filters.activeFilters.value).toEqual({})
    expect(filters.advancedFilters.value).toEqual([])
    expect(filters.activePresetId.value).toBeNull()
  })

  it('setFilter 및 removeFilter가 정상 작동해야 한다', () => {
    filters.setFilter('status', 'ACTIVE')
    expect(filters.activeFilters.value['status']).toBe('ACTIVE')

    filters.removeFilter('status')
    expect(filters.activeFilters.value['status']).toBeUndefined()
  })

  it('resetFilters 호출 시 모든 필터 조건이 초기화되어야 한다', () => {
    filters.searchQuery.value = '검색어'
    filters.setFilter('category', 'A')
    filters.advancedFilters.value = [{ field: 'name', operator: 'CONTAINS', value: '테스트' }]
    filters.activePresetId.value = 'preset-1'

    filters.resetFilters()

    expect(filters.searchQuery.value).toBe('')
    expect(filters.activeFilters.value).toEqual({})
    expect(filters.advancedFilters.value).toEqual([])
    expect(filters.activePresetId.value).toBeNull()
  })

  it('buildFilterParams가 올바른 쿼리 파라미터 객체를 반환해야 한다', () => {
    filters.searchQuery.value = '홍길동'
    filters.setFilter('status', 'ACTIVE')
    filters.setFilter('type', 'USER')

    const params = filters.buildFilterParams()
    expect(params.search).toBe('홍길동')
    expect(params['filter_status']).toBe('ACTIVE')
    expect(params['filter_type']).toBe('USER')
  })

  it('필터 프리셋 저장 및 적용이 정상 작동해야 한다', () => {
    filters.searchQuery.value = '프리셋 검색'
    filters.setFilter('dept', 'DEV')

    const preset = filters.saveFilterPreset('개발팀 프리셋')
    expect(preset.name).toBe('개발팀 프리셋')
    expect(filters.filterPresets.value.length).toBe(1)

    filters.resetFilters()
    expect(filters.searchQuery.value).toBe('')

    filters.applyFilterPreset(preset.id)
    expect(filters.searchQuery.value).toBe('프리셋 검색')
    expect(filters.activeFilters.value['dept']).toBe('DEV')
    expect(filters.activePresetId.value).toBe(preset.id)
  })
})
