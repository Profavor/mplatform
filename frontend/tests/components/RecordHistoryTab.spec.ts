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

  describe('Option 1: 변경 이력 요약 정보 및 속성명 칩 표시 (값 전후 상세 나열 대체)', () => {
    const fieldsMap: Record<string, string> = {
      PRODUCT_ID: '상품코드',
      PRODUCT_NAME: '상품명',
      PRODUCT_PRICE: '상품가격'
    }

    const resolveSourceSystemLabel = (log: any) => {
      if (log.sourceSystem) return log.sourceSystem
      if (log.approvalRequestId) return `결재 승인 (${formatIdentifier(log.approvalRequestId, 'REQ')})`
      return '직접 수정'
    }

    const getVisibleChips = (changedKeys: string[], isExpanded: boolean, maxLimit = 6) => {
      if (isExpanded || changedKeys.length <= maxLimit) {
        return { visible: changedKeys, hasMore: false, remainingCount: 0 }
      }
      return {
        visible: changedKeys.slice(0, maxLimit),
        hasMore: true,
        remainingCount: changedKeys.length - maxLimit
      }
    }

    it('연계 출처(sourceSystem)가 있는 경우 시스템명을 정상 표기하고, 없으면 결재 또는 직접 수정을 표기한다', () => {
      const batchLog = { sourceSystem: 'Spring Batch Pipeline [Stock Ingestion Job]' }
      expect(resolveSourceSystemLabel(batchLog)).toBe('Spring Batch Pipeline [Stock Ingestion Job]')

      const approvalLog = { approvalRequestId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7' }
      expect(resolveSourceSystemLabel(approvalLog)).toBe('결재 승인 (REQ-340a0917)')

      const manualLog = {}
      expect(resolveSourceSystemLabel(manualLog)).toBe('직접 수정')
    })

    it('변경된 속성 목록이 6개 이하일 때는 모든 칩을 표시하고 더보기 칩을 표시하지 않는다', () => {
      const changedKeys = ['PRODUCT_ID', 'PRODUCT_NAME']
      const { visible, hasMore, remainingCount } = getVisibleChips(changedKeys, false, 6)
      expect(visible).toEqual(['PRODUCT_ID', 'PRODUCT_NAME'])
      expect(hasMore).toBe(false)
      expect(remainingCount).toBe(0)
    })

    it('변경된 속성 목록이 6개를 초과할 때 접힌 상태에서는 6개만 노출하고 잔여 개수를 계산한다', () => {
      const changedKeys = ['F1', 'F2', 'F3', 'F4', 'F5', 'F6', 'F7', 'F8', 'F9']
      const { visible, hasMore, remainingCount } = getVisibleChips(changedKeys, false, 6)
      expect(visible.length).toBe(6)
      expect(hasMore).toBe(true)
      expect(remainingCount).toBe(3)
    })

    it('펼친 상태에서는 6개를 초과하더라도 전체 칩을 노출한다', () => {
      const changedKeys = ['F1', 'F2', 'F3', 'F4', 'F5', 'F6', 'F7', 'F8', 'F9']
      const { visible, hasMore } = getVisibleChips(changedKeys, true, 6)
      expect(visible.length).toBe(9)
      expect(hasMore).toBe(false)
    })

    it('속성명 라벨 매핑 시 필드명이 다국어 라벨로 정상 변환된다', () => {
      const changedKey = 'PRODUCT_PRICE'
      const label = fieldsMap[changedKey] || changedKey
      expect(label).toBe('상품가격')
    })
  })
})
