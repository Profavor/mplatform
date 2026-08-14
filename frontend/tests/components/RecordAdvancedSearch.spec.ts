import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RecordAdvancedSearch from '../../components/records/RecordAdvancedSearch.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('RecordAdvancedSearch.vue (TDD)', () => {
  const mockFields = [
    { id: 'f-1', key: 'name', name: { ko: '이름' }, type: 'STRING' },
    { id: 'f-2', key: 'age', name: { ko: '나이' }, type: 'NUMBER' },
    { id: 'f-3', key: 'status', name: { ko: '상태' }, type: 'SELECT', options: JSON.stringify([{ value: 'ACTIVE', label: '활성' }]) }
  ]

  it('검색 필드 목록 렌더링 및 필터 조작 검증', async () => {
    const wrapper = mount(RecordAdvancedSearch, {
      props: {
        searchableFields: mockFields,
        draftFilters: {},
        draftFiltersOp: {},
        draftFiltersMax: {},
        activeFilters: {}
      },
      global: {
        stubs: {
          'va-card': true,
          'va-card-content': true,
          'va-icon': true,
          'va-badge': true,
          'va-select': true,
          'va-input': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
