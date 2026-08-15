import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AsyncBatchExportModal from '../../components/AsyncBatchExportModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: (...args: any[]) => mockCustomFetch(...args)
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, fallback?: string) => fallback || key,
    locale: { value: 'ko' }
  })
}))

describe('AsyncBatchExportModal.vue (TDD)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('COMPLETED 상태일 때 파일 다운로드 버튼이 정상적으로 렌더링된다', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      taskId: 'task-12345678',
      status: 'COMPLETED',
      progressPercent: 100,
      processedCount: 1,
      totalCount: 1,
      downloadUrl: '/api/batch/download/task-12345678'
    })

    const wrapper = mount(AsyncBatchExportModal, {
      props: {
        modelValue: true,
        domainId: 'domain-1',
        gridApi: null
      },
      global: {
        mocks: {
          $t: (k: string, f?: string) => f || k
        },
        stubs: {
          'va-modal': {
            template: '<div><h1>{{ title }}</h1><slot /></div>',
            props: ['title']
          },
          'va-progress-bar': true,
          'va-badge': {
            template: '<span class="va-badge">{{ text }}</span>',
            props: ['text', 'color']
          },
          'va-button': {
            template: '<button :class="icon">{{ icon }}<slot /></button>',
            props: ['color', 'icon', 'loading', 'preset']
          }
        }
      }
    })

    // Click start export
    const startBtn = wrapper.find('button.download')
    if (startBtn.exists()) {
      await startBtn.trigger('click')
    }

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 50))

    // Check if download button appears
    expect(wrapper.text()).toContain('download_file')
    expect(wrapper.text()).toContain('완료')
  })
})
