import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick, ref } from 'vue'
import { createI18n } from 'vue-i18n'
import GlobalSearch from '~/components/layout/GlobalSearch.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      global_search_placeholder: '통합 검색...',
      search_min_length: '2자 이상 입력해주세요',
      search_no_results: '검색 결과가 없습니다',
      close: '닫기'
    }
  }
} as any)

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (d: any) => '2026-09-14 12:00:00'
  })
}))

describe('GlobalSearch.vue (Desktop & Mobile Support)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      content: [
        {
          id: '12345678-1234-1234-1234-123456789abc',
          data: JSON.stringify({ name: '테스트 데이터' }),
          status: 'ACTIVE',
          node: { id: 'node-1', name: '테스트 노드', domain: { id: 'dom-1', name: '테스트 도메인' } }
        }
      ]
    })
  })

  const stubs = {
    'va-input': {
      props: ['modelValue', 'placeholder'],
      emits: ['update:modelValue', 'focus', 'blur', 'keydown'],
      template: '<div class="va-input-stub"><input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" @focus="$emit(\'focus\')" /><slot name="prependInner" /><slot name="appendInner" /></div>'
    },
    'va-button': {
      template: '<button @click="$emit(\'click\')"><slot /></button>'
    },
    'va-icon': true,
    'va-badge': true,
    'va-inner-loading': {
      template: '<div><slot /></div>'
    },
    ImageLightboxModal: true,
    Teleport: {
      template: '<div class="teleport-stub"><slot /></div>'
    }
  }

  it('데스크탑 검색 입력창과 모바일 검색 트리거 버튼이 모두 렌더링되어야 한다', async () => {
    const wrapper = mount(GlobalSearch, {
      global: {
        plugins: [i18n],
        stubs
      }
    })

    await nextTick()

    // 데스크탑 전용 클래스가 적용된 입력창 존재
    const desktopInput = wrapper.find('.desktop-search-input')
    expect(desktopInput.exists()).toBe(true)

    // 모바일 전용 검색 아이콘 버튼 존재
    const mobileBtn = wrapper.find('.mobile-search-btn')
    expect(mobileBtn.exists()).toBe(true)
  })

  it('모바일 검색 아이콘 클릭 시 모바일 검색 오버레이가 활성화된다', async () => {
    const wrapper = mount(GlobalSearch, {
      global: {
        plugins: [i18n],
        stubs
      }
    })

    await nextTick()
    expect(wrapper.find('.mobile-search-overlay').exists()).toBe(false)

    // 모바일 검색 버튼 클릭
    const mobileBtn = wrapper.find('.mobile-search-btn')
    await mobileBtn.trigger('click')
    await nextTick()

    // 모바일 검색 풀스크린 오버레이 열림 확인
    expect(wrapper.vm.isMobileSearchOpen).toBe(true)
    const overlay = wrapper.find('.mobile-search-overlay')
    expect(overlay.exists()).toBe(true)

    // 닫기/뒤로가기 버튼 클릭 시 닫힘 확인
    const backBtn = wrapper.find('.mobile-search-back-btn')
    await backBtn.trigger('click')
    await nextTick()

    expect(wrapper.vm.isMobileSearchOpen).toBe(false)
  })

  it('검색 결과 카드 클릭 시 레코드 상세로 이동하고 모바일 오버레이가 닫힌다', async () => {
    const wrapper = mount(GlobalSearch, {
      global: {
        plugins: [i18n],
        stubs
      }
    })

    await nextTick()
    const vm = wrapper.vm as any
    vm.openMobileSearch()
    await nextTick()

    vm.results = [
      {
        id: 'test-record-uuid',
        data: '{"title":"모바일 테스트"}',
        node: { id: 'node-1', domain: { id: 'dom-1' } }
      }
    ]
    await nextTick()

    const card = wrapper.find('.search-card-item')
    expect(card.exists()).toBe(true)

    await card.trigger('click')
    await nextTick()

    // 라우터 이동 및 모바일 오버레이 닫힘
    expect(mockPush).toHaveBeenCalledWith(expect.objectContaining({
      path: '/records',
      query: expect.objectContaining({ recordId: 'test-record-uuid' })
    }))
    expect(wrapper.vm.isMobileSearchOpen).toBe(false)
  })

  it('Rust 백엔드의 records 필드 형식 응답을 정상적으로 파싱하고 stock_name을 주 필드로 표시한다', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      query: 'LG디스플레이',
      totalHits: 1,
      totalElements: 1,
      domains: [],
      records: [
        {
          id: '3f611a68-3101-414a-96dc-43f77859a5da',
          data: JSON.stringify({
            bps: 12602.0,
            stock_name: 'LG디스플레이',
            ticker_code: '034220',
            market_type: 'KOSPI'
          }),
          status: 'ACTIVE',
          node: {
            id: 'node-stock-1',
            name: '국내주식',
            domain: { id: 'dom-stock-1', name: '주식' }
          }
        }
      ]
    })

    const wrapper = mount(GlobalSearch, {
      global: {
        plugins: [i18n],
        stubs
      }
    })

    await nextTick()
    const vm = wrapper.vm as any
    vm.searchQuery = 'LG디스플레이'
    await vm.performSearch()
    await nextTick()

    // records 배열이 results에 매핑되었는지 검증
    expect(vm.results.length).toBe(1)
    const primary = vm.getPrimaryField(vm.results[0])
    expect(primary.rawKey).toBe('stock_name')
    expect(primary.val).toBe('LG디스플레이')
  })
})
