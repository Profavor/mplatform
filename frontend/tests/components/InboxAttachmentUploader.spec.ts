import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxAttachmentUploader from '../../components/inbox/InboxAttachmentUploader.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string, params?: any) => key })
}))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: vi.fn().mockResolvedValue({ url: '/api/files/download/test.png' })
  })
}))

describe('InboxAttachmentUploader', () => {
  const commonStubs = {
    VaIcon: true,
    VaButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
    VaProgressCircle: true
  }

  it('renders dropzone correctly when empty', () => {
    const wrapper = mount(InboxAttachmentUploader, {
      props: {
        modelValue: []
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: commonStubs
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.attachment-dropzone').exists()).toBe(true)
  })

  it('renders attached files with status badges', () => {
    const file = new File(['content'], 'sample.pdf', { type: 'application/pdf' })
    const items = [
      {
        id: 'att-1',
        name: 'sample.pdf',
        size: 1024,
        status: 'ready' as const,
        file
      },
      {
        id: 'att-2',
        name: 'uploading.xlsx',
        size: 2048,
        status: 'uploading' as const,
        file: new File(['data'], 'uploading.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
      }
    ]

    const wrapper = mount(InboxAttachmentUploader, {
      props: {
        modelValue: items
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: commonStubs
      }
    })

    expect(wrapper.findAll('.attachment-card').length).toBe(2)
    expect(wrapper.find('.status-ready').exists()).toBe(true)
    expect(wrapper.find('.status-uploading').exists()).toBe(true)
  })

  it('executes uploadAll on demand', async () => {
    const file = new File(['content'], 'sample.pdf', { type: 'application/pdf' })
    const items = [
      {
        id: 'att-1',
        name: 'sample.pdf',
        size: 1024,
        status: 'ready' as const,
        file
      }
    ]

    const wrapper = mount(InboxAttachmentUploader, {
      props: {
        modelValue: items
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: commonStubs
      }
    })

    const res = await (wrapper.vm as any).uploadAll()
    expect(res.success).toBe(true)
    expect(res.urls.length).toBe(1)
  })
})
