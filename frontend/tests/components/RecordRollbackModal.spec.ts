import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RecordRollbackModal from '../../components/records/RecordRollbackModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params && params.version) return `${key}:${params.version}`
      return key
    },
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

describe('RecordRollbackModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders rollback details and calls rollback API on submit', async () => {
    mockCustomFetch.mockResolvedValue({
      data: { value: { success: true, approvalRequestId: 'app-999' } },
      status: { value: 'success' }
    })

    const wrapper = mount(RecordRollbackModal, {
      props: {
        modelValue: true,
        recordId: 'rec-1234',
        recordDisplayCode: 'REC-1234',
        targetVersion: 1,
        diffRows: [{ key: 'name', label: '이름', before: '홍길동', after: '김철수' }]
      },
      global: {
        mocks: {
          $t: (key: string, params?: any) => key
        },
        stubs: {
          'va-modal': { template: '<div><slot /></div>' },
          'va-alert': { template: '<div><slot /></div>' },
          'va-input': {
            props: ['modelValue'],
            template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
          },
          'va-button': {
            template: '<button :disabled="$attrs.disabled" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('REC-1234')
    expect(wrapper.text()).toContain('Version 1')

    // Input reason
    const textarea = wrapper.find('textarea')
    await textarea.setValue('잘못된 정보 수정으로 인한 롤백')

    // Submit button
    const buttons = wrapper.findAll('button')
    const submitBtn = buttons.find(b => b.text().includes('rollback_btn'))
    expect(submitBtn).toBeDefined()
    await submitBtn!.trigger('click')

    expect(mockCustomFetch).toHaveBeenCalledWith('/records/rec-1234/rollback', {
      method: 'POST',
      body: {
        targetVersion: 1,
        reason: '잘못된 정보 수정으로 인한 롤백'
      }
    })
  })
})
