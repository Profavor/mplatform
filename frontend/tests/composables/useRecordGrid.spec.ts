import { describe, it, expect, beforeEach } from 'vitest'
import { useRecordGrid } from '~/composables/useRecordGrid'

describe('useRecordGrid', () => {
  let grid: ReturnType<typeof useRecordGrid>

  beforeEach(() => {
    grid = useRecordGrid()
  })

  it('초기 그리드 상태가 정상적으로 설정되어야 한다', () => {
    expect(grid.selectedRecordRows.value).toEqual([])
    expect(grid.selectedRecordId.value).toBeNull()
    expect(grid.columnDefs.value).toEqual([])
    expect(grid.gridApi.value).toBeNull()
  })

  it('onSelectionChanged 호출 시 선택된 행 및 selectedRecordId가 업데이트되어야 한다', () => {
    const mockEvent = {
      api: {
        getSelectedRows: () => [
          { id: 'rec-001', name: '레코드 1' },
          { id: 'rec-002', name: '레코드 2' }
        ]
      }
    }

    grid.onSelectionChanged(mockEvent as any)

    expect(grid.selectedRecordRows.value.length).toBe(2)
    expect(grid.selectedRecordId.value).toBe('rec-001')
  })

  it('선택된 행이 없을 경우 selectedRecordId가 null로 유지/초기화되어야 한다', () => {
    const mockEvent = {
      api: {
        getSelectedRows: () => []
      }
    }

    grid.onSelectionChanged(mockEvent as any)

    expect(grid.selectedRecordRows.value).toEqual([])
    expect(grid.selectedRecordId.value).toBeNull()
  })

  it('generateColumnDefs가 필드 정의 목록을 기반으로 컬럼 정의를 생성해야 한다', () => {
    const fields = [
      { key: 'field1', name: '필드1', type: 'STRING', gridWidth: 150 },
      { key: 'field2', name: '필드2', type: 'NUMBER', gridWidth: 100 }
    ]

    const cols = grid.generateColumnDefs(fields, 'ko')

    expect(cols.length).toBeGreaterThanOrEqual(2)
    expect(cols[0].field).toBe('data.field1')
    expect(cols[0].headerName).toBe('필드1')
    expect(cols[0].width).toBe(150)
  })
})
