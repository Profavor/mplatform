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

