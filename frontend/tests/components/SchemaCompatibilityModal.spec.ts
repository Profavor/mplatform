import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SchemaCompatibilityModal from '../../components/schema/SchemaCompatibilityModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('SchemaCompatibilityModal.vue', () => {
  const dummyFields = [
    {
      id: 'f-1',
      key: 'biz_reg_no',
      name: { ko: '사업자등록번호', en: 'Business Reg No' },
      type: 'STRING',
      required: false
    },
    {
      id: 'f-2',
      key: 'legacy_code',
      name: { ko: '레거시코드', en: 'Legacy Code' },
      type: 'STRING',
      required: false
    }
  ]

  const dummyImpactReport = {
    domainId: 'd-123',
    fieldKey: 'biz_reg_no',
    changeType: 'DROP',
    compatibilityStatus: 'BREAKING',
    riskScore: 95,
    totalDomainRecords: 1000,
    affectedRecordCount: 850,
    affectedRecordPercentage: 85.0,
    summary: '850건의 레코드에 실제 데이터가 존재하여 영구 유실될 위험이 있는 브레이킹 체인지입니다.',
    affectedDqRules: ['REGEX: 사업자등록번호 형식 검증'],
    affectedChannels: ['ERP 연동 채널 (WEB_SERVICE)'],
    risks: [
      {
        fieldKey: 'biz_reg_no',
        changeType: 'REMOVED',
        riskLevel: 'CRITICAL',
        impactDescription: '850건 레코드 데이터 영구 유실 및 조회 오류 위험',
        mitigationGuide: '필드를 즉시 삭제하지 말고 시스템 비활성화(Deprecated) 플래그를 설정하세요.'
      }
    ],
    mitigationGuides: [
      '기존 데이터 유실 방지를 위해 삭제 전 레코드 백업을 수행하세요.',
      '즉시 DROP 대신 Deprecated 처리하세요.'
    ]
  }

  const dummyDdlReport = {
    domainId: 'd-123',
    overallCompatibility: 'BREAKING_CHANGE',
    riskScore: 80,
    summary: 'DDL 변경 시 브레이킹 체인지 감지',
    risks: [
      {
        fieldKey: 'legacy_code',
        changeType: 'REMOVED',
        riskLevel: 'CRITICAL',
        impactDescription: 'API 호출 오류 위험',
        mitigationGuide: 'Deprecated 권장'
      }
    ]
  }

  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockImplementation(async (url: string) => {
      if (url.includes('/fields')) {
        return dummyFields
      }
      if (url.includes('/simulate-field-impact')) {
        return dummyImpactReport
      }
      if (url.includes('/compatibility-check')) {
        return dummyDdlReport
      }
      return null
    })
  })

  it('renders modal with tabs properly and loads domain fields', async () => {
    const wrapper = mount(SchemaCompatibilityModal, {
      props: {
        modelValue: true,
        domainId: 'd-123'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          AppModal: {
            template: '<div><h2>{{ title }}</h2><slot /></div>',
            props: ['title']
          },
          'va-tabs': {
            template: '<div><slot name="tabs" /></div>'
          },
          'va-tab': {
            template: '<button><slot /></button>'
          },
          'va-alert': true,
          'va-select': true,
          'va-input': true,
          'va-textarea': true,
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true,
          'va-chip': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('schema_compatibility')
    expect(wrapper.text()).toContain('field_impact.tab_field_sim')
    expect(wrapper.text()).toContain('field_impact.tab_ddl_sim')

    // Wait for async loadDomainFields
    await wrapper.vm.$nextTick()
    await new Promise(r => setTimeout(r, 10))
    expect(mockCustomFetch).toHaveBeenCalledWith('/api/domains/d-123/fields')
  })

  it('displays field simulation results with KPIs and mitigation guides', async () => {
    const wrapper = mount(SchemaCompatibilityModal, {
      props: {
        modelValue: true,
        domainId: 'd-123'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          AppModal: {
            template: '<div><h2>{{ title }}</h2><slot /></div>',
            props: ['title']
          },
          'va-tabs': {
            template: '<div><slot name="tabs" /></div>'
          },
          'va-tab': true,
          'va-alert': true,
          'va-select': true,
          'va-input': true,
          'va-textarea': true,
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true,
          'va-chip': true
        }
      }
    })

    // Wait for loadDomainFields
    await wrapper.vm.$nextTick()
    await new Promise(r => setTimeout(r, 10))

    // Trigger field simulation
    await (wrapper.vm as any).runFieldSimulation()
    await wrapper.vm.$nextTick()

    expect(mockCustomFetch).toHaveBeenCalledWith(
      '/api/domains/d-123/schema/simulate-field-impact',
      expect.objectContaining({ method: 'POST' })
    )

    expect(wrapper.text()).toContain(dummyImpactReport.summary)
    expect(wrapper.text()).toContain('850')
    expect(wrapper.text()).toContain('1,000')
    expect(wrapper.text()).toContain('field_impact.impact_details_title')
    expect(wrapper.text()).toContain('field_impact.mitigation_guides_title')
  })
})

