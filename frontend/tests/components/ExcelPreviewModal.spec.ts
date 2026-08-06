import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ExcelPreviewModal from '../../components/chat/ExcelPreviewModal.vue'

vi.mock('xlsx', () => {
  throw new Error('Vulnerable package xlsx should be removed and not imported!')
})

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: vi.fn().mockResolvedValue(new Blob([''], { type: 'application/octet-stream' }))
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

describe('ExcelPreviewModal.vue (Without vulnerable xlsx)', () => {
  it('mounts properly without importing xlsx package', () => {
    const wrapper = mount(ExcelPreviewModal, {
      props: {
        modelValue: true,
        fileUrl: '/api/files/test.xlsx',
        fileName: 'test.xlsx'
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': { template: '<div><slot /></div>' },
          'va-progress-circle': true,
          'va-button': true,
          'va-input': true,
          'va-icon': true,
          'va-badge': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
