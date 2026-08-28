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
})
