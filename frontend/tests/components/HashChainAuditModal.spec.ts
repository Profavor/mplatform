import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import HashChainAuditModal from '../../components/records/HashChainAuditModal.vue'

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

describe('HashChainAuditModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          totalBlocks: 2,
          validBlocks: 2,
          isChainIntact: true,
          summary: '해시체인 무결성 정상',
          blocks: [
            {
              blockIndex: 1,
              actionType: 'CREATE',
              actor: 'ADMIN',
              blockHash: 'abcdef1234567890',
              valid: true
            }
          ]
        }
      }
    })
  })

  it('renders hash chain audit modal properly', () => {
    const wrapper = mount(HashChainAuditModal, {
      props: {
        modelValue: true,
        recordId: 'rec-1'
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
    expect(wrapper.text()).toContain('hash_chain_ledger')
  })
})
