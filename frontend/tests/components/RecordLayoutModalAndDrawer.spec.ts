import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import RecordDetailDrawer from '../../components/records/RecordDetailDrawer.vue'
import RecordLayoutBuilderModal from '../../components/records/RecordLayoutBuilderModal.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      standard_form_view: '표준 폼 뷰',
      layout_select_label: '화면 레이아웃',
      layout_builder_title: '화면 레이아웃 빌더',
      btn_layout_builder: '화면 레이아웃 편집'
    }
  }
})

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({
    hasPermission: () => true
  })
}))

describe('RecordDetailDrawer & RecordLayoutBuilderModal Layout Switcher (TDD)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('RecordDetailDrawer: customFetch를 통해 커스텀 레이아웃을 정상 조회해야 한다', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      layouts: [
        {
          id: 'layout_2d_v1',
          name: { ko: '상세 커스텀 2D', en: 'Detail Custom 2D' },
          isDefault: true,
          cols: 12,
          rowHeight: 42,
          widgets: [{ id: 'w1', type: 'FIELD', fieldKey: 'customer_name', w: 6, h: 1 }]
        }
      ]
    })

    const wrapper = mount(RecordDetailDrawer, {
      props: {
        show: true,
        selectedDomainInfo: { id: 'domain-123', name: { ko: '고객 도메인' } },
        record: { id: 'rec-1', domainId: 'domain-123', customer_name: '테스트 고객' },
        fields: [{ key: 'customer_name', name: { ko: '고객명' }, type: 'TEXT' }]
      },
      global: {
        plugins: [i18n],
        stubs: {
          AppModal: { template: '<div class="app-modal-stub"><slot name="header"/><slot/></div>' },
          vaSelect: true,
          vaButton: true,
          vaIcon: true,
          vaChip: true,
          vaBadge: true,
          vaTabs: true,
          vaTab: true,
          SpecializedDomainWidgetRenderer: true,
          DynamicRecordLayoutRenderer: true,
          RecordLayoutBuilderModal: true
        }
      }
    })

    expect(mockCustomFetch).toHaveBeenCalledWith('/api/domains/domain-123/layout')
  })

  it('RecordLayoutBuilderModal: domainId가 누락되어도 targetNode의 domainId로 안전하게 폴백하여 API를 호출해야 한다', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      layouts: []
    })

    const wrapper = mount(RecordLayoutBuilderModal, {
      props: {
        modelValue: true,
        domainId: '',
        targetNode: { id: 'node-abc', domainId: 'domain-fallback-999', name: { ko: '노드 1' } },
        fields: [{ id: 'f1', key: 'cust_no', name: { ko: '고객번호' }, type: 'TEXT' }]
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaModal: { template: '<div class="va-modal-stub"><slot/></div>' },
          vaButton: true,
          vaIcon: true,
          vaInput: true,
          vaSelect: true,
          vaBadge: true
        }
      }
    })

    expect(mockCustomFetch).toHaveBeenCalledWith(expect.stringContaining('/api/domains/domain-fallback-999'))
  })
})
