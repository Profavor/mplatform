import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import BulkReclassifyModal from '../../components/records/BulkReclassifyModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vuestic-ui')>()
  return {
    ...actual,
    useToast: () => ({
      init: vi.fn()
    })
  }
})

describe('BulkReclassifyModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders correctly and fetches classification tree on open', async () => {
    mockCustomFetch.mockResolvedValue({
      data: {
        value: [
          { id: 'node-1', name: { ko: '상위 노드' }, children: [{ id: 'node-2', name: { ko: '하위 노드' } }] }
        ]
      }
    })

    const wrapper = mount(BulkReclassifyModal, {
      props: {
        modelValue: true,
        selectedRecordIds: ['rec-1', 'rec-2'],
        domainId: 'domain-1'
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': {
            template: '<div><slot /></div>'
          },
          'va-alert': true,
          'va-select': true,
          'va-input': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(mockCustomFetch).toHaveBeenCalledWith('/domains/domain-1/classification-tree')
  })
})
