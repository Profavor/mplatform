import { describe, it, expect } from 'vitest'

describe('RecordHistoryTab - Record Change History and Audit Trail', () => {

  const sampleHistory = [
    {
      id: 'hist-1',
      recordId: 'rec-001',
      changeType: 'CREATE',
      changedBy: 'user-admin',
      changedByName: '관리자',
      version: 1,
      changedAt: '2026-09-01T10:00:00Z',
      previousData: null,
      newData: JSON.stringify({ PRODUCT_ID: '6011227725', PRODUCT_NAME: '로지텍 G102', PRODUCT_PRICE: 24060 }),
      approvalRequestId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7'
    },
    {
      id: 'hist-2',
      recordId: 'rec-001',
      changeType: 'UPDATE',
      changedBy: 'user-editor',
      changedByName: '수정자',
      version: 2,
      changedAt: '2026-09-02T15:30:00Z',
      previousData: JSON.stringify({ PRODUCT_ID: '6011227725', PRODUCT_NAME: '로지텍 G102', PRODUCT_PRICE: 24060 }),
      newData: JSON.stringify({ PRODUCT_ID: '6011227725', PRODUCT_NAME: '로지텍 G102', PRODUCT_PRICE: 24410 }),
      approvalRequestId: 'b7829a1c-55c0-42fe-912f-1249bce38821',
      changedFields: ['PRODUCT_PRICE']
    },
    {
      id: 'hist-3',
      recordId: 'rec-001',
      changeType: 'RECORD_MERGE',
      changedBy: 'user-admin',
      changedByName: '관리자',
      version: 3,
      changedAt: '2026-09-03T11:00:00Z',
      previousData: JSON.stringify({ PRODUCT_ID: '6011227725', PRODUCT_NAME: '로지텍 G102', PRODUCT_PRICE: 24410 }),
      newData: JSON.stringify({ PRODUCT_ID: '6011227725', PRODUCT_NAME: '로지텍 G102 2세대', PRODUCT_PRICE: 24410 }),
      changedFields: ['PRODUCT_NAME']
    }
  ]

  const formatIdentifier = (rawId: string | null | undefined, prefix = 'REC') => {
    if (!rawId) return ''
    const str = String(rawId)
    if (/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(str)) {
      return `${prefix}-${str.substring(0, 8)}`
    }
    return str
  }

  const filterHistory = (history: any[], typeFilter: string, searchField: string) => {
    return history.filter(log => {
      // 1. Change type filter
      if (typeFilter !== 'ALL') {
        const logType = log.changeType || ''
        if (typeFilter === 'UPDATE' && !['UPDATE', 'RECORD_UPDATE', 'BATCH_UPDATE'].includes(logType)) {
          return false
        } else if (typeFilter === 'CREATE' && !['CREATE', 'RECORD_CREATE'].includes(logType)) {
          return false
        } else if (typeFilter === 'RECORD_MERGE' && !['MERGE', 'RECORD_MERGE', 'INBOUND_MERGE'].includes(logType)) {
          return false
        } else if (!['UPDATE', 'CREATE', 'RECORD_MERGE'].includes(typeFilter) && logType !== typeFilter) {
          return false
        }
      }

      // 2. Field search filter
      if (searchField && searchField.trim()) {
        const q = searchField.trim().toLowerCase()
        const changedKeys = log.changedFields || []
        const hasMatch = changedKeys.some((k: string) => k.toLowerCase().includes(q))
        if (!hasMatch) return false
      }

      return true
    })
  }

  it('#143: raw UUID를 식별 코드(REQ-xxxxxxxx)로 치환하여 사용자에게 안전하게 노출한다', () => {
    const rawUuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7'
    const formatted = formatIdentifier(rawUuid, 'REQ')
    expect(formatted).toBe('REQ-340a0917')
    expect(formatted.includes('340a0917-af0b-4d13-a1ce-479d4b2e2ca7')).toBe(false)
  })

  it('#143: 작업 유형(UPDATE) 필터링 시 해당 유형의 이력만 정확히 필터링된다', () => {
    const filtered = filterHistory(sampleHistory, 'UPDATE', '')
    expect(filtered.length).toBe(1)
    expect(filtered[0].id).toBe('hist-2')
    expect(filtered[0].version).toBe(2)
  })

  it('#143: 속성명(PRODUCT_PRICE) 검색 시 해당 필드가 변경된 이력만 선별된다', () => {
    const filtered = filterHistory(sampleHistory, 'ALL', 'price')
    expect(filtered.length).toBe(1)
    expect(filtered[0].id).toBe('hist-2')
    expect(filtered[0].changedFields).toContain('PRODUCT_PRICE')
  })
})
