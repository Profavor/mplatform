// @vitest-environment happy-dom
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SpecializedDomainWidgetRenderer from '../../components/records/specialized/SpecializedDomainWidgetRenderer.vue'
import DomainRecordHeaderWidget from '../../components/records/specialized/DomainRecordHeaderWidget.vue'

describe('SpecializedDomainWidgetRenderer', () => {
  const globalConfig = {
    mocks: {
      $t: (key: string, params?: any) => {
        if (params && params.count !== undefined) return `${params.count}`
        return key
      }
    },
    stubs: {
      vaCard: { template: '<div class="va-card-stub"><slot/></div>' },
      vaCardContent: { template: '<div class="va-card-content-stub"><slot/></div>' },
      vaAvatar: { template: '<div class="va-avatar-stub"><slot/></div>' },
      vaIcon: { template: '<i class="va-icon-stub"></i>' }
    }
  }

  it('도메인에 속성 필드가 하나도 설정되지 않은 경우 헤더 위젯을 렌더링하지 않아야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: { id: 'dom-1', name: { ko: '일반도메인' } },
        recordData: { some_field: 'value' },
        fields: []
      },
      global: globalConfig
    })

    expect(wrapper.findComponent(DomainRecordHeaderWidget).exists()).toBe(false)
  })

  it('identifierFieldId 또는 displayNameFieldId가 설정된 경우 DomainRecordHeaderWidget을 렌더링해야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: {
          id: 'dom-1',
          name: { ko: '고객 도메인' },
          identifierFieldId: 'f-1',
          displayNameFieldId: 'f-2'
        },
        recordData: { customer_no: 'CUST-001', customer_name: '홍길동' },
        fields: [
          { id: 'f-1', name: 'customer_no', label: { ko: '고객번호' } },
          { id: 'f-2', name: 'customer_name', label: { ko: '고객명' } }
        ]
      },
      global: globalConfig
    })

    expect(wrapper.findComponent(DomainRecordHeaderWidget).exists()).toBe(true)
  })

  it('다국어(MULTILINGUAL) 객체 필드가 [object Object]가 아닌 언어별 텍스트로 정상 렌더링되어야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: {
          id: 'dom-customer',
          name: { ko: '고객 마스터', en: 'Customer Master' },
          identifierFieldId: 'f-id',
          displayNameFieldId: 'f-name'
        },
        recordData: {
          CUSTOMER_NO: 'CUST-2026-000001',
          CUSTOMER_NAME: { ko: '홍길동', en: 'Hong Gil Dong' },
          status: 'ACTIVE'
        },
        fields: [
          { id: 'f-id', key: 'CUSTOMER_NO', name: { ko: '고객번호', en: 'Customer No' }, type: 'TEXT' },
          { id: 'f-name', key: 'CUSTOMER_NAME', name: { ko: '고객명', en: 'Customer Name' }, type: 'MULTILINGUAL', options: {} }
        ],
        customSubFieldKeys: ['CUSTOMER_NO', 'CUSTOMER_NAME']
      },
      global: globalConfig
    })

    const headerWidget = wrapper.findComponent(DomainRecordHeaderWidget)
    expect(headerWidget.exists()).toBe(true)

    // [object Object]가 텍스트에 포함되지 않아야 함
    expect(headerWidget.text()).not.toContain('[object Object]')
    expect(headerWidget.text()).toContain('홍길동')
    expect(headerWidget.text()).toContain('CUST-2026-000001')
  })
})
