import { describe, it, expect } from 'vitest'
import { ref } from 'vue'

describe('Domain Save Payload Logic', () => {
  it('should correctly format identifierFieldId when null', () => {
    const newDomain = ref({
      identifierFieldId: null
    })
    
    const payloadValue = (typeof newDomain.value.identifierFieldId === 'object' && newDomain.value.identifierFieldId !== null) 
      ? (newDomain.value.identifierFieldId as any).value 
      : (newDomain.value.identifierFieldId || null)

    expect(payloadValue).toBeNull()
  })

  it('should correctly format identifierFieldId when empty string (vuestic clear)', () => {
    const newDomain = ref({
      identifierFieldId: ''
    })
    
    const payloadValue = (typeof newDomain.value.identifierFieldId === 'object' && newDomain.value.identifierFieldId !== null) 
      ? (newDomain.value.identifierFieldId as any).value 
      : (newDomain.value.identifierFieldId || null)

    expect(payloadValue).toBeNull()
  })

  it('should correctly format identifierFieldId when object is selected', () => {
    const newDomain = ref({
      identifierFieldId: { value: '123e4567-e89b-12d3-a456-426614174000', text: 'ID' }
    })
    
    const payloadValue = (typeof newDomain.value.identifierFieldId === 'object' && newDomain.value.identifierFieldId !== null) 
      ? (newDomain.value.identifierFieldId as any).value 
      : (newDomain.value.identifierFieldId || null)

    expect(payloadValue).toBe('123e4567-e89b-12d3-a456-426614174000')
  })

  it('should correctly format identifierFieldId when primitive string is bound by value-by', () => {
    const newDomain = ref({
      identifierFieldId: '123e4567-e89b-12d3-a456-426614174000'
    })
    
    const payloadValue = (typeof newDomain.value.identifierFieldId === 'object' && newDomain.value.identifierFieldId !== null) 
      ? (newDomain.value.identifierFieldId as any).value 
      : (newDomain.value.identifierFieldId || null)

    expect(payloadValue).toBe('123e4567-e89b-12d3-a456-426614174000')
  })
})

describe('Schema Grid ColumnDefs & Template Column Width', () => {
  it('should display multilingual common code name for Type column', () => {
    const mockCodeMap: Record<string, string> = {
      'TEXT': '문자열 (Text)',
      'EMAIL': '✉️ 이메일 (Email)',
      'NUMBER': '숫자 (Number)',
      'SELECT': '단일 선택 (Select)'
    }

    const mockCodeStore = {
      getCodeName: (groupCode: string, detailCode: string, fallback: string) => {
        if (groupCode === 'FIELD_TYPE') {
          return mockCodeMap[detailCode] || fallback
        }
        return fallback
      }
    }

    const typeValueGetter = (params: any) => {
      if (!params.data || !params.data.type) return ''
      return mockCodeStore.getCodeName('FIELD_TYPE', params.data.type, params.data.type)
    }

    expect(typeValueGetter({ data: { type: 'EMAIL' } })).toBe('✉️ 이메일 (Email)')
    expect(typeValueGetter({ data: { type: 'TEXT' } })).toBe('문자열 (Text)')
    expect(typeValueGetter({ data: { type: 'UNKNOWN_TYPE' } })).toBe('UNKNOWN_TYPE')
    expect(typeValueGetter({ data: {} })).toBe('')
  })

  it('should calculate appropriate excel column widths based on AG-Grid tableColumnWidth', () => {
    const computeExcelWidth = (f: { tableColumnWidth?: number; gridWidth?: number }) => {
      const agWidth = (f.tableColumnWidth && f.tableColumnWidth > 0) ? f.tableColumnWidth : null
      return agWidth
        ? Math.max(15, Math.min(60, Math.round(agWidth / 7.5)))
        : ((f.gridWidth && f.gridWidth > 0) ? Math.max(15, f.gridWidth * 2.5) : 25)
    }

    // 140px -> 19
    expect(computeExcelWidth({ tableColumnWidth: 140 })).toBe(19)
    // 200px -> 27
    expect(computeExcelWidth({ tableColumnWidth: 200 })).toBe(27)
    // 250px -> 33
    expect(computeExcelWidth({ tableColumnWidth: 250 })).toBe(33)
    // 110px -> 15 (min bound: 15)
    expect(computeExcelWidth({ tableColumnWidth: 110 })).toBe(15)
    // 800px -> 60 (max bound: 60)
    expect(computeExcelWidth({ tableColumnWidth: 800 })).toBe(60)
    // fallback when tableColumnWidth is undefined or 0
    expect(computeExcelWidth({ gridWidth: 4 })).toBe(15)
    expect(computeExcelWidth({ gridWidth: 8 })).toBe(20)
    expect(computeExcelWidth({})).toBe(25)
  })
})

