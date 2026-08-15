import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ColdStorageArchiveModal from '../../components/admin/ColdStorageArchiveModal.vue'

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

describe('ColdStorageArchiveModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: [
          {
            archiveId: 'PKG-001',
            archiveName: '정기 동결 백업',
            checksumSha256: 'abcdef1234567890',
            domainCount: 5,
            recordCount: 10000,
            compressionRatio: '80%'
          }
        ]
      }
    })
  })

  it('renders cold storage archive modal properly', () => {
    const wrapper = mount(ColdStorageArchiveModal, {
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
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('cold_storage')
  })
})
