import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxMemoApprovalModal from '../../components/inbox/InboxMemoApprovalModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<Record<string, any>>()
  return {
    ...actual,
    useToast: () => ({
      init: vi.fn()
    })
  }
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('InboxMemoApprovalModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({ id: 'memo-appr-1', targetType: 'MEMO' })
  })

  it('renders memo approval modal correctly', () => {
    const wrapper = mount(InboxMemoApprovalModal, {
      props: {
        modelValue: true
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          AppModal: {
            template: '<div><h1>{{ title }}</h1><slot /><slot name="footer" /></div>',
            props: ['title', 'modelValue']
          },
          ApprovalRouteBuilder: {
            template: '<div class="approval-route-builder-stub"></div>',
            props: ['modelValue']
          },
          InboxAttachmentUploader: true,
          HtmlEditor: {
            template: '<div class="html-editor-stub"></div>',
            props: ['modelValue']
          },
          SubmissionCommentModal: true,
          'va-input': true,
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('inbox.memo_approval_title')
  })
})