describe('Domain & Node Delete i18n Confirmation Messages', () => {
  it('should format domain delete confirmation message with localized domain name', () => {
    const mockT = (key: string, params?: Record<string, any>) => {
      if (key === 'delete_domain_confirm_desc') {
        return `정말로 [${params?.name}] 도메인을 삭제하시겠습니까? 연결된 하위 분류 노드, 필드 정의 및 마스터 데이터 레코드가 모두 영구 삭제됩니다.`
      }
      if (key === 'domain') return '도메인'
      return key
    }

    const getDomainDisplayName = (target: any, currentLocale = 'ko') => {
      return target.label || (target.originalNameMap ? (target.originalNameMap[currentLocale] || target.originalNameMap.ko || target.originalNameMap.en) : mockT('domain'))
    }

    const domain1 = { label: '인사 마스터' }
    const domain2 = { originalNameMap: { ko: '고객 마스터', en: 'Customer Master' } }
    const domain3 = {}

    expect(mockT('delete_domain_confirm_desc', { name: getDomainDisplayName(domain1) }))
      .toContain('[인사 마스터]')
    expect(mockT('delete_domain_confirm_desc', { name: getDomainDisplayName(domain2, 'en') }))
      .toContain('[Customer Master]')
    expect(mockT('delete_domain_confirm_desc', { name: getDomainDisplayName(domain3) }))
      .toContain('[도메인]')
  })

  it('should format node delete confirmation message with localized node name', () => {
    const mockT = (key: string, params?: Record<string, any>) => {
      if (key === 'confirm_delete_node') {
        return `'${params?.name}' 노드를 정말 삭제하시겠습니까?`
      }
      if (key === 'node') return '분류 노드'
      return key
    }

    const getNodeDisplayName = (target: any, currentLocale = 'ko') => {
      return target.label || (target.originalNameMap ? (target.originalNameMap[currentLocale] || target.originalNameMap.ko || target.originalNameMap.en) : mockT('node'))
    }

    const node1 = { label: '정직원' }
    const node2 = {}

    expect(mockT('confirm_delete_node', { name: getNodeDisplayName(node1) }))
      .toBe("'정직원' 노드를 정말 삭제하시겠습니까?")
    expect(mockT('confirm_delete_node', { name: getNodeDisplayName(node2) }))
      .toBe("'분류 노드' 노드를 정말 삭제하시겠습니까?")
  })
})

describe('Schema Tree Responsive Layout & Scroll Computations', () => {
  it('should compute optimal tree wrapper styles for desktop and mobile resolutions', () => {
    const getTreeWrapperClassAndStyles = (viewportWidth: number, viewportHeight: number) => {
      const isMobile = viewportWidth <= 768
      const isTablet = viewportWidth <= 1024 && !isMobile

      return {
        isMobile,
        isTablet,
        flexGrow: isMobile ? 0 : 1,
        maxHeight: isMobile ? '280px' : 'none',
        minHeight: isMobile ? '160px' : '120px',
        overflowY: 'auto'
      }
    }

    // 1920x1080 Full HD Desktop
    const desktop = getTreeWrapperClassAndStyles(1920, 1080)
    expect(desktop.isMobile).toBe(false)
    expect(desktop.flexGrow).toBe(1)
    expect(desktop.maxHeight).toBe('none')
    expect(desktop.overflowY).toBe('auto')

    // 1366x768 Small Laptop
    const laptop = getTreeWrapperClassAndStyles(1366, 768)
    expect(laptop.isMobile).toBe(false)
    expect(laptop.flexGrow).toBe(1)
    expect(laptop.minHeight).toBe('120px')

    // 1024x768 Tablet
    const tablet = getTreeWrapperClassAndStyles(1024, 768)
    expect(tablet.isTablet).toBe(true)
    expect(tablet.flexGrow).toBe(1)

    // 375x667 Mobile
    const mobile = getTreeWrapperClassAndStyles(375, 667)
    expect(mobile.isMobile).toBe(true)
    expect(mobile.flexGrow).toBe(0)
    expect(mobile.maxHeight).toBe('280px')
  })
})



