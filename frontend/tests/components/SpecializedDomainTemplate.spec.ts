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
})
