import { describe, it, expect, beforeEach } from 'vitest'
import { serializeGridState, deserializeGridState } from '../../composables/useGridState'

describe('useGridState Composable (TDD)', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('그리드 상태 객체를 JSON 직렬화 및 역직렬화할 수 있어야 함', () => {
    const mockState = {
      columnState: [{ colId: 'id', width: 120, hide: false }],
      sortState: [{ colId: 'id', sort: 'asc' }],
    }

    const serialized = serializeGridState(mockState)
    expect(serialized).toBeTypeOf('string')

    const restored = deserializeGridState(serialized)
    expect(restored).toEqual(mockState)
  })

  it('잘못된 JSON 파싱 시 null을 안전하게 반환해야 함', () => {
    expect(deserializeGridState('invalid-json')).toBeNull()
    expect(deserializeGridState(null)).toBeNull()
  })

  it('다중 그리드 키(gridKey)별로 상태가 격리되어 저장되어야 함', () => {
    const key1 = 'grid_a'
    const key2 = 'grid_b'
    
    const stateA = { columnState: [{ colId: 'field_a', width: 100 }] }
    const stateB = { columnState: [{ colId: 'field_b', width: 200 }] }

    localStorage.setItem(`ag_grid_state_${key1}`, serializeGridState(stateA))
    localStorage.setItem(`ag_grid_state_${key2}`, serializeGridState(stateB))

    const restoredA = deserializeGridState(localStorage.getItem(`ag_grid_state_${key1}`))
    const restoredB = deserializeGridState(localStorage.getItem(`ag_grid_state_${key2}`))

    expect(restoredA).toEqual(stateA)
    expect(restoredB).toEqual(stateB)
    expect(restoredA).not.toEqual(restoredB)
  })
})
