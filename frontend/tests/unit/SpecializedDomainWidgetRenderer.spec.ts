import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SpecializedDomainWidgetRenderer from '../../components/records/specialized/SpecializedDomainWidgetRenderer.vue'
import CustomerSummaryWidget from '../../components/records/specialized/CustomerSummaryWidget.vue'
import VendorBusinessWidget from '../../components/records/specialized/VendorBusinessWidget.vue'
import ProductCatalogWidget from '../../components/records/specialized/ProductCatalogWidget.vue'
import MaterialInventoryWidget from '../../components/records/specialized/MaterialInventoryWidget.vue'
import EmployeeProfileWidget from '../../components/records/specialized/EmployeeProfileWidget.vue'
import StockInfoWidget from '../../components/records/specialized/StockInfoWidget.vue'

describe('SpecializedDomainWidgetRenderer', () => {
  const globalConfig = {
    mocks: {
      $t: (key: string, params?: any) => {
        if (params && params.count !== undefined) return `${params.count}`
        return key
      }
    }
  }

  it('일반 도메인인 경우 어떤 특화 위젯도 렌더링하지 않아야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: { domainType: 'GENERAL', specializedCategory: null },
        recordData: { some_field: 'value' }
      },
      global: globalConfig
    })

    expect(wrapper.find('.specialized-widget-container').exists()).toBe(false)
  })

  it('CUSTOMER 카테고리 도메인일 때 CustomerSummaryWidget을 렌더링해야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: { domainType: 'SPECIALIZED', specializedCategory: 'CUSTOMER' },
        recordData: { customer_no: 'CUST-001', customer_name: '홍길동' }
      },
      global: globalConfig
    })

    expect(wrapper.findComponent(CustomerSummaryWidget).exists()).toBe(true)
  })

  it('VENDOR 카테고리 도메인일 때 VendorBusinessWidget을 렌더링해야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: { domainType: 'SPECIALIZED', specializedCategory: 'VENDOR' },
        recordData: { vendor_code: 'VEND-001', vendor_name: '대한상사', biz_reg_no: '1234567890' }
      },
      global: globalConfig
    })

    expect(wrapper.findComponent(VendorBusinessWidget).exists()).toBe(true)
  })

  it('PRODUCT 카테고리 도메인일 때 ProductCatalogWidget을 렌더링해야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: { domainType: 'SPECIALIZED', specializedCategory: 'PRODUCT' },
        recordData: { sku_code: 'PROD-001', product_name: '고성능 노트북', retail_price: 1500000, cost_price: 1000000 }
      },
      global: globalConfig
    })

    expect(wrapper.findComponent(ProductCatalogWidget).exists()).toBe(true)
  })

  it('MATERIAL 카테고리 도메인일 때 MaterialInventoryWidget을 렌더링해야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: { domainType: 'SPECIALIZED', specializedCategory: 'MATERIAL' },
        recordData: { material_code: 'MAT-001', material_name: '특수강판', base_uom: 'KG' }
      },
      global: globalConfig
    })

    expect(wrapper.findComponent(MaterialInventoryWidget).exists()).toBe(true)
  })

  it('EMPLOYEE 카테고리 도메인일 때 EmployeeProfileWidget을 렌더링해야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: { domainType: 'SPECIALIZED', specializedCategory: 'EMPLOYEE' },
        recordData: { employee_no: 'EMP-001', employee_name: '김철수', department: '개발팀' }
      },
      global: globalConfig
    })

    expect(wrapper.findComponent(EmployeeProfileWidget).exists()).toBe(true)
  })

  it('STOCK 카테고리 도메인일 때 StockInfoWidget을 렌더링해야 한다', () => {
    const wrapper = mount(SpecializedDomainWidgetRenderer, {
      props: {
        domain: { domainType: 'SPECIALIZED', specializedCategory: 'STOCK' },
        recordData: { ticker_code: '005930', stock_name: '삼성전자', market_type: 'KOSPI' }
      },
      global: globalConfig
    })

    expect(wrapper.findComponent(StockInfoWidget).exists()).toBe(true)
  })
})
