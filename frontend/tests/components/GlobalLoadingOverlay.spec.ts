import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import GlobalLoadingOverlay from '../../components/GlobalLoadingOverlay.vue'
import { useLoading } from '../../composables/useLoading'

describe('GlobalLoadingOverlay Component (TDD)', () => {
  let loading: ReturnType<typeof useLoading>

  beforeEach(() => {
    vi.useFakeTimers()
    loading = useLoading()
    loading.resetLoading()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('isLoading이 false일 때 오버레이가 보이지 않아야 한다', () => {
    const wrapper = mount(GlobalLoadingOverlay, {
      global: {
        stubs: {
          Teleport: true,
          'va-progress-circle': true
        }
      }
    })
    expect(wrapper.find('.glass-overlay').exists()).toBe(false)
  })

  it('200ms 지연 후 isLoading이 true일 때 오버레이 및 텍스트가 정상 렌더링되어야 한다', async () => {
    loading.showLoading('DB 조회 중...')
    vi.advanceTimersByTime(200)

    const wrapper = mount(GlobalLoadingOverlay, {
      global: {
        stubs: {
          Teleport: true,
          'va-progress-circle': true
        }
      }
    })
    expect(wrapper.find('.glass-overlay').exists()).toBe(true)
    expect(wrapper.find('.loading-text').text()).toBe('DB 조회 중...')
  })
})
