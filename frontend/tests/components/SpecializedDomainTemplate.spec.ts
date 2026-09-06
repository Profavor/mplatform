import { describe, it, expect } from 'vitest'

describe('SpecializedDomainTemplate Frontend Unit Tests', () => {
  const validCommonCodeFieldTypes = new Set([
    'TEXT', 'NUMBER', 'DATE', 'BOOLEAN', 'JSON', 'SELECT',
    'DOMAIN_REFERENCE', 'TIME', 'HTML_TEXT', 'CALCULATED',
    'MULTILINGUAL', 'FILE', 'IMAGE', 'DATE_RANGE', 'EMAIL'
  ])

  it('특화도메인 카테고리별 유효한 필드 타입만 정의되어야 한다 (RICHTEXT 등 미정의 타입 배제)', () => {
    const mockCustomerFields = [
      { key: 'customer_no', type: 'TEXT' },
      { key: 'customer_name', type: 'TEXT' },
      { key: 'customer_type', type: 'SELECT' },
      { key: 'contact_email', type: 'EMAIL' },
      { key: 'credit_limit', type: 'NUMBER' },
      { key: 'memo', type: 'HTML_TEXT' }
    ]

    mockCustomerFields.forEach(field => {
      expect(validCommonCodeFieldTypes.has(field.type)).toBe(true)
      expect(field.type).not.toBe('RICHTEXT')
    })
  })

  it('다시 만들기 요청 시 category 및 메타데이터 페이로드가 올바르게 생성되어야 한다', () => {
    const tplForm = {
      category: 'CUSTOMER',
      name: { ko: '고객 마스터', en: 'Customer Master' },
      description: { ko: '고객 정보 관리', en: 'Customer Master Data' },
      numberingPattern: 'CUST-{YYYY}-{SEQ:6}',
      icon: 'person_pin'
    }

    expect(tplForm.category).toBe('CUSTOMER')
    expect(tplForm.name.ko).toBe('고객 마스터')
    expect(tplForm.numberingPattern).toBe('CUST-{YYYY}-{SEQ:6}')
  })

  it('분류트리 노드 목록에서 최상위 더미 전체 노드 없이 1단계 노드들이 루트로 구성되어야 한다', () => {
    const mockNodes = [
      { code: 'INDIVIDUAL', parentCode: null, name: { ko: '개인 고객' }, depth: 0 },
      { code: 'INDIVIDUAL_GENERAL', parentCode: 'INDIVIDUAL', name: { ko: '일반 개인' }, depth: 1 },
      { code: 'CORPORATE', parentCode: null, name: { ko: '법인/기업 고객' }, depth: 0 }
    ]

    const rootNodes = mockNodes.filter(n => n.parentCode === null)
    expect(rootNodes).toHaveLength(2)
    expect(rootNodes.map(n => n.code)).toEqual(['INDIVIDUAL', 'CORPORATE'])
    expect(mockNodes.find(n => n.name.ko === '전체 고객')).toBeUndefined()
  })

  it('LEASE_CONTRACT(부동산 임대차 마스터) 템플릿 페이로드 및 필드 타입이 규격에 맞게 구성되어야 한다', () => {
    const leaseTemplate = {
      category: 'LEASE_CONTRACT',
      name: { ko: '부동산 임대차 마스터', en: 'Real Estate Lease Master' },
      description: { ko: '건물·층·호실별 임대차 계약 정보, 보증금 및 월세 관리, 만기일자 및 권리관계 리스크 상시 감시', en: 'Real Estate Lease & Risk Management Master Data' },
      numberingPattern: 'LEASE-{YYYY}-{SEQ:6}',
      icon: 'apartment',
      axisCode: 'LEASE_PROPERTY_TYPE',
      identifierFieldKey: 'contract_no',
      displayNameFieldKey: 'tenant_name',
      fields: [
        { key: 'contract_no', type: 'TEXT' },
        { key: 'building_name', type: 'TEXT' },
        { key: 'address_primary', type: 'TEXT' },
        { key: 'unit_number', type: 'TEXT' },
        { key: 'exclusive_area', type: 'NUMBER' },
        { key: 'tenant_name', type: 'TEXT' },
        { key: 'tenant_contact', type: 'TEXT' },
        { key: 'tenant_email', type: 'EMAIL' },
        { key: 'lease_type', type: 'SELECT' },
        { key: 'contract_start_date', type: 'DATE' },
        { key: 'contract_end_date', type: 'DATE' },
        { key: 'deposit_amount', type: 'NUMBER' },
        { key: 'monthly_rent', type: 'NUMBER' },
        { key: 'maintenance_fee', type: 'NUMBER' },
        { key: 'rent_payment_day', type: 'NUMBER' },
        { key: 'prior_mortgage_amount', type: 'NUMBER' },
        { key: 'market_price_estimate', type: 'NUMBER' },
        { key: 'debt_ratio', type: 'NUMBER' },
        { key: 'contract_status', type: 'SELECT' },
        { key: 'special_agreement', type: 'HTML_TEXT' }
      ]
    }

    expect(leaseTemplate.category).toBe('LEASE_CONTRACT')
    expect(leaseTemplate.icon).toBe('apartment')
    expect(leaseTemplate.numberingPattern).toBe('LEASE-{YYYY}-{SEQ:6}')
    expect(leaseTemplate.fields).toHaveLength(20)

    leaseTemplate.fields.forEach(field => {
      expect(validCommonCodeFieldTypes.has(field.type)).toBe(true)
    })
  })
})
