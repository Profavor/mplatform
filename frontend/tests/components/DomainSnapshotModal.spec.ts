import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import DomainSnapshotModal from '../../components/domain/DomainSnapshotModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  formatWithTimezone: (date: string) => date
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('DomainSnapshotModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: [
          {
            snapshotId: 'snap-1',
            domainId: 'domain-1',
            snapshotName: '2026 Q3 Backup',
            versionTag: 'v1.0',
            recordCount: 50,
            createdBy: 'ADMIN',
            createdAt: '2026-08-15T12:00:00'
          }
        ]
      }
    })
  })

  it('renders domain snapshot modal properly', () => {
    const wrapper = mount(DomainSnapshotModal, {
      props: {
        modelValue: true,
        domainId: 'domain-1'
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
          'va-input': true,
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('domain_snapshot')
  })
})
