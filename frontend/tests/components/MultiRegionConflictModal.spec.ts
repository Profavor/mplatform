import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MultiRegionConflictModal from '../../components/admin/MultiRegionConflictModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('MultiRegionConflictModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          totalRegions: 3,
          activeConflicts: 0,
          autoResolvedCount: 2,
          summary: '충돌 100% 자율 해소 완료',
          conflicts: [
            {
              conflictId: 'CONF-01',
              domainCode: 'DOM-CUST',
              recordCode: 'REC-CUST-8812',
              regionA: 'KR_SEOUL',
              regionB: 'US_VIRGINIA',
              fieldKey: 'contact_phone',
              valueA: '010-9988-7766',
              valueB: '+1-202-555-0199',
              resolvedValue: '+1-202-555-0199',
              resolutionStrategy: 'VECTOR_CLOCK_LWW',
              status: 'AUTO_RESOLVED'
            }
          ]
        }
      }
    })
  })

  it('renders multi region conflict modal properly', () => {
    const wrapper = mount(MultiRegionConflictModal, {
      props: {
        modelValue: true
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-modal': {
            template: '<div><h1>{{ title }}</h1><slot /></div>',
            props: ['title']
          },
          'va-alert': true,
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('multi_region_conflict')
  })
})
