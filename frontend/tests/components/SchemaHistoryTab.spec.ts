import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SchemaHistoryTab from '../../components/schema/SchemaHistoryTab.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (d: string) => d || '2026-09-06 12:00:00'
  })
}))

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    fetchUserMap: vi.fn(),
    getUserName: (id: string) => id === 'u-1' ? '홍길동' : id
  })
}))

vi.mock('~/stores/useCodeStore', () => ({
  useCodeStore: () => ({
    preloadGroups: vi.fn().mockResolvedValue([]),
    getCodeName: (grp: string, code: string, fallback: string) => fallback || code
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: (key: string) => key === 'schema_prop_name',
    locale: { value: 'ko' }
  })
}))

describe('SchemaHistoryTab.vue', () => {
  const dummyHistory = {
    content: [
      {
        id: 'h-1',
        targetType: 'FIELD',
        targetId: 'f-12345678-abcd',
        action: 'UPDATE',
        changedBy: 'u-1',
        changedAt: '2026-09-06T12:00:00Z',
        beforeData: JSON.stringify({ key: 'biz_no', name: { ko: '사업자번호' }, required: false }),
        afterData: JSON.stringify({ key: 'biz_no', name: { ko: '사업자번호' }, required: true })
      },
      {
        id: 'h-2',
        targetType: 'NODE',
        targetId: 'n-99998888-cdef',
        action: 'CREATE',
        changedBy: 'u-1',
        changedAt: '2026-09-05T12:00:00Z',
        beforeData: null,
        afterData: JSON.stringify({ name: '고객분류', code: 'CUST' })
      }
    ],
    totalPages: 1
  }

  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue(dummyHistory)
  })

  it('renders history table with search toolbar and target_name column', async () => {
    const wrapper = mount(SchemaHistoryTab, {
      props: {
        domainId: 'd-123'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-input': true,
          'va-select': true,
          'va-button': true,
          'va-badge': true,
          'va-chip': true,
          'va-pagination': true,
          'va-progress-circle': true
        }
      }
    })

    await wrapper.vm.$nextTick()
    await new Promise(r => setTimeout(r, 10))

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('schema_history.title')
    expect(wrapper.text()).toContain('schema_history_tab.target_name')

    // Verify row target name rendered properly
    expect(wrapper.text()).toContain('biz_no (사업자번호)')
    expect(wrapper.text()).toContain('고객분류')
    expect(wrapper.text()).toContain('홍길동')
  })
})
